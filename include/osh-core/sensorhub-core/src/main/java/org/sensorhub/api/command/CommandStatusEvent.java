/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.command;

import org.vast.util.Asserts;


/**
 * <p>
 * Event carrying command status data
 * </p>
 *
 * @author Alex Robin
 * @date Mar 9, 2021
 */
public class CommandStatusEvent extends CommandStreamEvent
{
    protected long correlationID;
    protected ICommandStatus status;
    
    
    public CommandStatusEvent(long timeStamp, String sysUID, String controlInputName, long correlationID, ICommandStatus status)
    {
        super(timeStamp, sysUID, controlInputName);
        this.correlationID = correlationID;
        this.status = Asserts.checkNotNull(status, ICommandStatus.class);
    }
    
    
    public CommandStatusEvent(IStreamingControlInterface controlInterface, long correlationID, ICommandStatus status)
    {
        this(System.currentTimeMillis(),
             controlInterface.getParentProducer().getUniqueIdentifier(),
             controlInterface.getName(),
             correlationID,
             status);
    }


    public long getCorrelationID()
    {
        return correlationID;
    }


    public ICommandStatus getStatus()
    {
        return status;
    }
    
    
    public CommandStatusEvent withCorrelationID(long correlationID)
    {
        return new CommandStatusEvent(this.getTimeStamp(),
            this.getSystemUID(),
            this.getControlInputName(),
            correlationID,
            this.getStatus());
    }

}
