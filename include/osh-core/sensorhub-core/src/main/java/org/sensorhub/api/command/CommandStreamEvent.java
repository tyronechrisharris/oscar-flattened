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

import org.sensorhub.api.common.BigId;
import org.sensorhub.api.event.EventUtils;
import org.sensorhub.api.system.SystemEvent;
import org.vast.util.Asserts;


/**
 * <p>
 * Base class for all command stream (i.e. tasking/control input) related events
 * </p>
 *
 * @author Alex Robin
 * @date Nov 23, 2020
 */
public abstract class CommandStreamEvent extends SystemEvent
{
    String controlInputName;
    protected BigId cmdStreamID;


    /**
     * @param timeStamp time of event generation (unix time in milliseconds, base 1970)
     * @param sysUID Unique ID of parent system
     * @param controlInputName Name of control input
     */
    public CommandStreamEvent(long timeStamp, String sysUID, String controlInputName)
    {
        super(timeStamp, sysUID);
        this.controlInputName = controlInputName;
    }
    
    
    /**
     * Helper constructor that sets the timestamp to current system time
     * @param sysUID Unique ID of parent system
     * @param controlInputName Name of output producing the datastream
     */
    public CommandStreamEvent(String sysUID, String controlInputName)
    {
        super(sysUID);
        this.controlInputName = controlInputName;
    }
    
    
    /**
     * Helper constructor that sets the timestamp to current system time
     * @param csInfo Command stream the event relates to
     */
    public CommandStreamEvent(ICommandStreamInfo csInfo)
    {
        super(Asserts.checkNotNull(csInfo, ICommandStreamInfo.class).getSystemID().getUniqueID());
        this.controlInputName = csInfo.getControlInputName();
    }


    /**
     * @return Name of control input
     */
    public String getControlInputName()
    {
        return controlInputName;
    }


    /**
     * @return Internal ID of the command stream related to this event
     */
    public BigId getCommandStreamID()
    {
        return cmdStreamID;
    }
    
    
    /**
     * Called by the framework to assign the command stream's local ID to this event.
     * This can only be called once and must be called before the event is
     * dispatched.
     * @param internalID Internal ID of related command stream
     */
    public void assignCommandStreamID(BigId internalID)
    {
        Asserts.checkState(cmdStreamID == null, "Command stream ID is already assigned");
        this.cmdStreamID = internalID;
    }


    @Override
    public String getSourceID()
    {
        if (sourceID == null)
            sourceID = EventUtils.getCommandStreamStatusTopicID(systemUID, controlInputName);
        return sourceID;
    }
}
