/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.swe;

import javax.servlet.WriteListener;
import org.eclipse.jetty.websocket.api.Session;
import org.sensorhub.impl.service.WebSocketOutputStream;
import org.slf4j.Logger;


/**
 * <p>
 * Adapter output stream for sending data to a websocket.<br/>
 * Data is actually sent to the web socket only when flush() is called.
 * </p>
 *
 * @author Alex Robin
 * @since Feb 19, 2015
 */
public class WebSocketOutputStreamAsync extends WebSocketOutputStream implements IAsyncOutputStream
{
    
    public WebSocketOutputStreamAsync(Session session, int bufferSize, boolean autoSendOnFlush, Logger log)
    {
        super(session, bufferSize, autoSendOnFlush, log);
    }
    
    
    @Override
    public boolean isClosed()
    {
        return !session.isOpen();
    }
    
    
    @Override
    public boolean isReady()
    {
        return true;
    }
    
    
    @Override
    public void setWriteListener(WriteListener listener)
    {
    }

}
