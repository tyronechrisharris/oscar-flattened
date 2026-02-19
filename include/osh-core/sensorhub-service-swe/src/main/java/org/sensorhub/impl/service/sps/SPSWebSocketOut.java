/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sps;

import java.io.IOException;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.sensorhub.impl.service.WebSocketOutputStream;
import org.sensorhub.impl.service.WebSocketUtils;
import org.slf4j.Logger;
import org.vast.cdm.common.DataStreamWriter;
import net.opengis.swe.v20.DataBlock;


/**
 * <p>
 * Output only websocket for sending live SPS commands to registered sensor
 * </p>
 *
 * @author Alex Robin
 * @since Dec 20, 2016
 */
public class SPSWebSocketOut implements WebSocketListener, Subscriber<DataBlock> 
{
    final static String WS_WRITE_ERROR = "Error writing command message to websocket";
    final static String SUBSCRIBE_ERROR = "Error forwarding command messages to outgoing websocket";
    
    ISPSConnector connector;
    DataStreamWriter writer;
    Logger log;
    Session session;
    Subscription subscription;
    
    
    public SPSWebSocketOut(ISPSConnector connector, DataStreamWriter writer, Logger log)
    {
        this.connector = connector;
        this.writer = writer;
        this.log = log;
    }
    
    
    @Override
    public void onWebSocketConnect(Session session)
    {
        this.session = session;
        WebSocketUtils.logOpen(session, log);
        
        try
        {
            writer.setOutput(new WebSocketOutputStream(session, 1024, true, log));
            connector.subscribeToCommands(writer.getDataComponents(), this);
        }
        catch (Exception e)
        {
            WebSocketUtils.closeSession(session, StatusCode.SERVER_ERROR, WebSocketUtils.INIT_ERROR, log);
        }
    }


    @Override
    public void onWebSocketError(Throwable e)
    {
        if (subscription != null)
            subscription.cancel();
        log.error(WebSocketUtils.SEND_ERROR_MSG, e);
        WebSocketUtils.closeSession(session, StatusCode.PROTOCOL, "", log);
    }
    
    
    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        if (subscription != null)
            subscription.cancel();
        WebSocketUtils.logClose(session, statusCode, reason, log);
    }
    
    
    @Override
    public void onWebSocketBinary(byte payload[], int offset, int len)
    {
        WebSocketUtils.closeSession(session, StatusCode.BAD_DATA, WebSocketUtils.INPUT_NOT_SUPPORTED, log);
    }


    @Override
    public void onWebSocketText(String msg)
    {
        WebSocketUtils.closeSession(session, StatusCode.BAD_DATA, WebSocketUtils.INPUT_NOT_SUPPORTED, log);
    }


    @Override
    public void onSubscribe(Subscription subscription)
    {
        this.subscription = subscription;
        subscription.request(Long.MAX_VALUE);
    }


    @Override
    public void onNext(DataBlock cmdData)
    {
        try
        {
            writer.write(cmdData);
            writer.flush();
        }
        catch (IOException e)
        {
            WebSocketUtils.closeSession(session, StatusCode.SERVER_ERROR, WS_WRITE_ERROR, log);
            log.error(WS_WRITE_ERROR, e);
        }
        
    }


    @Override
    public void onError(Throwable e)
    {
        if (subscription != null)
            subscription.cancel();
        WebSocketUtils.closeSession(session, StatusCode.SERVER_ERROR, WebSocketUtils.INPUT_NOT_SUPPORTED, log);
        log.error(SUBSCRIBE_ERROR, e);
    }


    @Override
    public void onComplete()
    {
        WebSocketUtils.closeSession(session, StatusCode.NORMAL, "complete", log);
    }
}
