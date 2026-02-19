/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.database.registry;

import java.util.Collection;
import java.util.Map;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.deployment.DeploymentFilter;
import org.sensorhub.api.datastore.deployment.IDeploymentStore;
import org.sensorhub.api.datastore.deployment.IDeploymentStore.DeploymentField;
import org.sensorhub.api.datastore.system.ISystemDescStore;
import org.sensorhub.api.system.IDeploymentWithDesc;
import org.sensorhub.impl.database.registry.FederatedDatabase.ObsSystemDbFilterInfo;


/**
 * <p>
 * Implementation of deployment store that provides federated read-only access
 * to several underlying databases.
 * </p>
 *
 * @author Alex Robin
 * @since June 22, 2023
 */
public class FederatedDeploymentStore extends FederatedBaseFeatureStore<IDeploymentWithDesc, DeploymentField, DeploymentFilter, IObsSystemDatabase> implements IDeploymentStore
{
    
    FederatedDeploymentStore(FederatedDatabase db)
    {
        super(db);
    }
    
    
    protected Collection<IObsSystemDatabase> getAllDatabases()
    {
        return parentDb.getAllObsDatabases();
    }
    
    
    protected IObsSystemDatabase getDatabase(BigId id)
    {
        return parentDb.getObsSystemDatabase(id);
    }
    
    
    protected IDeploymentStore getFeatureStore(IObsSystemDatabase db)
    {
        return db.getDeploymentStore();
    }
    
    
    protected Map<Integer, ObsSystemDbFilterInfo> getFilterDispatchMap(DeploymentFilter filter)
    {
        if (filter.getInternalIDs() != null)
        {
            var filterDispatchMap = parentDb.getObsDbFilterDispatchMap(filter.getInternalIDs());
            for (var filterInfo: filterDispatchMap.values())
            {
                filterInfo.filter = DeploymentFilter.Builder
                    .from(filter)
                    .withInternalIDs(filterInfo.ids)
                    .build();
            }
            
            return filterDispatchMap;
        }
        
        return null;
    }


    @Override
    public void linkTo(ISystemDescStore systemStore)
    {
        throw new UnsupportedOperationException();
    }

}
