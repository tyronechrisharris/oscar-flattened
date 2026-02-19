/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.vast.util.Asserts;
import com.google.common.collect.Sets;


/**
 * <p>
 * Base interface for all object data stores. This is an extension of the
 * {@link Map} interface that adds support for:
 * <li>Selecting, removing and counting elements matching a filter query</li>
 * <li>Backup/restore operations</li>
 * </p><p>
 * Many operations return {@link Stream} objects allowing additional
 * filtering, projecting and sorting using Java Stream API methods.
 * </p><p>
 * <i>Note that certain data store implementations may optimize execution
 * by partitioning, parallelizing or distributing the Stream pipeline
 * operations.</i>
 * </p>
 * @param <K> Key type
 * @param <V> Value type  
 * @param <VF> Value field type
 * @param <Q> Query type
 **/
public interface IDataStore<K, V, VF extends ValueField, Q extends IQueryFilter> extends Map<K, V>
{        
    
    /**
     * @return Data store name
     */
    public String getDatastoreName();


    /**
     * @return Total number of records contained in this data store
     */
    public default long getNumRecords()
    {
        return countMatchingEntries(selectAllFilter());
    }


    /**
     * Select all entries matching the query and return full entries
     * @param query selection filter (datastore specific)
     * @return Stream of matching entries (i.e. key/value pairs)
     */
    public default Stream<Entry<K, V>> selectEntries(Q query)
    {
        return selectEntries(query, (Set<VF>)null);
    }
    
    
    /**
     * Select all entries matching the query and include selected fields only
     * @param query selection filter (datastore specific)
     * @param fields List of value fields to read from datastore (or null to select all fields)
     * @return Stream of value objects. Caller should not try to access fields of 
     * value objects that were not included in the {@code fields} parameter.
     */
    public Stream<Entry<K, V>> selectEntries(Q query, Set<VF> fields);
    
    
    /**
     * @see {@link #select(IQueryFilter, Set)}
     */
    @SuppressWarnings({ "unchecked", "javadoc" })
    public default Stream<Entry<K, V>> selectEntries(Q query, VF... fields)
    {
        return selectEntries(query, Sets.newHashSet(fields));
    }


    /**
     * Select all values matching the query
     * @param query selection filter (datastore specific)
     * @return Stream of value objects
     */
    public default Stream<V> select(Q query)
    {
        return select(query, (Set<VF>)null);
    }
    
    
    /**
     * Select all values matching the query and include selected fields only
     * @param query selection filter (datastore specific)
     * @param fields List of value fields to read from datastore
     * @return Stream of value objects. Values returned by get methods
     * corresponding to omitted fields may be invalid.
     */
    public default Stream<V> select(Q query, Set<VF> fields)
    {
        return selectEntries(query, fields).map(e -> e.getValue());
    }
    
    
    /**
     * @see {@link #select(IQueryFilter, Set)}
     */
    @SuppressWarnings({ "unchecked", "javadoc" })
    public default Stream<V> select(Q query, VF... fields)
    {
        return select(query, Sets.newHashSet(fields));
    }


    /**
     * Select all entries matching the query and return keys only
     * @param query selection filter (datastore specific)
     * @return Stream of key objects
     */
    public default Stream<K> selectKeys(Q query)
    {
        return selectEntries(query).map(e -> e.getKey());
    }


    /**
     * Batch remove all entries matching the query
     * @param query selection filter (datastore specific)
     * @return Number of deleted entries
     */
    public default long removeEntries(Q query)
    {
        return selectKeys(query)
            .filter(k -> remove(k) != null)
            .count();
    }


    /**
     * Count all entries matching the query
     * @param query selection filter (datastore specific)
     * @return number of matching entries, or the value of query limit if
     * reached
     */
    public default long countMatchingEntries(Q query)
    {
        return selectEntries(query).count();
    }


    /**
     * Commit changes to storage
     * @throws DataStoreException if an error occurred while committing changes
     */
    public void commit() throws DataStoreException;


    /**
     * Backup datastore content to the specified output stream
     * @param is target output stream
     * @throws IOException if backup failed
     */
    public void backup(OutputStream is) throws IOException;


    /**
     * Restore datastore content from the specified input stream
     * @param os source input stream
     * @throws IOException if restoration failed
     */
    public void restore(InputStream os) throws IOException;


    /**
     * @return true if the datastore is read-only, false otherwise
     */
    public default boolean isReadOnly()
    {
        return false;
    }
    
    
    /**
     * @return The filter to use to select all records
     */
    public Q selectAllFilter();
    
    
    /*
     * Default implementation of some map methods
     */
    
    @Override
    public default int size()
    {
        return (int)getNumRecords();
    }
    
    
    @Override
    public default boolean isEmpty()
    {
        return getNumRecords() == 0;
    }
    
    
    @Override
    public default boolean containsKey(Object key)
    {
        return get(key) != null;
    }
    
    
    @Override
    public default boolean containsValue(Object value)
    {
        throw new UnsupportedOperationException();
    }
    
    
    @Override
    public default Set<Entry<K, V>> entrySet()
    {
        return new AbstractSet<>()
        {
            @Override
            public Iterator<Entry<K, V>> iterator()
            {
                return selectEntries(selectAllFilter()).iterator();
            }

            @Override
            public int size()
            {
                return IDataStore.this.size();
            }        
        };
    }


    @Override
    public default Set<K> keySet()
    {
        return new AbstractSet<>()
        {
            @Override
            public Iterator<K> iterator()
            {
                return selectKeys(selectAllFilter()).iterator();
            }

            @Override
            public int size()
            {
                return IDataStore.this.size();
            }        
        };
    }


    @Override
    public default Collection<V> values()
    {
        return new AbstractCollection<>()
        {
            @Override
            public Iterator<V> iterator()
            {
                return select(selectAllFilter()).iterator();
            }

            @Override
            public int size()
            {
                return IDataStore.this.size();
            }        
        };
    }
    
    
    @Override
    public default void putAll(Map<? extends K, ? extends V> map)
    {
        Asserts.checkNotNull(map, Map.class);
        map.forEach((k, v) -> put(k, v));
    }

}
