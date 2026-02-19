/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.command;

import org.vast.util.Asserts;


/**
 * <p>
 * Event carrying command data sent to a command receiver
 * </p>
 *
 * @author Alex Robin
 * @date Mar 9, 2021
 */
public class CommandEvent extends CommandStreamEvent
{
    protected ICommandData command;
    protected long correlationID;
    
    
    public CommandEvent(long timeStamp, String sysUID, String controlInputName, ICommandData command, long correlationID)
    {
        super(timeStamp, sysUID, controlInputName);
        this.command = Asserts.checkNotNull(command, ICommandData.class);
        this.sourceID = Asserts.checkNotNullOrEmpty(command.getSenderID(), "senderID");
        this.correlationID = correlationID;
    }
    
    
    public CommandEvent(String sysUID, String controlInputName, ICommandData command, long correlationID)
    {
        this(System.currentTimeMillis(), sysUID, controlInputName, command, correlationID);
    }


    public long getCorrelationID()
    {
        return correlationID;
    }


    public ICommandData getCommand()
    {
        return command;
    }
}
