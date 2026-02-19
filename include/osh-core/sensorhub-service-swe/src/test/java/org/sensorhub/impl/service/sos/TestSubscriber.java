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

import static org.junit.Assert.fail;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * <p>
 * Base subscriber implementation used in JUnit tests
 * </p>
 * 
 * @param <T> The subscribed type
 *
 * @author Alex Robin
 * @date Apr 16, 2020
 */
public abstract class TestSubscriber<T> implements Subscriber<T>
{
    Subscription sub;
    AtomicBoolean isDone = new AtomicBoolean(false);
    T lastItem;
    
    
    protected abstract void checkItem(T item);
    
    
    @Override
    public void onSubscribe(Subscription sub)
    {
        this.sub = sub;
        sub.request(1);
    }
    

    @Override
    public void onNext(T item)
    {
        if (isDone.get())
            fail("onNext() should never be called after onComplete()");
        
        if (lastItem != null && item == lastItem)
            fail("Duplicate item");
        lastItem = item;
        
        checkItem(item);
        sub.request(1);
    }
    

    @Override
    public void onError(Throwable e)
    {
        e.printStackTrace();
        fail(e.getMessage());
    }
    

    @Override
    public void onComplete()
    {
        if (isDone.get())
            fail("onComplete() already called once");
        
        System.out.println("Done");
        isDone.set(true);
        
        synchronized (isDone)
        {
            isDone.notifyAll();
        }
    }
    
    
    public boolean awaitComplete(long timeout) throws InterruptedException
    {
        synchronized (isDone)
        {
            boolean hasTimedOut = false;
            long t0 = System.currentTimeMillis();
            while (!isDone.get() && !(hasTimedOut = System.currentTimeMillis() - t0 >= timeout))
                isDone.wait(timeout);
            return hasTimedOut;
        }
    }

}
