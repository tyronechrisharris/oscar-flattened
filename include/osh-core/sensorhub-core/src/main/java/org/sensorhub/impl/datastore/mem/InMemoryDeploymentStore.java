/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.mem;

import java.util.stream.Stream;
import org.sensorhub.api.datastore.IdProvider;
import org.sensorhub.api.datastore.deployment.DeploymentFilter;
import org.sensorhub.api.datastore.deployment.IDeploymentStore;
import org.sensorhub.api.datastore.deployment.IDeploymentStore.DeploymentField;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.api.datastore.system.ISystemDescStore;
import org.sensorhub.api.system.IDeploymentWithDesc;
import org.sensorhub.impl.datastore.DataStoreUtils;
import org.vast.util.Asserts;


/**
 * <p>
 * In-memory implementation of procedure store backed by a {@link java.util.NavigableMap}.
 * This implementation only maintains the latest version of the procedure
 * description and thus does not support procedure description history.
 * </p>
 *
 * @author Alex Robin
 * @date Mar 30, 2022
 */
public class InMemoryDeploymentStore extends InMemoryBaseFeatureStore<IDeploymentWithDesc, DeploymentField, DeploymentFilter> implements IDeploymentStore
{
    ISystemDescStore systemStore;
    
    
    public InMemoryDeploymentStore(int idScope)
    {
        super(idScope, DataStoreUtils.getFeatureHashIdProvider(2051431686));
    }
    
    
    public InMemoryDeploymentStore(int idScope, IdProvider<? super IDeploymentWithDesc> idProvider)
    {
        super(idScope, idProvider);
    }
    
    
    @Override
    protected Stream<Entry<FeatureKey, IDeploymentWithDesc>> getIndexedStream(DeploymentFilter filter)
    {
        var resultStream = super.getIndexedStream(filter);
        
        if (filter.getSystemFilter() != null)
        {
            // TODO implement deployment->system join
        }
        
        return resultStream;
    }


    @Override
    public void linkTo(ISystemDescStore systemStore)
    {
        this.systemStore = Asserts.checkNotNull(systemStore, ISystemDescStore.class);
    }
}
