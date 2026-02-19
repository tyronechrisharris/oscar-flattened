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


public interface StreamHandler
{

    public void setStartCallback(Runnable onStart);
    
    public void setCloseCallback(Runnable onClose);
    
    public OutputStream getOutputStream();
    
    public void sendPacket() throws IOException;
    
    public default void sendPacket(long correlId) throws IOException
    {
        sendPacket();
    }
    
    public void close();
}
