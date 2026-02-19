/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUObsData WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.

******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.mem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Stream;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.DataStoreException;
import org.sensorhub.api.datastore.resource.IResourceStore;
import org.sensorhub.api.datastore.resource.IResourceStore.ResourceField;
import org.sensorhub.api.resource.ResourceFilter;
import org.sensorhub.impl.datastore.DataStoreUtils;
import org.vast.util.IResource;


/**
 * <p>
 * In-memory implementation of a resource store backed by a {@link NavigableMap}.
 * </p>
 * @param <K> Key type
 * @param <V> Resource type 
 * @param <VF> Resource value field enum type
 * @param <F> Resource filter type
 *
 * @author Alex Robin
 * @since June 22, 2023
 */
public abstract class InMemoryResourceStore<K extends Comparable<? super K>, V extends IResource, VF extends ResourceField, F extends ResourceFilter<? super V>>
    implements IResourceStore<K, V, VF, F>
{
    final protected int idScope;
    final protected NavigableMap<K, V> map = new ConcurrentSkipListMap<>();


    public InMemoryResourceStore(int idScope)
    {
        this.idScope = idScope;
    }
    
    
    protected abstract K generateKey(V res);
    
    
    protected abstract K getKey(BigId id);
    
    
    protected abstract K checkKey(Object key);
    
    
    protected abstract V checkValue(V res) throws DataStoreException;
    
    
    @Override
    public synchronized K add(V res) throws DataStoreException
    {
        // check value is valid
        res = checkValue(res);
        
        // create key
        var newKey = generateKey(res);

        // add to store
        put(newKey, res, false);
        return newKey;
    }


    @Override
    public V get(Object key)
    {
        var k = checkKey(key);
        return map.get(k);
    }


    @Override
    public Stream<Entry<K, V>> selectEntries(F filter, Set<VF> fields)
    {
        Stream<K> keyStream = null;
        Stream<Entry<K, V>> resultStream;

        if (filter.getInternalIDs() != null)
        {
            keyStream = filter.getInternalIDs().stream()
                .map(id -> getKey(id));
        }
        
        if (keyStream != null)
        {
            resultStream = keyStream.map(key -> {
                var dsInfo = map.get(key);
                if (dsInfo == null)
                    return null;
                return (Entry<K, V>)new AbstractMap.SimpleEntry<>(key, dsInfo);
            })
            .filter(Objects::nonNull);
        }
        else
        {
            // stream all entries
            resultStream = map.entrySet().stream();
        }
        
        // filter with predicate and apply limit
        return resultStream
            .filter(e -> filter.test(e.getValue()))
            .limit(filter.getLimit());
    }


    @Override
    public V put(K key, V res)
    {
        checkKey(key);
        
        try
        {
            res = checkValue(res);
            return put(key, res, true);
        }
        catch (DataStoreException e)
        {
            throw new IllegalArgumentException(e);
        }
    }


    protected V put(K key, V res, boolean replace) throws DataStoreException
    {
        if (!replace)
        {
            V newVal = map.putIfAbsent(key, res);
            if (newVal == res)
                throw new DataStoreException(DataStoreUtils.ERROR_EXISTING_RESOURCE);
            return newVal;
        }
        else
            return map.put(key, res);
    }


    @Override
    public V remove(Object key)
    {
        return map.remove(key);
    }


    @Override
    public long getNumRecords()
    {
        return map.size();
    }


    @Override
    public void clear()
    {
        map.clear();
    }


    @Override
    public boolean containsKey(Object key)
    {
        var dsKey = DataStoreUtils.checkDataStreamKey(key);
        return map.containsKey(dsKey);
    }


    @Override
    public boolean containsValue(Object val)
    {
        return map.containsValue(val);
    }


    @Override
    public Set<Entry<K, V>> entrySet()
    {
        return map.entrySet();
    }


    @Override
    public boolean isEmpty()
    {
        return map.isEmpty();
    }


    @Override
    public Set<K> keySet()
    {
        return Collections.unmodifiableSet(map.keySet());
    }


    @Override
    public int size()
    {
        return map.size();
    }


    @Override
    public Collection<V> values()
    {
        return Collections.unmodifiableCollection(map.values());
    }


    @Override
    public String getDatastoreName()
    {
        return getClass().getSimpleName();
    }


    @Override
    public void commit()
    {
    }


    @Override
    public void backup(OutputStream is) throws IOException
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public void restore(InputStream os) throws IOException
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean isReadOnly()
    {
        return false;
    }
}
