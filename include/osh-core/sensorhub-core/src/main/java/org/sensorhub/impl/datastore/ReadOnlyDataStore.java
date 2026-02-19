/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.sensorhub.api.datastore.IDataStore;
import org.sensorhub.api.datastore.IQueryFilter;
import org.sensorhub.api.datastore.ValueField;


/**
 * <p>
 * Helper class for read-only datastores
 * </p>
 * @param <K> Key type
 * @param <V> Value type  
 * @param <VF> Value field type
 * @param <Q> Query type
 *
 * @author Alex Robin
 * @date Mar 23, 2020
 */
public abstract class ReadOnlyDataStore<K, V, VF extends ValueField, Q extends IQueryFilter> implements IDataStore<K, V, VF, Q>
{
    public static final String READ_ONLY_ERROR_MSG = "This datastore is read-only";
    
    
    public void backup(OutputStream is) throws IOException
    {            
    }
    

    public void restore(InputStream os) throws IOException
    {            
    }
    

    public boolean isReadSupported()
    {
        return true;
    }
    

    public boolean isWriteSupported()
    {
        return false;
    }
    
    
    public boolean isReadOnly()
    {
        return true;
    }


    @Override
    public V put(K key, V value)
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }


    @Override
    public V remove(Object key)
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }


    @Override
    public void clear()
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }
    
    
    @Override
    public void commit()
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }
}