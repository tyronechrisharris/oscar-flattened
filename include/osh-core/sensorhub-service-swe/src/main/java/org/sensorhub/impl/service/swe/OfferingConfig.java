/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2016 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.swe;

import org.sensorhub.api.config.DisplayInfo;


/**
 * <p>
 * Base class for offering configurations (e.g. providers, connectors, etc.)
 * </p>
 *
 * @author Alex Robin
 * @since Dec 20, 2016
 */
public abstract class OfferingConfig
{
    
    //@DisplayInfo(desc="Offering URI as exposed in capabilities. (if null, the procedure UID is used)")
    //public String offeringID;
    
    
    @DisplayInfo(desc="Offering name (if null, the procedure name is used)")
    public String name;
    
    
    @DisplayInfo(desc="Offering description (if null, it will be auto-generated)")
    public String description;
    
    
    @DisplayInfo(desc="Set if offering is enabled, unset if disabled")
    public boolean enabled;    
}
