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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;


/**
 * <p>
 * Simulated servlet async context used for JUnit tests
 * </p>
 *
 * @author Alex Robin
 * @date Apr 9, 2020
 */
public class TestAsyncContext implements AsyncContext
{
    static int WRITE_BUFFER_READY_LIMIT = 64;
    static int WRITE_BUFFER_CONSUME_DELAY_MS = 300;
    ByteArrayOutputStream os = new ByteArrayOutputStream();    
    AtomicInteger counter = new AtomicInteger();
    
    ServletOutputStream servletOS = new ServletOutputStream() {
        WriteListener writeListener;
        AtomicBoolean ready = new AtomicBoolean(true);        
        
        @Override
        public boolean isReady()
        {
            if (counter.get() >= WRITE_BUFFER_READY_LIMIT)
                ready.set(false);
            return ready.get();
        }

        @Override
        public void setWriteListener(WriteListener writeListener)
        {
            this.writeListener = writeListener;
            Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this::writePossible, 100, WRITE_BUFFER_CONSUME_DELAY_MS, TimeUnit.MILLISECONDS);
        }

        @Override
        public void write(int b) throws IOException
        {
            System.out.write(b);
            os.write(b);
            counter.incrementAndGet();
        }
        
        void writePossible()
        {
            try
            {
                if (ready.compareAndSet(false, true))
                {
                    counter.set(0);
                    writeListener.onWritePossible();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        
        @Override
        public void flush() throws IOException
        {
            System.out.flush();
        }
        
        @Override
        public void close() throws IOException
        {
            flush();
            counter.set(0);
        }
    };
    
    
    private class DummyServletResponse implements HttpServletResponse
    {
        @Override
        public String getCharacterEncoding()
        {
            return null;
        }

        @Override
        public String getContentType()
        {
            return null;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException
        {
            return servletOS;
        }

        @Override
        public PrintWriter getWriter() throws IOException
        {
            return null;
        }

        @Override
        public void setCharacterEncoding(String charset)
        {            
        }

        @Override
        public void setContentLength(int len)
        {            
        }

        @Override
        public void setContentLengthLong(long len)
        {            
        }

        @Override
        public void setContentType(String type)
        {
            System.out.println("ContentType: " + type);
        }

        @Override
        public void setBufferSize(int size)
        {            
        }

        @Override
        public int getBufferSize()
        {
            return 0;
        }

        @Override
        public void flushBuffer() throws IOException
        {            
        }

        @Override
        public void resetBuffer()
        {            
        }

        @Override
        public boolean isCommitted()
        {
            return false;
        }

        @Override
        public void reset()
        {            
        }

        @Override
        public void setLocale(Locale loc)
        {            
        }

        @Override
        public Locale getLocale()
        {
            return null;
        }

        @Override
        public void addCookie(Cookie cookie)
        {            
        }

        @Override
        public boolean containsHeader(String name)
        {
            return false;
        }

        @Override
        public String encodeURL(String url)
        {
            return null;
        }

        @Override
        public String encodeRedirectURL(String url)
        {
            return null;
        }

        @Override
        public String encodeUrl(String url)
        {
            return null;
        }

        @Override
        public String encodeRedirectUrl(String url)
        {
            return null;
        }

        @Override
        public void sendError(int sc, String msg) throws IOException
        {            
        }

        @Override
        public void sendError(int sc) throws IOException
        {            
        }

        @Override
        public void sendRedirect(String location) throws IOException
        {            
        }

        @Override
        public void setDateHeader(String name, long date)
        {            
        }

        @Override
        public void addDateHeader(String name, long date)
        {            
        }

        @Override
        public void setHeader(String name, String value)
        {            
        }

        @Override
        public void addHeader(String name, String value)
        {            
        }

        @Override
        public void setIntHeader(String name, int value)
        {            
        }

        @Override
        public void addIntHeader(String name, int value)
        {            
        }

        @Override
        public void setStatus(int sc)
        {            
        }

        @Override
        public void setStatus(int sc, String sm)
        {            
        }

        @Override
        public int getStatus()
        {
            return 0;
        }

        @Override
        public String getHeader(String name)
        {
            return null;
        }

        @Override
        public Collection<String> getHeaders(String name)
        {
            return null;
        }

        @Override
        public Collection<String> getHeaderNames()
        {
            return null;
        }
        
    }


    @Override
    public void complete()
    {
        try
        {
            servletOS.close();
        }
        catch (IOException e)
        {
        }
    }
    
    
    @Override
    public ServletRequest getRequest()
    {
        return null;
    }


    @Override
    public ServletResponse getResponse()
    {
        return new DummyServletResponse();
    }


    @Override
    public boolean hasOriginalRequestAndResponse()
    {
        return true;
    }


    @Override
    public void dispatch()
    {
    }


    @Override
    public void dispatch(String path)
    {
    }


    @Override
    public void dispatch(ServletContext context, String path)
    {
    }


    @Override
    public void start(Runnable run)
    {
    }


    @Override
    public void addListener(AsyncListener listener)
    {
    }


    @Override
    public void addListener(AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse)
    {
    }


    @Override
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException
    {
        return null;
    }


    @Override
    public void setTimeout(long timeout)
    {
    }


    @Override
    public long getTimeout()
    {
        return 0;
    }

}
