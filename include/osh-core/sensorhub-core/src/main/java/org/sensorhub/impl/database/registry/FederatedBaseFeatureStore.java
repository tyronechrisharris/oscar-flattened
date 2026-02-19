/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.database.registry;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.database.IDatabase;
import org.sensorhub.api.datastore.feature.FeatureFilterBase;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.api.datastore.feature.IFeatureStoreBase;
import org.sensorhub.api.datastore.feature.IFeatureStoreBase.FeatureField;
import org.sensorhub.impl.database.registry.FederatedDatabase.LocalFilterInfo;
import org.sensorhub.impl.datastore.DataStoreUtils;
import org.sensorhub.impl.datastore.ReadOnlyDataStore;
import org.vast.ogc.gml.IFeature;
import org.vast.util.Asserts;
import org.vast.util.Bbox;


/**
 * <p>
 * Base federated feature store used as the base for all federated feature
 * stores (features, fois, systems, etc.)
 * </p>
 * 
 * @param <T> Feature type
 * @param <VF> Feature field type
 * @param <F> Filter type
 * @param <DB> Parent database type
 *
 * @author Alex Robin
 * @date Dec 3, 2020
 */
public abstract class FederatedBaseFeatureStore<T extends IFeature, VF extends FeatureField, F extends FeatureFilterBase<? super T>, DB extends IDatabase> extends ReadOnlyDataStore<FeatureKey, T, VF, F> implements IFeatureStoreBase<T, VF, F>
{
    final FederatedDatabase parentDb;
    
    
    FederatedBaseFeatureStore(FederatedDatabase db)
    {
        this.parentDb = Asserts.checkNotNull(db, FederatedDatabase.class);
    }
    
    
    protected abstract Collection<DB> getAllDatabases();
    
    
    protected abstract DB getDatabase(BigId id);
    
    
    protected abstract IFeatureStoreBase<T, VF, F> getFeatureStore(DB db);
    
    
    protected abstract Map<Integer, ? extends LocalFilterInfo<DB>> getFilterDispatchMap(F filter);
    


    @Override
    public String getDatastoreName()
    {
        return getClass().getSimpleName();
    }
    
    
    @Override
    public long getNumFeatures()
    {
        long count = 0;
        for (var db: getAllDatabases())
            count += getFeatureStore(db).getNumFeatures();
        return count;
    }


    @Override
    public long getNumRecords()
    {
        long count = 0;
        for (var db: getAllDatabases())
            count += getFeatureStore(db).getNumRecords();
        return count;
    }


    @Override
    public Bbox getFeaturesBbox()
    {
        Bbox bbox = new Bbox();
        for (var db: getAllDatabases())
            bbox.add(getFeatureStore(db).getFeaturesBbox());
        return bbox;
    }
    
    
    @Override
    public boolean contains(BigId id)
    {
        DataStoreUtils.checkInternalID(id);
        
        // delegate to database identified by id scope
        var db = getDatabase(id);
        if (db != null)
            return getFeatureStore(db).contains(id);
        else
            return false;
    }


    @Override
    public boolean contains(String uid)
    {
        DataStoreUtils.checkUniqueID(uid);
        
        for (var db: getAllDatabases())
        {
            if (getFeatureStore(db).contains(uid))
                return true;
        }
        
        return false;
    }


    @Override
    public boolean containsKey(Object obj)
    {
        FeatureKey key = DataStoreUtils.checkFeatureKey(obj);
        
        // delegate to database identified by id scope
        var db = getDatabase(key.getInternalID());
        if (db != null)
            return getFeatureStore(db).containsKey(key);
        else
            return false;
    }


    @Override
    public boolean containsValue(Object value)
    {
        for (var db: getAllDatabases())
        {
            if (getFeatureStore(db).containsValue(value))
                return true;
        }
        
        return false;
    }
    
    
    @Override
    public BigId getParent(BigId id)
    {
        // use public key to lookup database and local key
        var db = getDatabase(id);
        if (db != null)
            return getFeatureStore(db).getParent(id);
        else
            return null;
    }


    @Override
    public T get(Object obj)
    {
        FeatureKey key = DataStoreUtils.checkFeatureKey(obj);
        
        // delegate to database identified by id scope
        var db = getDatabase(key.getInternalID());
        if (db != null)
            return getFeatureStore(db).get(key);
        else
            return null;
    }


    @Override
    public Entry<FeatureKey, T> getCurrentVersionEntry(BigId internalID)
    {
        // delegate to database identified by id scope
        var db = getDatabase(internalID);
        if (db != null)
            return getFeatureStore(db).getCurrentVersionEntry(internalID);
        else
            return null;
    }


    @Override
    @SuppressWarnings("unchecked")
    public Stream<Entry<FeatureKey, T>> selectEntries(F filter, Set<VF> fields)
    {
        // if any kind of internal IDs are used, we need to dispatch the correct filter
        // to the corresponding DB so we create this map
        var filterDispatchMap = getFilterDispatchMap(filter);
        
        if (filterDispatchMap != null)
        {
            return filterDispatchMap.values().stream()
                .flatMap(v -> getFeatureStore(v.db).selectEntries((F)v.filter, fields))
                .limit(filter.getLimit());
        }
        
        // otherwise scan all DBs
        else
        {
            return getAllDatabases().stream()
                .flatMap(db -> getFeatureStore(db).selectEntries(filter, fields))
                .limit(filter.getLimit());
        }
    }
    
    
    @Override
    @SuppressWarnings("unchecked")
    public long countMatchingEntries(F filter)
    {
        // if any kind of internal IDs are used, we need to dispatch the correct filter
        // to the corresponding DB so we create this map
        var filterDispatchMap = getFilterDispatchMap(filter);
        
        if (filterDispatchMap != null)
        {
            return filterDispatchMap.values().stream()
                .mapToLong(v -> getFeatureStore(v.db).countMatchingEntries((F)v.filter))
                .reduce(0, Long::sum);
        }
        
        // otherwise scan all DBs
        else
        {
            return getAllDatabases().stream()
                .mapToLong(db -> getFeatureStore(db).countMatchingEntries(filter))
                .reduce(0, Long::sum);
        }
    }
    
    
    @Override
    public FeatureKey add(T feature)
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }


    @Override
    public FeatureKey add(BigId parentId, T value)
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }

}
