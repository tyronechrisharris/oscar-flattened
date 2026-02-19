/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.view;

import org.sensorhub.api.datastore.deployment.DeploymentFilter;
import org.sensorhub.api.datastore.deployment.IDeploymentStore;
import org.sensorhub.api.datastore.deployment.IDeploymentStore.DeploymentField;
import org.sensorhub.api.datastore.system.ISystemDescStore;
import org.sensorhub.api.system.IDeploymentWithDesc;
import org.vast.util.Asserts;


/**
 * <p>
 * Filtered view implemented as a wrapper of the underlying {@link IDeploymentStore}
 * </p>
 *
 * @author Alex Robin
 * @since June 22, 2023
 */
public class DeploymentStoreView extends FeatureStoreViewBase<IDeploymentWithDesc, DeploymentField, DeploymentFilter, IDeploymentStore> implements IDeploymentStore
{
    
    public DeploymentStoreView(IDeploymentStore delegate, DeploymentFilter viewFilter)
    {
        super(Asserts.checkNotNull(delegate, IDeploymentStore.class), viewFilter);
    }
    

    @Override
    public void linkTo(ISystemDescStore systemStore)
    {
        throw new UnsupportedOperationException();
    }
}