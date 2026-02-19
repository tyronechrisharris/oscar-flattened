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

import java.time.Instant;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Stream;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.DataStoreException;
import org.sensorhub.api.datastore.IdProvider;
import org.sensorhub.api.datastore.feature.FeatureFilterBase;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.api.datastore.feature.IFeatureStoreBase;
import org.sensorhub.api.datastore.feature.IFeatureStoreBase.FeatureField;
import org.sensorhub.impl.datastore.DataStoreUtils;
import org.sensorhub.utils.FilterUtils;
import org.vast.ogc.gml.IFeature;
import org.vast.ogc.gml.JTSUtils;
import org.vast.util.Asserts;
import org.vast.util.Bbox;
import org.locationtech.jts.index.quadtree.Quadtree;


/**
 * <p>
 * In-memory implementation of a feature store backed by a {@link NavigableMap}.
 * This implementation is only used to store the latest system state and thus
 * doesn't support versioning/history of feature descriptions.
 * </p>
 * 
 * @param <T> Feature type
 * @param <VF> Feature field Type
 * @param <F> Filter type
 *
 * @author Alex Robin
 * @date Sep 28, 2019
 */
public abstract class InMemoryBaseFeatureStore<T extends IFeature, VF extends FeatureField, F extends FeatureFilterBase<? super T>> extends InMemoryDataStore implements IFeatureStoreBase<T, VF, F>
{
    final NavigableMap<FeatureKey, T> map = new ConcurrentSkipListMap<>(new InternalIdComparator());
    final NavigableMap<String, FeatureKey> uidMap = new ConcurrentSkipListMap<>();
    final NavigableMap<BigId, Set<BigId>> parentChildMap = new ConcurrentSkipListMap<>(new BigIdComparator());
    final Quadtree spatialIndex = new Quadtree();
    final Bbox allFeaturesBbox = new Bbox();
    final IdProvider<? super T> idProvider;
    final int idScope;
    
    
    /*
     * Use custom comparator so we only keep the latest version of the feature
     * i.e. don't use the time stamp part of the key when indexing
     */
    static class InternalIdComparator implements Comparator<FeatureKey>
    {
        @Override
        public int compare(FeatureKey k1, FeatureKey k2)
        {
            return Long.compare(
                k1.getInternalID().getIdAsLong(),
                k2.getInternalID().getIdAsLong());
        }
    }
    
    
    /*
     * Use BigID conparator that always use the long value to compare.
     * Otherwise we get different results when comparing 2 BigIdLong and
     * comparing a BigIdLong and a BigIdBytes
     */
    static class BigIdComparator implements Comparator<BigId>
    {
        @Override
        public int compare(BigId k1, BigId k2)
        {
            return Long.compare(
                k1.getIdAsLong(),
                k2.getIdAsLong());
        }
    }
    
    
    /*
     * Mutable feature key so we can update valid time of an existing key.
     * This is needed because many in-memory map implementations don't overwrite
     * the key when it is equal to the existing key. 
     */
    static class MutableFeatureKey extends FeatureKey
    {
        MutableFeatureKey(BigId internalID, Instant validStartTime)
        {
            super(internalID, validStartTime);
        }
        
        MutableFeatureKey(FeatureKey key)
        {
            super(key.getInternalID(), key.getValidStartTime());
        }
        
        void updateValidTime(Instant validStartTime)
        {
            this.validStartTime = validStartTime;
        }
    }
    
    
    protected InMemoryBaseFeatureStore(int idScope, IdProvider<? super T> idProvider)
    {
        this.idScope = idScope;
        this.idProvider = Asserts.checkNotNull(idProvider, IdProvider.class);
    }


    @Override
    public synchronized FeatureKey add(BigId parentID, T feature) throws DataStoreException
    {        
        var uid = DataStoreUtils.checkFeatureObject(feature);
        checkParentFeatureExists(parentID);
        
        var existingKey = uidMap.get(uid);
        FeatureKey newKey = generateKey(existingKey, feature);
        if (existingKey != null && existingKey.getValidStartTime().equals(newKey.getValidStartTime()))
            throw new DataStoreException(DataStoreUtils.ERROR_EXISTING_FEATURE_VERSION);
                
        put(newKey, feature);
        
        // also update parent map
        if (existingKey == null)
        {
            // if needed, add a new feature keyset for the specified parent
            parentChildMap.compute(parentID, (id, keys) -> {
                if (keys == null)
                    keys = new TreeSet<>();
                keys.add(newKey.getInternalID());
                return keys;
            });
        }
        
        return newKey;
    }
    
    
    protected void checkParentFeatureExists(BigId parentID) throws DataStoreException
    {
        DataStoreUtils.checkParentFeatureExists(this, parentID);
    }
    
    
    protected FeatureKey generateKey(FeatureKey existingKey, T f)
    {
        BigId internalID = existingKey != null ? 
            existingKey.getInternalID() : BigId.fromLong(idScope, idProvider.newInternalID(f));
        
        // get valid start time from feature object
        // or use default value (meaning always valid) if no valid time is set
        Instant validStartTime;
        if (f.getValidTime() != null)
            validStartTime = f.getValidTime().begin();
        else
            validStartTime = FeatureKey.TIMELESS;
        
        return new MutableFeatureKey(internalID, validStartTime);
    }
    
    
    @Override
    public boolean contains(BigId internalID)
    {
        DataStoreUtils.checkInternalID(internalID);
        return map.containsKey(new FeatureKey(internalID));
    }


    @Override
    public boolean contains(String uid)
    {
        DataStoreUtils.checkUniqueID(uid);
        return uidMap.containsKey(uid);
    }
    
    
    @Override
    public Entry<FeatureKey, T> getCurrentVersionEntry(BigId id)
    {
        var entry = map.ceilingEntry(new FeatureKey(id));
        if (entry == null || entry.getKey().getInternalID().getIdAsLong() != id.getIdAsLong())
            return null;
        return entry;
    }
    
    
    @Override
    public Entry<FeatureKey, T> getCurrentVersionEntry(String uid)
    {
        FeatureKey key = getCurrentVersionKey(uid);
        if (key == null)
            return null;
        return new AbstractMap.SimpleEntry<>(key, map.get(key));
    }
    
    
    @Override
    public FeatureKey getCurrentVersionKey(BigId id)
    {
        var k = map.ceilingKey(new FeatureKey(id));
        if (k == null || k.getInternalID().getIdAsLong() != id.getIdAsLong())
            return null;
        return k;
    }
    
    
    @Override
    public FeatureKey getCurrentVersionKey(String uid)
    {
        DataStoreUtils.checkUniqueID(uid);
        return uidMap.get(uid);
    }
    
    
    @Override
    public BigId getParent(BigId internalID)
    {
        for (var entry: parentChildMap.entrySet())
        {
            if (entry.getValue().contains(internalID))
                return entry.getKey() == BigId.NONE ? null : entry.getKey();
        }
        
        return null;
    }


    @Override
    public long getNumFeatures()
    {
        return uidMap.size();
    }


    @Override
    public Bbox getFeaturesBbox()
    {
        return allFeaturesBbox;
    }


    @Override
    public long getNumRecords()
    {
        return map.size();
    }
    
    
    protected Stream<Entry<FeatureKey, T>> entryStream(Stream<BigId> idStream)
    {
        return idStream.map(id -> {
                FeatureKey key = new FeatureKey(id);
                var entry = map.ceilingEntry(key);
                var fid = entry.getKey().getInternalID();
                return entry != null && fid.getIdAsLong() == id.getIdAsLong() ? entry : null;
            })
            .filter(Objects::nonNull);
    }
    
    
    protected Stream<Entry<FeatureKey, T>> getFeaturesByParent(BigId parentID)
    {
        var children = parentChildMap.get(parentID);
        if (children == null)
            return Stream.empty();
        return entryStream(children.stream());
    }
    
    
    protected Stream<Entry<FeatureKey, T>> postFilterOnParents(Stream<Entry<FeatureKey, T>> resultStream, Stream<? extends BigId> parentIDStream)
    {
        if (resultStream == null)
        {
            return parentIDStream.flatMap(id -> getFeaturesByParent(id));
        }
        else
        {
            // collect child ids from all selected parents
            var allIds = new HashSet<BigId>();
            parentIDStream.forEach(id -> {
                var children = parentChildMap.get(id);
                if (children != null)
                    allIds.addAll(children);
            });
            
            // use existing result stream and post-filter to check
            // if each item is in id set
            return resultStream.filter(e -> {
                return allIds.contains(e.getKey().getInternalID());
            });
        }
    }
    
    
    protected Stream<BigId> getKeyStreamForUniqueIdOrPrefix(String uid)
    {
        // case of wildcard
        if (uid.endsWith(FilterUtils.WILDCARD_CHAR))
        {
            var from = uid.substring(0, uid.length()-1);
            var to = from + '\uffff';
            return uidMap.subMap(from, to).values().stream()
                .map(fk -> fk.getInternalID());
        }
        
        // case of exact UID
        else
        {
            var fk = uidMap.get(uid);
            if (fk == null)
                return Stream.empty(); // return empty stream if uid is not found 
            return Stream.of(fk.getInternalID());
        }
    }
    
    
    protected Stream<Entry<FeatureKey, T>> getIndexedStream(F filter)
    {
        if (filter.getInternalIDs() != null)
        {
            var idStream = filter.getInternalIDs().stream();
            return entryStream(idStream);
        }
        
        else if (filter.getUniqueIDs() != null)
        {
            var idStream = filter.getUniqueIDs().stream()
                .flatMap(this::getKeyStreamForUniqueIdOrPrefix);
            return entryStream(idStream);
        }
        
        else if (filter.getLocationFilter() != null)
        {
            synchronized (spatialIndex)
            {
                var bbox = filter.getLocationFilter().getRoi().getEnvelopeInternal();
                @SuppressWarnings("unchecked")
                var idStream = (Stream<BigId>)spatialIndex.query(bbox).stream()
                    .map(k ->((FeatureKey)k).getInternalID())
                    .distinct();
                return entryStream(idStream);
            }
        }
        
        return null;
    }
    
    
    @Override
    public Stream<Entry<FeatureKey, T>> selectEntries(F filter, Set<VF> fields)
    {
        var resultStream = getIndexedStream(filter);
        
        // if no index used, just scan all features
        if (resultStream == null)
            resultStream = map.entrySet().stream();
        
        // apply post filter
        resultStream = resultStream.filter(e -> filter.test(e.getValue()));
        
        // if including group members
        if (filter.includeMembers())
        {         
            resultStream = resultStream
                .flatMap(e -> {
                    var s1 = Stream.of(e);
                    var s2 = getFeaturesByParent(e.getKey().getInternalID());
                    return Stream.concat(s1, s2);
                });
        }
        
        return resultStream
            .limit(filter.getLimit());
    }


    @Override
    public void clear()
    {
        map.clear();
    }


    @Override
    public boolean containsKey(Object key)
    {
        FeatureKey fk = DataStoreUtils.checkFeatureKey(key);
        return map.containsKey(fk);
    }


    @Override
    public boolean containsValue(Object val)
    {
        return map.containsValue(val);
    }


    public Set<Entry<FeatureKey, T>> entrySet()
    {
        return Collections.unmodifiableSet(map.entrySet());
    }
    

    @Override
    public T get(Object key)
    {
        FeatureKey fk = DataStoreUtils.checkFeatureKey(key);
        return map.get(fk);
    }


    @Override
    public boolean isEmpty()
    {
        return map.isEmpty();
    }


    @Override
    public Set<FeatureKey> keySet()
    {
        return Collections.unmodifiableSet(map.keySet());
    }


    @Override
    public synchronized T put(FeatureKey key, T feature)
    {
        try
        {
            FeatureKey fk = new MutableFeatureKey(DataStoreUtils.checkFeatureKey(key));
            DataStoreUtils.checkFeatureObject(feature);
            
            // check that no other feature with same UID exists
            var uid = feature.getUniqueIdentifier();
            var existingKey = uidMap.get(uid);
            if (existingKey != null && existingKey.getInternalID().getIdAsLong() != key.getInternalID().getIdAsLong())
                throw new DataStoreException(DataStoreUtils.ERROR_EXISTING_FEATURE + uid);
            
            // skip silently if feature currently in store is newer
            // or if new feature has a valid time in the future
            if (existingKey != null)
            {
                if (existingKey.getValidStartTime().isAfter(key.getValidStartTime()) || 
                    key.getValidStartTime().isAfter(Instant.now()))
                return map.get(existingKey);
            }
            
            // otherwise update main map
            // update both key and value atomically
            T old;
            if (existingKey != null)
            {
                old = map.get(fk);
                if (!old.getUniqueIdentifier().equals(feature.getUniqueIdentifier()))
                    throw new DataStoreException(DataStoreUtils.ERROR_CHANGED_FEATURE_UID);
                
                // make sure key valid time is updated
                Instant newValidStartTime = fk.getValidStartTime();
                map.compute(existingKey, (k, v) -> {
                    ((MutableFeatureKey)k).updateValidTime(newValidStartTime);
                    return feature;
                });
                
                // make sure from now on we use the same key object
                // as the one already in the main map
                fk = existingKey;
            }
            else
                old = map.put(fk, feature);
            /*T old = map.remove(fk);
            map.put(fk, feature);*/
            
            // update other indexes
            uidMap.put(feature.getUniqueIdentifier(), fk);
            addToSpatialIndex(fk, feature);
            
            return old;
        }
        catch (DataStoreException e)
        {
            throw new IllegalArgumentException(e);
        }
    }
    
    
    protected void addToSpatialIndex(FeatureKey key, IFeature feature)
    {
        if (feature.getGeometry() != null)
        {
            synchronized(spatialIndex)
            {
                var jtsEnv = JTSUtils.getAsJTSGeometry(feature.getGeometry()).getEnvelopeInternal();
                allFeaturesBbox.resizeToContain(jtsEnv.getMinX(), jtsEnv.getMinY(), 0);
                allFeaturesBbox.resizeToContain(jtsEnv.getMaxX(), jtsEnv.getMaxY(), 0);
                spatialIndex.insert(jtsEnv, key);
            }
        }
    }


    @Override
    public synchronized T remove(Object key)
    {
        FeatureKey fk = DataStoreUtils.checkFeatureKey(key);
        T proc = map.remove(fk);
        if (proc != null)
        {
            uidMap.remove(proc.getUniqueIdentifier());
            
            // also remove parent assoc
            for (var childIDs: parentChildMap.values())
                childIDs.remove(fk.getInternalID());
        }
        return proc;
    }


    @Override
    public int size()
    {
        return map.size();
    }


    @Override
    public Collection<T> values()
    {
        return Collections.unmodifiableCollection(map.values());
    }
}
