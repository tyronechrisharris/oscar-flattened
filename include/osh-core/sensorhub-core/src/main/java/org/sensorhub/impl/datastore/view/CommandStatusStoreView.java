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
import org.sensorhub.api.command.ICommandStatus;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.EmptyFilterIntersection;
import org.sensorhub.api.datastore.command.CommandStatusFilter;
import org.sensorhub.api.datastore.command.ICommandStatusStore;
import org.sensorhub.api.datastore.command.ICommandStatusStore.CommandStatusField;
import org.sensorhub.impl.datastore.ReadOnlyDataStore;
import org.vast.util.Asserts;


/**
 * <p>
 * Filtered view implemented as a wrapper of the underlying {@link ICommandStatusStore}
 * </p>
 *
 * @author Alex Robin
 * @date Jan 4, 2022
 */
public class CommandStatusStoreView extends ReadOnlyDataStore<BigId, ICommandStatus, CommandStatusField, CommandStatusFilter> implements ICommandStatusStore
{
    ICommandStatusStore delegate;
    CommandStoreView commandStreamStoreView;
    CommandStatusFilter viewFilter;
    
    
    public CommandStatusStoreView(ICommandStatusStore delegate, CommandStatusFilter viewFilter)
    {
        this.delegate = Asserts.checkNotNull(delegate, ICommandStatusStore.class);
        this.viewFilter = viewFilter;
    }


    @Override
    public Stream<Entry<BigId, ICommandStatus>> selectEntries(CommandStatusFilter filter, Set<CommandStatusField> fields)
    {
        try
        {
            if (viewFilter != null)
                filter = viewFilter.intersect(filter);
            return delegate.selectEntries(filter, fields);
        }
        catch (EmptyFilterIntersection e)
        {
            return Stream.empty();
        }
    }
    
    
    @Override
    public long countMatchingEntries(CommandStatusFilter filter)
    {
        try
        {
            if (viewFilter != null)
                filter = viewFilter.intersect(filter);
            return delegate.countMatchingEntries(filter);
        }
        catch (EmptyFilterIntersection e)
        {
            return 0L;
        } 
    }


    @Override
    public ICommandStatus get(Object key)
    {
        return delegate.get(key);
    }
    
    
    @Override
    public String getDatastoreName()
    {
        return delegate.getDatastoreName();
    }


    @Override
    public BigId add(ICommandStatus status)
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }
}
