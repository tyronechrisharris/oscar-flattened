/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.system;

import org.sensorhub.api.config.DisplayInfo;
import org.sensorhub.api.config.DisplayInfo.Required;


/**
 * <p>
 * Class used in config to define virtual system groups.
 * </p>
 *
 * @author Alex Robin
 * @date Sep 26, 2019
 */
public class VirtualSystemGroupConfig
{
    @Required
    @DisplayInfo(desc="Unique ID of system group")
    public String uid;
    
    @DisplayInfo(desc="Name of system group")
    public String name;
    
    @DisplayInfo(desc="Description of system group")
    public String description;
    
    // security role to map to this group
    //public String securityRole;
}
