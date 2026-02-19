/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sos;

import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.config.DisplayInfo;
import org.sensorhub.api.config.DisplayInfo.FieldType;
import org.sensorhub.api.config.DisplayInfo.FieldType.Type;
import org.sensorhub.impl.service.swe.OfferingConfig;
import org.vast.ows.OWSRequest;


public abstract class SOSProviderConfig extends OfferingConfig
{
    
    @DisplayInfo(desc="Unique ID of a system that this configuration applies to.\n"
        + "Can include a trailing wildcard '*' to match several systems at once.")
    @FieldType(Type.SYSTEM_UID)
    public String systemUID;
    
    
    @DisplayInfo(desc="Maximum number of FoI IDs listed in capabilities")
    public int maxFois = 10;
    
    
    public SOSProviderConfig()
    {
        this.enabled = true;
    }
    
    
    /**
     * Creates a new data provider for handling an SOS request
     * @param service Parent SOS service
     * @param request SOS request
     * @return The data provider instance corresponding to this config class
     * @throws SensorHubException 
     */
    public abstract ISOSAsyncDataProvider createProvider(SOSService service, OWSRequest request) throws SensorHubException;
}