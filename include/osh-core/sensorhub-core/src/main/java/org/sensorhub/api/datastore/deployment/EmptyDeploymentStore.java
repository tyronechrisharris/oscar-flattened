/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2023 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore.deployment;

import org.sensorhub.api.datastore.feature.EmptyFeatureBaseStore;
import org.sensorhub.api.datastore.system.ISystemDescStore;
import org.sensorhub.api.system.IDeploymentWithDesc;
import org.sensorhub.api.datastore.deployment.IDeploymentStore.DeploymentField;


/**
 * <p>
 * Helper class to implement databases that don't support all datastores
 * </p>
**/
public class EmptyDeploymentStore extends EmptyFeatureBaseStore<IDeploymentWithDesc, DeploymentField, DeploymentFilter> implements IDeploymentStore
{

    @Override
    public void linkTo(ISystemDescStore systemStore)
    {
    }

}
