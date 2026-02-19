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
import org.sensorhub.api.database.IProcedureDatabase;
import org.sensorhub.api.datastore.procedure.IProcedureStore;
import org.sensorhub.api.datastore.procedure.IProcedureStore.ProcedureField;
import org.sensorhub.api.datastore.procedure.ProcedureFilter;
import org.sensorhub.api.procedure.IProcedureWithDesc;
import org.sensorhub.impl.database.registry.FederatedDatabase.ProcedureDbFilterInfo;


/**
 * <p>
 * Implementation of procedure store that provides federated read-only access
 * to several underlying databases.
 * </p>
 *
 * @author Alex Robin
 * @date Oct 4, 2021
 */
public class FederatedProcedureStore extends FederatedBaseFeatureStore<IProcedureWithDesc, ProcedureField, ProcedureFilter, IProcedureDatabase> implements IProcedureStore
{
    
    FederatedProcedureStore(FederatedDatabase db)
    {
        super(db);
    }
    
    
    protected Collection<IProcedureDatabase> getAllDatabases()
    {
        return parentDb.getAllProcDatabases();
    }
    
    
    protected IProcedureDatabase getDatabase(BigId id)
    {
        return parentDb.getProcedureDatabase(id);
    }
    
    
    protected IProcedureStore getFeatureStore(IProcedureDatabase db)
    {
        return db.getProcedureStore();
    }
    
    
    protected Map<Integer, ProcedureDbFilterInfo> getFilterDispatchMap(ProcedureFilter filter)
    {
        if (filter.getInternalIDs() != null)
        {
            var filterDispatchMap = parentDb.getProcDbFilterDispatchMap(filter.getInternalIDs());
            for (var filterInfo: filterDispatchMap.values())
            {
                filterInfo.filter = ProcedureFilter.Builder
                    .from(filter)
                    .withInternalIDs(filterInfo.ids)
                    .build();
            }
            
            return filterDispatchMap;
        }
        
        return null;
    }

}
