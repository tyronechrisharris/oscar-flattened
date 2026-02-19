/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.swe;

import org.sensorhub.api.config.DisplayInfo;
import org.sensorhub.api.security.SecurityConfig;
import org.sensorhub.impl.datastore.view.ObsSystemDatabaseViewConfig;
import org.sensorhub.impl.service.ogc.OGCServiceConfig;
import org.sensorhub.impl.system.VirtualSystemGroupConfig;


/**
 * <p>
 * Base config class for both SOS and SPS
 * </p>
 *
 * @author Alex Robin
 * @since Mar 31, 2021
 */
public class SWEServiceConfig extends OGCServiceConfig
{
    @DisplayInfo(desc="Metadata of system group that will be created to contain all procedures/sensors "
        + "registered through this service. Only sensors in this group will be modifiable by this service")
    public VirtualSystemGroupConfig virtualSensorGroup = null;
    
    
    @DisplayInfo(label="Database ID", desc="ID of database module used for persisting data received by this service. "
        + "If none is provided, new systems registered through this service will be available on the hub, but "
        + "with no persistence guarantee across restarts.")
    public String databaseID = null;


    @DisplayInfo(desc="Filtered view to select systems exposed as read-only through this service")
    public ObsSystemDatabaseViewConfig exposedResources = null;


    @DisplayInfo(desc="Security related options")
    public SecurityConfig security = new SecurityConfig();


    @DisplayInfo(desc="Set to true to enable transactional operation support")
    public boolean enableTransactional = false;

}
