/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.deployment;

import org.sensorhub.api.datastore.deployment.IDeploymentStore;
import org.sensorhub.api.datastore.deployment.IDeploymentStore.DeploymentField;
import org.sensorhub.api.datastore.system.ISystemDescStore;
import org.sensorhub.api.datastore.deployment.DeploymentFilter;
import org.sensorhub.api.system.IDeploymentWithDesc;
import org.sensorhub.impl.service.consys.feature.AbstractFeatureStoreWrapper;


public class DeploymentStoreWrapper extends AbstractFeatureStoreWrapper<IDeploymentWithDesc, DeploymentField, DeploymentFilter, IDeploymentStore> implements IDeploymentStore
{
    
    public DeploymentStoreWrapper(IDeploymentStore readStore, IDeploymentStore writeStore)
    {
        super(readStore, writeStore);
    }
    
    
    @Override
    public DeploymentFilter.Builder filterBuilder()
    {
        return (DeploymentFilter.Builder)super.filterBuilder();
    }


    @Override
    public void linkTo(ISystemDescStore systemStore)
    {
        throw new UnsupportedOperationException();
    }

}
