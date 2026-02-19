/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sos;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sensorhub.impl.service.swe.IAsyncOutputStream;
import org.vast.ows.OWSRequest;
import org.vast.util.Asserts;


/**
 * <p>
 * Base class with common async logic for all serializers
 * </p>
 * 
 * @param <R> The service request type
 * @param <T> The serialized item type
 *
 * @author Alex Robin
 * @date Apr 16, 2020
 */
public abstract class AbstractAsyncSerializer<R extends OWSRequest, T> implements IAsyncResponseSerializer<R, T>
{
    protected SOSServlet servlet;
    protected R request;
    protected AsyncContext asyncCtx;
    protected OutputStream os;
    protected IAsyncOutputStream asyncOs;
    protected Queue<T> recordQueue;
    volatile Subscription subscription;
    volatile boolean done = false;
    volatile boolean beforeRecordsCalled = false;
    volatile boolean onCompleteCalled = false;
    volatile boolean writing = false;

    // atomic synchronization flags
    static final int READY = 0;
    static final int WRITING = 1;
    static final int MORE_TO_WRITE = 2;
    AtomicInteger cas = new AtomicInteger(READY);
    

    protected abstract void beforeRecords() throws IOException;

    protected abstract void afterRecords() throws IOException;

    protected abstract void writeRecord(T item) throws IOException;


    public void init(SOSServlet servlet, AsyncContext asyncCtx, R request) throws IOException
    {
        this.servlet = Asserts.checkNotNull(servlet, SOSServlet.class);
        this.request = Asserts.checkNotNull(request, OWSRequest.class);
        this.recordQueue = new ConcurrentLinkedQueue<>();

        // init output stream with either:
        // - async context servlet output stream
        // - websocket output stream
        Asserts.checkArgument(asyncCtx != null || request.getResponseStream() instanceof IAsyncOutputStream, "invalid output stream");
        if (asyncCtx != null)
        {
            this.asyncCtx = asyncCtx;
            asyncCtx.addListener(this);
            this.os = new BufferedAsyncOutputStream(asyncCtx.getResponse().getOutputStream(), 16 * 1024);            
        }
        else
            this.os = request.getResponseStream();
        this.asyncOs = (IAsyncOutputStream) os;
    }


    @Override
    public void onSubscribe(Subscription subscription)
    {
        this.subscription = Asserts.checkNotNull(subscription, Subscription.class);
        asyncOs.setWriteListener(this);
        subscription.request(1);
    }


    @Override
    public void onWritePossible() throws IOException
    {
        // cancel subscription if output stream was closed asynchronously
        // i.e. if websocket session was closed by client
        if (asyncOs.isClosed())
        {
            close();
            return;
        }

        // write everything we can while servlet output stream is ready
        // otherwise we'll be called back later
        if (asyncOs.isReady() && cas.compareAndSet(READY, WRITING))
        {
            do
            {
                if (!beforeRecordsCalled)
                {
                    beforeRecords();
                    beforeRecordsCalled = true;
                }

                while (!recordQueue.isEmpty() && asyncOs.isReady())
                {
                    writeRecord(recordQueue.remove());
                    if (subscription != null)
                        subscription.request(1);
                }

                if (recordQueue.isEmpty() && onCompleteCalled)
                {
                    afterRecords();
                    close();
                    done = true;
                }
            }
            while (!done && cas.getAndUpdate(s -> s == MORE_TO_WRITE ? WRITING : READY) == MORE_TO_WRITE);
        }
    }


    @Override
    public void onNext(T item)
    {
        try
        {
            //System.out.println("\nonNext()");
            if (!onCompleteCalled)
            {
                if (!recordQueue.offer(item))
                    onError(new IllegalStateException("Write queue is full"));

                if (!cas.compareAndSet(WRITING, MORE_TO_WRITE))
                    onWritePossible();
            }
        }
        catch (Throwable e)
        {
            onError(e);
        }
    }


    @Override
    public void onComplete()
    {
        try
        {
            //System.out.println("onComplete()");
            if (!onCompleteCalled)
            {
                onCompleteCalled = true;
                if (!cas.compareAndSet(WRITING, MORE_TO_WRITE))
                    onWritePossible();
            }
        }
        catch (Throwable e)
        {
            onError(e);
        }
    }


    @Override
    public void onError(Throwable e)
    {
        if (asyncCtx != null)
        {
            servlet.handleError(
                (HttpServletRequest) asyncCtx.getRequest(),
                (HttpServletResponse) asyncCtx.getResponse(),
                null, e);
        }

        try
        {
            close();
        }
        catch (IOException e1)
        {
        }
    }


    protected void close() throws IOException
    {
        os.close();
        if (asyncCtx != null)
            asyncCtx.complete();
        if (subscription != null)
            subscription.cancel();
    }


    @Override
    public void onTimeout(AsyncEvent event) throws IOException
    {
        close();
        servlet.getLogger().debug("Connection closed on idle timeout");
    }


    @Override
    public void onError(AsyncEvent event) throws IOException
    {
        close();
        servlet.getLogger().debug("Async error", event.getThrowable());
    }


    @Override
    public void onComplete(AsyncEvent event) throws IOException
    {
        if (subscription != null)
            subscription.cancel();
        servlet.getLogger().debug("Asynchronous connection complete");
    }


    @Override
    public void onStartAsync(AsyncEvent event) throws IOException
    {
    }

}