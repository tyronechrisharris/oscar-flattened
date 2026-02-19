/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import org.sensorhub.api.datastore.DataStoreException;
import org.sensorhub.api.datastore.IDataStore;
import org.sensorhub.api.datastore.IQueryFilter;
import org.sensorhub.api.datastore.ValueField;
import org.vast.util.Asserts;


/**
 * <p>
 * Wrapper to allow reading from federated data store or filtered view while
 * writing to a dedicated store. Conversions between internal and public IDs
 * are handled automatically.
 * </p>
 * @param <K> Key type
 * @param <V> Value type  
 * @param <VF> Value field type
 * @param <Q> Query type
 * @param <S> Delegate datastore type
 *
 * @author Alex Robin
 * @since Oct 12, 2020
 */
public abstract class AbstractDataStoreWrapper<
    K, V, VF extends ValueField, Q extends IQueryFilter,
    S extends IDataStore<K, V, VF, Q>> implements IDataStore<K, V, VF, Q>
{
    String name;
    S readStore;
    S writeStore;
    
    
    protected AbstractDataStoreWrapper(S readStore, S writeStore)
    {
        Asserts.checkArgument(readStore != null || writeStore != null);
        this.readStore = readStore;
        this.writeStore = writeStore;
    }
    
    
    protected S getReadStore()
    {
        Asserts.checkState(readStore != null, "Datastore is not readable");
        return readStore;
    }
    
    
    protected S getWriteStore()
    {
        Asserts.checkState(writeStore != null, "Datastore is not writable");
        return writeStore;
    }
    

    public String getDatastoreName()
    {
        return getReadStore().getDatastoreName();
    }


    public long getNumRecords()
    {
        return getReadStore().getNumRecords();
    }


    public Stream<Entry<K, V>> selectEntries(Q query)
    {
        return getReadStore().selectEntries(query);
    }


    public Stream<Entry<K, V>> selectEntries(Q query, Set<VF> fields)
    {
        return getReadStore().selectEntries(query, fields);
    }


    @SuppressWarnings("unchecked")
    public Stream<Entry<K, V>> selectEntries(Q query, VF... fields)
    {
        return getReadStore().selectEntries(query, fields);
    }


    public Stream<V> select(Q query)
    {
        return getReadStore().select(query);
    }


    public Stream<V> select(Q query, Set<VF> fields)
    {
        return getReadStore().select(query, fields);
    }


    @SuppressWarnings("unchecked")
    public Stream<V> select(Q query, VF... fields)
    {
        return getReadStore().select(query, fields);
    }


    public Stream<K> selectKeys(Q query)
    {
        return getReadStore().selectKeys(query);
    }


    public long countMatchingEntries(Q query)
    {
        return getReadStore().countMatchingEntries(query);
    }


    public Q selectAllFilter()
    {
        return getReadStore().selectAllFilter();
    }


    public int size()
    {
        return getReadStore().size();
    }


    public boolean isEmpty()
    {
        return getReadStore().isEmpty();
    }


    public boolean containsKey(Object key)
    {
        return getReadStore().containsKey(key);
    }


    public boolean containsValue(Object value)
    {
        return getReadStore().containsValue(value);
    }


    public Set<Entry<K, V>> entrySet()
    {
        return getReadStore().entrySet();
    }


    public Set<K> keySet()
    {
        return getReadStore().keySet();
    }


    public Collection<V> values()
    {
        return getReadStore().values();
    }


    public V get(Object key)
    {
        return getReadStore().get(key);
    }


    public V getOrDefault(Object key, V defaultValue)
    {
        return getReadStore().getOrDefault(key, defaultValue);
    }


    public void forEach(BiConsumer<? super K, ? super V> action)
    {
        getReadStore().forEach(action);
    }


    public V put(K key, V value)
    {
        return getWriteStore().put(key, value);
    }


    public void putAll(Map<? extends K, ? extends V> map)
    {
        getWriteStore().putAll(map);
    }


    public V remove(Object key)
    {
        return getWriteStore().remove(key);
    }


    public long removeEntries(Q query)
    {
        return getWriteStore().removeEntries(query);
    }


    public void clear()
    {
        getWriteStore().clear();
    }


    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function)
    {
        getWriteStore().replaceAll(function);
    }


    public V putIfAbsent(K key, V value)
    {
        return getWriteStore().putIfAbsent(key, value);
    }


    public boolean remove(Object key, Object value)
    {
        return getWriteStore().remove(key, value);
    }


    public boolean replace(K key, V oldValue, V newValue)
    {
        return getWriteStore().replace(key, oldValue, newValue);
    }


    public V replace(K key, V value)
    {
        return getWriteStore().replace(key, value);
    }


    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
    {
        return getWriteStore().computeIfAbsent(key, mappingFunction);
    }


    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    {
        return getWriteStore().computeIfPresent(key, remappingFunction);
    }


    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    {
        return getWriteStore().compute(key, remappingFunction);
    }


    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction)
    {
        return getWriteStore().merge(key, value, remappingFunction);
    }


    public void commit() throws DataStoreException
    {
        getWriteStore().commit();
    }


    public void backup(OutputStream is) throws IOException
    {
        throw new UnsupportedOperationException();
    }


    public void restore(InputStream os) throws IOException
    {
        throw new UnsupportedOperationException();
    }


    public boolean isReadOnly()
    {
        return writeStore == null || getWriteStore().isReadOnly();
    }
}
