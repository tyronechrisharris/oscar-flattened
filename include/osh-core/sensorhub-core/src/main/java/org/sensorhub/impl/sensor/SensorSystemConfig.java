/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2016 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.sensor;

import java.util.ArrayList;
import java.util.List;
import org.sensorhub.api.config.DisplayInfo;
import org.sensorhub.api.config.DisplayInfo.Required;
import org.sensorhub.api.module.ModuleConfig;
import org.sensorhub.api.sensor.PositionConfig.CartesianLocation;
import org.sensorhub.api.sensor.PositionConfig.EulerOrientation;
import org.sensorhub.api.sensor.PositionConfig.LLALocation;
import org.sensorhub.api.sensor.SensorConfig;


/**
 * <p>
 * Configuration class for SensorGroup modules
 * </p>
 *
 * @author Alex Robin
 * @since Apr 1, 2016
 */
public class SensorSystemConfig extends SensorConfig
{    
   
    public static class SystemMember
    {
        @DisplayInfo(label="Subsystem Config", desc="Configuration of the subsystem")
        public ModuleConfig config;
        
        @DisplayInfo(label="Relative Location", desc="Location of this subsystem relative to the main system or platform reference frame")
        public CartesianLocation location;
        
        @DisplayInfo(label="Relative Orientation", desc="Orientation of this subsystem relative to the main system or platform reference frame")
        public EulerOrientation orientation;
    }
    
        
    @Required
    @DisplayInfo(desc="Unique ID (full URN or only suffix) to use for the sensor system or 'auto' to use the UUID randomly generated the first time the module is initialized")
    public String uniqueID;
    
    
    @DisplayInfo(label="Fixed Location", desc="Fixed system location in EPSG 4979 (WGS84) coordinate system")
    public LLALocation location;
    
    
    @DisplayInfo(label="Fixed Orientation", desc="Fixed system orientation in the local NED reference frame")
    public EulerOrientation orientation;
    
    
    @DisplayInfo(label="Subsystems", desc="Configuration of components of this sensor system")
    public List<SystemMember> subsystems = new ArrayList<SystemMember>();


    @Override
    public LLALocation getLocation()
    {
        return location;
    }


    @Override
    public EulerOrientation getOrientation()
    {
        return orientation;
    }
}
