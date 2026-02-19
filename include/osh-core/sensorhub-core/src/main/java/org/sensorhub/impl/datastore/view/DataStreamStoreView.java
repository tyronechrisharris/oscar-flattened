/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.view;

import java.util.Set;
import java.util.stream.Stream;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.api.datastore.EmptyFilterIntersection;
import org.sensorhub.api.datastore.obs.DataStreamFilter;
import org.sensorhub.api.datastore.obs.DataStreamKey;
import org.sensorhub.api.datastore.obs.IDataStreamStore;
import org.sensorhub.api.datastore.obs.IDataStreamStore.DataStreamInfoField;
import org.sensorhub.api.datastore.system.ISystemDescStore;
import org.sensorhub.api.resource.ResourceFilter;
import org.sensorhub.impl.datastore.ReadOnlyDataStore;
import org.vast.util.Asserts;


/**
 * <p>
 * Filtered view implemented as a wrapper of the underlying {@link IDataStreamStore}
 * </p>
 *
 * @author Alex Robin
 * @date Nov 3, 2020
 */
public class DataStreamStoreView extends ReadOnlyDataStore<DataStreamKey, IDataStreamInfo, DataStreamInfoField, DataStreamFilter> implements IDataStreamStore
{    
    IDataStreamStore delegate;
    DataStreamFilter viewFilter;
    
    
    public DataStreamStoreView(IDataStreamStore delegate, DataStreamFilter viewFilter)
    {
        this.delegate = Asserts.checkNotNull(delegate, IDataStreamStore.class);
        this.viewFilter = viewFilter;
    }
    

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Stream<Entry<DataStreamKey, IDataStreamInfo>> selectEntries(DataStreamFilter filter, Set<DataStreamInfoField> fields)
    {
        try
        {
            if (viewFilter != null)
                filter = viewFilter.intersect((ResourceFilter)filter);
            return delegate.selectEntries(filter, fields);
        }
        catch (EmptyFilterIntersection e)
        {
            return Stream.empty();
        }
    }
    
    
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public long countMatchingEntries(DataStreamFilter filter)
    {
        try
        {
            if (viewFilter != null)
                filter = viewFilter.intersect((ResourceFilter)filter);
            return delegate.countMatchingEntries(filter);
        }
        catch (EmptyFilterIntersection e)
        {
            return 0L;
        } 
    }


    @Override
    public IDataStreamInfo get(Object key)
    {
        Asserts.checkArgument(key instanceof DataStreamKey);
        
        if (viewFilter == null)
            return delegate.get(key);
        
        var pk = (DataStreamKey)key;
        if (viewFilter.getInternalIDs() != null && !viewFilter.getInternalIDs().contains(pk.getInternalID()))
            return null;
        
        var proc = delegate.get(key);
        return viewFilter.test(proc) ? proc : null;
    }
    
    
    @Override
    public String getDatastoreName()
    {
        return delegate.getDatastoreName();
    }


    @Override
    public DataStreamKey add(IDataStreamInfo dsInfo)
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }


    @Override
    public void linkTo(ISystemDescStore systemStore)
    {
        throw new UnsupportedOperationException();        
    }
}
