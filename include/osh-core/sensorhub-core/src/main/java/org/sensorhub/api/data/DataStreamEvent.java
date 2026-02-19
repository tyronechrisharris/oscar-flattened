/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.

******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.data;

import org.sensorhub.api.common.BigId;
import org.sensorhub.api.event.EventUtils;
import org.sensorhub.api.system.SystemEvent;
import org.vast.util.Asserts;


/**
 * <p>
 * Base class for all datastream (i.e. output) related events
 * </p>
 *
 * @author Alex Robin
 * @date Sep 28, 2020
 */
public abstract class DataStreamEvent extends SystemEvent
{
    protected String outputName;
    protected BigId dataStreamID;


    /**
     * @param timeStamp time of event generation (unix time in milliseconds, base 1970)
     * @param sysUID Unique ID of parent system
     * @param outputName Name of output producing the datastream
     */
    public DataStreamEvent(long timeStamp, String sysUID, String outputName)
    {
        super(timeStamp, sysUID);
        this.outputName = Asserts.checkNotNullOrEmpty(outputName, "outputName");
    }
    
    
    /**
     * Helper constructor that sets the timestamp to current system time
     * @param sysUID Unique ID of parent system
     * @param outputName Name of output producing the datastream
     */
    public DataStreamEvent(String sysUID, String outputName)
    {
        this(System.currentTimeMillis(), sysUID, outputName);
    }
    
    
    /**
     * Helper constructor that sets the timestamp to current system time
     * @param dsInfo Data stream the event relates to
     */
    public DataStreamEvent(IDataStreamInfo dsInfo)
    {
        super(Asserts.checkNotNull(dsInfo, IDataStreamInfo.class).getSystemID().getUniqueID());
        this.outputName = Asserts.checkNotNullOrEmpty(dsInfo.getOutputName(), "outputName");
    }


    /**
     * @return Name of datastream
     */
    public String getOutputName()
    {
        return outputName;
    }


    /**
     * @return Internal ID of the datastream related to this event
     */
    public BigId getDataStreamID()
    {
        return dataStreamID;
    }
    
    
    /**
     * Called by the framework to assign the datastream's local ID to this event.
     * This can only be called once and must be called before the event is
     * dispatched.
     * @param internalID Local ID of related datastream
     */
    public void assignDataStreamID(BigId internalID)
    {
        Asserts.checkState(dataStreamID == null, "Datastream ID is already assigned");
        this.dataStreamID = internalID;
    }


    @Override
    public String getSourceID()
    {
        if (sourceID == null)
            sourceID = EventUtils.getDataStreamStatusTopicID(systemUID, outputName);
        return sourceID;
    }
    
}
