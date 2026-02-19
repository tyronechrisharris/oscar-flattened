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


/**
 * <p>
 * Event sent when a command stream (i.e. tasking/control input) is enabled
 * </p>
 *
 * @author Alex Robin
 * @date Nov 23, 2020
 */
public class CommandStreamEnabledEvent extends CommandStreamEvent
{
    
    /**
     * Pass-through to super class constructor
     * @see CommandStreamEvent#CommandStreamEvent(long, String, String)
     */
    public CommandStreamEnabledEvent(long timeStamp, String sysUID, String controlInputName)
    {
        super(timeStamp, sysUID, controlInputName);
    }
    
    
    /**
     * Pass-through to super class constructor
     * @see CommandStreamEvent#CommandStreamEvent(String, String)
     */
    public CommandStreamEnabledEvent(String sysUID, String controlInputName)
    {
        super(sysUID, controlInputName);
    }
    
    
    /**
     * Pass-through to super class constructor
     * @see CommandStreamEvent#CommandStreamEvent(ICommandStreamInfo)
     */
    public CommandStreamEnabledEvent(ICommandStreamInfo csInfo)
    {
        super(csInfo);
    }
}
