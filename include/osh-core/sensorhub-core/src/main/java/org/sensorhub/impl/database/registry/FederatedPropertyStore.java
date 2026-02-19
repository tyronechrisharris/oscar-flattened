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

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.sensorhub.api.datastore.property.IPropertyStore;
import org.sensorhub.api.datastore.property.IPropertyStore.PropertyField;
import org.sensorhub.api.datastore.property.PropertyFilter;
import org.sensorhub.api.datastore.property.PropertyKey;
import org.sensorhub.api.semantic.IDerivedProperty;
import org.sensorhub.impl.database.registry.FederatedDatabase.ProcedureDbFilterInfo;
import org.sensorhub.impl.datastore.ReadOnlyDataStore;
import org.vast.util.Asserts;


/**
 * <p>
 * Implementation of procedure store that provides federated read-only access
 * to several underlying databases.
 * </p>
 *
 * @author Alex Robin
 * @date Oct 4, 2021
 */
public class FederatedPropertyStore extends ReadOnlyDataStore<PropertyKey, IDerivedProperty, PropertyField, PropertyFilter> implements IPropertyStore
{
    final FederatedDatabase parentDb;
    
    
    FederatedPropertyStore(FederatedDatabase db)
    {
        this.parentDb = Asserts.checkNotNull(db, FederatedDatabase.class);
    }


    @Override
    public String getDatastoreName()
    {
        return getClass().getSimpleName();
    }


    @Override
    public long getNumRecords()
    {
        long count = 0;
        for (var db: parentDb.getAllProcDatabases())
            count += db.getPropertyStore().getNumRecords();
        return count;
    }
    
    
    protected PropertyKey ensurePropertyKey(Object obj)
    {
        Asserts.checkArgument(obj instanceof PropertyKey, "key must be a PropertyKey");
        return (PropertyKey)obj;
    }


    @Override
    public boolean containsKey(Object obj)
    {
        var key = ensurePropertyKey(obj);
        var id = key.getInternalID();
        
        // delegate to database identified by id scope
        var db = parentDb.getProcedureDatabase(id);
        if (db != null)
            return db.getPropertyStore().containsKey(key);
        else
            return false;
    }


    @Override
    public boolean containsValue(Object value)
    {
        for (var db: parentDb.getAllProcDatabases())
        {
            if (db.getPropertyStore().containsValue(value))
                return true;
        }
        
        return false;
    }


    @Override
    public IDerivedProperty get(Object obj)
    {
        var key = ensurePropertyKey(obj);
        var id = key.getInternalID();
        
        // delegate to database identified by id scope
        var db = parentDb.getProcedureDatabase(id);
        if (db != null)
            return db.getPropertyStore().get(key);
        else
            return null;
    }
    
    
    /*
     * Get dispatch map according to internal IDs used in filter
     */
    protected Map<Integer, ProcedureDbFilterInfo> getFilterDispatchMap(PropertyFilter filter)
    {
        if (filter.getInternalIDs() != null)
        {
            var filterDispatchMap = parentDb.getProcDbFilterDispatchMap(filter.getInternalIDs());
            for (var filterInfo: filterDispatchMap.values())
            {
                filterInfo.filter = PropertyFilter.Builder
                    .from(filter)
                    .withInternalIDs(filterInfo.ids)
                    .build();
            }
            
            return filterDispatchMap;
        }
        
        return null;
    }
    
    
    @Override
    public Stream<Entry<PropertyKey, IDerivedProperty>> selectEntries(PropertyFilter filter, Set<PropertyField> fields)
    {
        // if any kind of internal IDs are used, we need to dispatch the correct filter
        // to the corresponding DB so we create this map
        var filterDispatchMap = getFilterDispatchMap(filter);
        
        if (filterDispatchMap != null)
        {
            return filterDispatchMap.values().stream()
                .flatMap(v -> {
                    return v.db.getPropertyStore().selectEntries((PropertyFilter)v.filter, fields);
                })
                .limit(filter.getLimit());
        }
        
        // otherwise scan all DBs
        else
        {
            return parentDb.getAllProcDatabases().stream()
                .flatMap(db -> {
                    return db.getPropertyStore().selectEntries(filter, fields);
                })
                .limit(filter.getLimit());
        }
    }


    @Override
    public PropertyKey add(IDerivedProperty prop)
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }
}