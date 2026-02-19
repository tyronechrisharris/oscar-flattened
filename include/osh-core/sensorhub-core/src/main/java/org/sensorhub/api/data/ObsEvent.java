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

import org.sensorhub.api.event.EventUtils;
import org.vast.util.Asserts;


/**
 * <p>
 * Event carrying one or more observations by reference
 * </p>
 *
 * @author Alex Robin
 * @date Mar 10, 2021
 */
public class ObsEvent extends DataStreamEvent
{
    IObsData[] observations;
    
    
    public ObsEvent(long timeStamp, String sysUID, String outputName, IObsData... obs)
    {
        super(timeStamp, sysUID, outputName);
        this.observations = Asserts.checkNotNullOrEmpty(obs, IObsData[].class);
    }


    public IObsData[] getObservations()
    {
        return observations;
    }


    @Override
    public String getSourceID()
    {
        if (sourceID == null)
            sourceID = EventUtils.getDataStreamDataTopicID(systemUID, outputName);
        return sourceID;
    }
    
}
