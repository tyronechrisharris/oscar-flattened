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


/**
 * <p>
 * Event sent when a datastream (i.e. output) is added to a system
 * </p>
 *
 * @author Alex Robin
 * @date Sep 28, 2020
 */
public class DataStreamAddedEvent extends DataStreamEvent
{
    
    /**
     * Pass-through to super class constructor
     * @see DataStreamEvent#DataStreamEvent(long, String, String)
     */
    public DataStreamAddedEvent(long timeStamp, String sysUID, String outputName)
    {
        super(timeStamp, sysUID, outputName);
    }
    
    
    /**
     * Pass-through to super class constructor
     * @see DataStreamEvent#DataStreamEvent(String, String)
     */
    public DataStreamAddedEvent(String sysUID, String outputName)
    {
        super(sysUID, outputName);
    }
    
    
    /**
     * Pass-through to super class constructor
     * @see DataStreamEvent#DataStreamEvent(IDataStreamInfo)
     */
    public DataStreamAddedEvent(IDataStreamInfo dsInfo)
    {
        super(dsInfo);
    }
}
