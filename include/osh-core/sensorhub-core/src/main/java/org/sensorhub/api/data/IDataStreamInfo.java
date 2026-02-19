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

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.sensorhub.api.feature.FeatureId;
import org.vast.util.IResource;
import org.vast.util.TimeExtent;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;


/**
 * <p>
 * Interface for DataStream descriptors
 * </p>
 *
 * @author Alex Robin
 * @date Mar 23, 2020
 */
public interface IDataStreamInfo extends IResource
{

    /**
     * @return The identifier of the system that produces this data stream
     */
    FeatureId getSystemID();


    /**
     * @return The name of the system output that is/was the source of
     * this data stream
     */
    String getOutputName();
    

    /**
     * @return The data stream record structure
     */
    DataComponent getRecordStructure();


    /**
     * @return The recommended encoding for the data stream
     */
    DataEncoding getRecordEncoding();
    
    
    /**
     * @return The time of validity of this datastream. This corresponds to the time
     * during which the corresponding system output actually existed.
     */
    TimeExtent getValidTime();
    
    
    /**
     * @return The range of phenomenon times of observations that are part
     * of this datastream, or null if no observations have been recorded yet.
     */
    TimeExtent getPhenomenonTimeRange();
    
    
    /**
     * @return The average interval between two observation phenomenon times,
     * or null if unknown or highly variable.
     */
    Duration getPhenomenonTimeInterval();
    
    
    /**
     * @return The range of result times of observations that are part
     * of this datastream, or null if no observations have been recorded yet.
     */
    TimeExtent getResultTimeRange();
    
    
    /**
     * @return The average interval between two observation result times,
     * or null if unknown or highly variable.
     */
    Duration getResultTimeInterval();
    
    
    /**
     * @return True if this datastream contains observations acquired for a discrete
     * number of result times (e.g. model runs, test campaigns, etc.) 
     */
    boolean hasDiscreteResultTimes();
    
    
    /**
     * @return A map of discrete result times to the phenomenon time range of all
     * observations whose result was produced at each result time, or an empty map if
     * {@link #hasDiscreteResultTimes()} returns true.
     */
    Map<Instant, TimeExtent> getDiscreteResultTimes();
    
    
    /**
     * @return A reference to the ultimate feature of interest associated to this datastream.
     * (only provided if all observations in the datastream share the same feature of interest)
     */
    FeatureId getFeatureOfInterestID();
    
    
    /**
     * @return A reference to the sampling feature associated to this datastream.
     * (only provided if all observations in the datastream share the same sampling feature)
     */
    FeatureId getSamplingFeatureID();
    
    
    /**
     * @return A reference to the procedure associated to this datastream.
     * (only provided if all observations in the datastream share the same procedure)
     */
    FeatureId getProcedureID();
    
    
    /**
     * @return A reference to the deployment associated to this datastream.
     * (only provided if all observations in the datastream were produced during the
     * same deployment)
     */
    FeatureId getDeploymentID();
    
    
    /**
     * @return The full name of the datastream combining the system UID and the output name
     */
    default String getFullName()
    {
        return getSystemID().getUniqueID() + "#" + getOutputName();
    }

}
