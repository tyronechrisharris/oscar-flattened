/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.service;

import org.sensorhub.api.config.DisplayInfo;
import org.sensorhub.api.config.DisplayInfo.Required;

/**
 * <p>
 * Base configuration class for HTTP services (i.e. services deployed on the
 * embedded HTTP server) 
 * </p>
 *
 * @author Alex Robin
 * @since Apr 22, 2021
 */
public class HttpServiceConfig extends ServiceConfig
{
    
    @Required
    @DisplayInfo(label="Endpoint", desc="Path of service endpoint relative to the context URL (e.g. http://server.net/sensorhub)")
    public String endPoint;
}
