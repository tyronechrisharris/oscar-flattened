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
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import org.sensorhub.impl.service.swe.IAsyncOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.util.Asserts;


/**
 * <p>
 * Wrapper for servlet output stream that always checks isReady() before
 * flushing or writing out any data.
 * </p>
 *
 * @author Alex Robin
 * @since Nov 25, 2020
 */
public class BufferedAsyncOutputStream extends OutputStream implements IAsyncOutputStream, WriteListener
{
    static Logger log = LoggerFactory.getLogger(BufferedAsyncOutputStream.class);
    
    ServletOutputStream out;
    WriteListener listener;
    byte[] buffer;
    int bytesToWrite;
    boolean flushRequested;
    
    
    public BufferedAsyncOutputStream(ServletOutputStream out, int bufferSize)
    {
        this.out = out;
        this.buffer = new byte[bufferSize];
    }


    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        if (out.isReady())
        {
            out.write(b, off, len);
            //log.debug("Direct write: " + len + " bytes");
        }
        else
        {
            System.arraycopy(b, off, buffer, bytesToWrite, len);
            bytesToWrite += len;
        }
    }


    @Override
    public void write(int b) throws IOException
    {
        if (out.isReady())
        {
            out.write(b);
            //log.debug("Direct write: 1 byte");
        }
        else
        {
            buffer[bytesToWrite] = (byte)(b & 0xFF);
            bytesToWrite++;
        }        
    }
    
    
    @Override
    public void flush() throws IOException
    {
        if (out.isReady())
        {
            //log.debug("Direct flush");
            flushBuffer();
            flushUnderlyingStream();            
        }
        else
            flushRequested = true;
    }
    
    
    @Override
    public void close() throws IOException
    {
        flushBuffer();
        out.close();
    }
    
    
    protected void flushBuffer() throws IOException
    {
        // flush all bytes from buffer
        if (bytesToWrite > 0)
        {
            out.write(buffer, 0, bytesToWrite);
            //log.debug("Deferred write: " + bytesToWrite + " bytes");
            bytesToWrite = 0;
            
            if (flushRequested)
            {
                //log.debug("Deferred flush");
                flushUnderlyingStream();
            }   
        }
    }
    
    
    protected void flushUnderlyingStream() throws IOException
    {
        if (out.isReady())
        {
            out.flush();
            flushRequested = false;
        }
    }

    
    @Override
    public void onWritePossible() throws IOException
    {
        flushBuffer();
        
        if (listener != null)
            listener.onWritePossible();
    }


    @Override
    public void onError(Throwable t)
    {
        if (listener != null)
            listener.onError(t);
    }


    @Override
    public boolean isClosed()
    {
        // cannot detect it here, we have to wait for EOFException
        return false;
    }
    
    
    @Override
    public boolean isReady()
    {
        return out.isReady();
    }
    
    
    @Override
    public void setWriteListener(WriteListener listener)
    {
        this.listener = Asserts.checkNotNull(listener, WriteListener.class);
        out.setWriteListener(this);
    }

}
