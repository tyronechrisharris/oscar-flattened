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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.sensorhub.api.config.DisplayInfo;
import org.sensorhub.impl.service.swe.SWEServiceConfig;


/**
 * <p>
 * Configuration class for the SOS service module
 * </p>
 *
 * @author Alex Robin
 * @since Sep 7, 2013
 */
public class SOSServiceConfig extends SWEServiceConfig
{
    
    @DisplayInfo(desc="Custom provider configurations")
    public LinkedHashSet<SOSProviderConfig> customDataProviders = new LinkedHashSet<>();


    @DisplayInfo(desc="Mapping of custom formats mime-types to custom serializer classes")
    public List<SOSCustomFormatConfig> customFormats = new ArrayList<>();

    @DisplayInfo(label="Max Observations Returned", desc="Maximum number of observations returned "
        + " by a historical GetObservation request (for each selected offering)")
    public int maxObsCount = 100;


    @DisplayInfo(label="Max Records Returned", desc="Maximum number of result records returned by a historical GetResult request")
    public int maxRecordCount = 100000;


    @DisplayInfo(desc="Maximum number of FoI IDs listed in capabilities")
    public int maxFois = 10;
    
    
    @DisplayInfo(desc="Default live time-out for all offerings, unless overriden by custom provider settings")
    public double defaultLiveTimeout = 10.0;


    @DisplayInfo(desc="Time-out period after which a template ID reserved using InsertResultTemplate "
        + "will expire if not used in InsertResult requests (in seconds)")
    public int templateTimeout = 600;


    public SOSServiceConfig()
    {
        this.moduleClass = SOSService.class.getCanonicalName();
        this.endPoint = "/sos";
    }
}
