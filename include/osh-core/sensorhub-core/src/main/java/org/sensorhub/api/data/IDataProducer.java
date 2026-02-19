/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2017 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.data;

import java.util.Map;
import org.sensorhub.api.system.ISystemDriver;
import org.vast.ogc.gml.IFeature;


/**
 * <p>
 * Interface for all producers of streaming data
 * </p>
 *
 * @author Alex Robin
 * @since Mar 23, 2017
 */
public interface IDataProducer extends ISystemDriver
{
    
    /**
     * Retrieves the list of all outputs
     * @return Read-only map of output names to data interface objects
     */
    public Map<String, ? extends IStreamingDataInterface> getOutputs();
    
    
    /**
     * Retrieves the list of all features of interest for which this producer
     * is currently generating data
     * @return Read-only map of FOI unique IDs to feature objects
     */
    public Map<String, ? extends IFeature> getCurrentFeaturesOfInterest();
    
}
