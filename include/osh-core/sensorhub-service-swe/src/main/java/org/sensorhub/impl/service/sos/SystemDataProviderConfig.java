/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sos;

import java.util.ArrayList;
import java.util.List;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.config.DisplayInfo;
import org.vast.ows.OWSRequest;
import org.vast.ows.sos.GetObservationRequest;
import org.vast.ows.sos.GetResultRequest;


/**
 * <p>
 * Configuration class for SOS data providers fetching data from persistent
 * stores using the database API.
 * </p>
 *
 * @author Alex Robin
 * @since Apr 15, 2020
 */
public class SystemDataProviderConfig extends SOSProviderConfig
{
    
    @DisplayInfo(desc="Time-out after which real-time requests are disabled if no more "
        + "measurements are received (in seconds). Real-time is reactivated as soon as "
        + "new records start being received again")
    public double liveDataTimeout = 10.;
    
    
    @DisplayInfo(desc="Names of datastreams whose data will be hidden from the SOS. " +
            "If this is null, all streams produced by the procedure are exposed")
    public List<String> excludedOutputs = new ArrayList<>();
    

    public SystemDataProviderConfig()
    {
    }
    
    
    @Override
    public ISOSAsyncDataProvider createProvider(SOSService service, OWSRequest request) throws SensorHubException
    {
        if (request instanceof GetResultRequest)
        {
            if (SOSProviderUtils.isFutureTimePeriod(((GetResultRequest)request).getTime()))
                return new StreamingDataProvider(service, this);
        }
        
        else if (request instanceof GetObservationRequest)
        {
            if (SOSProviderUtils.isFutureTimePeriod(((GetObservationRequest)request).getTime()))
                return new StreamingDataProvider(service, this);
        }
            
        return new HistoricalDataProvider(service, this);
    }
}
