/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.sensorhub.impl.service.WebSocketOutputStream;
import org.sensorhub.impl.service.WebSocketUtils;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.slf4j.Logger;
import org.vast.util.Asserts;


/**
 * <p>
 * Output only websocket for sending real-time events and obs data
 * </p>
 *
 * @author Alex Robin
 * @since Feb 5, 2021
 */
public class WebSocketOut implements WebSocketListener, StreamHandler
{
    RequestContext ctx;
    Logger log;
    Session session;
    WebSocketOutputStream os;
    Runnable onStart, onClose;
    AtomicBoolean closed = new AtomicBoolean();
    
    
    public WebSocketOut(Logger log)
    {
        this.log = Asserts.checkNotNull(log, Logger.class);
    }
    
    
    @Override
    public void onWebSocketConnect(Session session)
    {
        this.session = session;
        WebSocketUtils.logOpen(session, log);
        this.os = new WebSocketOutputStream(session, 1024, false, log);
        
        if (onStart != null)
            onStart.run();
    }


    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        if (!closed.get())
        {
            WebSocketUtils.logClose(session, statusCode, reason, log);
            
            if (onClose != null)
                onClose.run();
            
            session = null;
        }
    }
    
    
    @Override
    public void onWebSocketError(Throwable e)
    {
        log.error(WebSocketUtils.PROTOCOL_ERROR_MSG, e);
    }
    
    
    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len)
    {
        WebSocketUtils.closeSession(session, StatusCode.BAD_DATA, WebSocketUtils.INPUT_NOT_SUPPORTED, log);
    }


    @Override
    public void onWebSocketText(String msg)
    {
        WebSocketUtils.closeSession(session, StatusCode.BAD_DATA, WebSocketUtils.INPUT_NOT_SUPPORTED, log);
    }
    
    
    public OutputStream getOutputStream()
    {
        return os;
    }


    @Override
    public void setStartCallback(Runnable onStart)
    {
        this.onStart = Asserts.checkNotNull(onStart, "onStart");
    }


    @Override
    public void setCloseCallback(Runnable onClose)
    {
        this.onClose = Asserts.checkNotNull(onClose, "onClose");
    }


    @Override
    public void sendPacket() throws IOException
    {
        if (session != null && session.isOpen())
            os.send();
    }


    @Override
    public void close()
    {
        if (closed.compareAndSet(false, true))
            os.close();
    }
}
