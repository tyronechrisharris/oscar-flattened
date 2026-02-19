/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.view;

import java.util.Set;
import java.util.stream.Stream;
import org.sensorhub.api.command.ICommandStreamInfo;
import org.sensorhub.api.datastore.EmptyFilterIntersection;
import org.sensorhub.api.datastore.command.CommandStreamFilter;
import org.sensorhub.api.datastore.command.CommandStreamKey;
import org.sensorhub.api.datastore.command.ICommandStreamStore;
import org.sensorhub.api.datastore.command.ICommandStreamStore.CommandStreamInfoField;
import org.sensorhub.api.datastore.system.ISystemDescStore;
import org.sensorhub.api.resource.ResourceFilter;
import org.sensorhub.impl.datastore.ReadOnlyDataStore;
import org.vast.util.Asserts;


/**
 * <p>
 * Filtered view implemented as a wrapper of the underlying {@link ICommandStreamStore}
 * </p>
 *
 * @author Alex Robin
 * @date Mar 12, 2021
 */
public class CommandStreamStoreView extends ReadOnlyDataStore<CommandStreamKey, ICommandStreamInfo, CommandStreamInfoField, CommandStreamFilter> implements ICommandStreamStore
{    
    ICommandStreamStore delegate;
    CommandStreamFilter viewFilter;
    
    
    public CommandStreamStoreView(ICommandStreamStore delegate, CommandStreamFilter viewFilter)
    {
        this.delegate = Asserts.checkNotNull(delegate, ICommandStreamStore.class);
        this.viewFilter = viewFilter;
    }
    

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Stream<Entry<CommandStreamKey, ICommandStreamInfo>> selectEntries(CommandStreamFilter filter, Set<CommandStreamInfoField> fields)
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
    public long countMatchingEntries(CommandStreamFilter filter)
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
    public ICommandStreamInfo get(Object key)
    {
        Asserts.checkArgument(key instanceof CommandStreamKey);
        
        if (viewFilter == null)
            return delegate.get(key);
        
        var pk = (CommandStreamKey)key;
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
    public CommandStreamKey add(ICommandStreamInfo dsInfo)
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }


    @Override
    public void linkTo(ISystemDescStore systemStore)
    {
        throw new UnsupportedOperationException();        
    }
}
