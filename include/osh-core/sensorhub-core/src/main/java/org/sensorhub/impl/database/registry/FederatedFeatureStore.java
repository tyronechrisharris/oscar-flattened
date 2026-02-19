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
import org.sensorhub.api.database.IFeatureDatabase;
import org.sensorhub.api.datastore.feature.FeatureFilter;
import org.sensorhub.api.datastore.feature.IFeatureStore;
import org.sensorhub.api.datastore.feature.IFeatureStoreBase.FeatureField;
import org.sensorhub.impl.database.registry.FederatedDatabase.FeatureDbFilterInfo;
import org.vast.ogc.gml.IFeature;


/**
 * <p>
 * Implementation of feature store that provides federated read-only access
 * to several underlying databases.
 * </p>
 *
 * @author Alex Robin
 * @since June 22, 2023
 */
public class FederatedFeatureStore extends FederatedBaseFeatureStore<IFeature, FeatureField, FeatureFilter, IFeatureDatabase> implements IFeatureStore
{
        
    FederatedFeatureStore(FederatedDatabase db)
    {
        super(db);
    }
    
    
    protected Collection<IFeatureDatabase> getAllDatabases()
    {
        return parentDb.getAllFeatureDatabases();
    }
    
    
    protected IFeatureDatabase getDatabase(BigId id)
    {
        return parentDb.getFeatureDatabase(id);
    }
    
    
    protected IFeatureStore getFeatureStore(IFeatureDatabase db)
    {
        return db.getFeatureStore();
    }
    
    
    protected Map<Integer, FeatureDbFilterInfo> getFilterDispatchMap(FeatureFilter filter)
    {
        if (filter.getInternalIDs() != null)
        {
            var filterDispatchMap = parentDb.getFeatureDbFilterDispatchMap(filter.getInternalIDs());
            for (var filterInfo: filterDispatchMap.values())
            {
                filterInfo.filter = FeatureFilter.Builder
                    .from(filter)
                    .withInternalIDs(filterInfo.ids)
                    .build();
            }
            
            return filterDispatchMap;
        }
        
        return null;
    }

}
