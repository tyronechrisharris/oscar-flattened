/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2023 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.system;

import org.sensorhub.api.feature.ISmlFeature;
import org.vast.ogc.om.IProcedure;
import net.opengis.sensorml.v20.Deployment;


/**
 * <p>
 * Interface for deployment resources associated to a SensorML description.
 * </p><p>
 * An instance of this class can be used to model different kinds of deployments,
 * such as deployments at fixed locations, missions, field operations, survey
 * campaigns, etc.
 * </p>
 * 
 * @author Alex Robin
 * @date Oct 4, 2021
 */
public interface IDeploymentWithDesc extends ISmlFeature<Deployment>, IProcedure
{
    
}
