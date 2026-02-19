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
import org.sensorhub.api.command.ICommandStreamInfo;
import org.sensorhub.api.datastore.command.CommandFilter;
import org.sensorhub.api.datastore.command.CommandStreamFilter;
import org.sensorhub.api.datastore.command.CommandStreamKey;
import org.sensorhub.api.datastore.command.ICommandStreamStore;
import org.sensorhub.api.datastore.command.ICommandStreamStore.CommandStreamInfoField;
import org.sensorhub.api.datastore.system.ISystemDescStore;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.impl.database.registry.FederatedDatabase.ObsSystemDbFilterInfo;
import org.sensorhub.impl.datastore.ReadOnlyDataStore;
import org.vast.util.Asserts;


/**
 * <p>
 * Implementation of command stream store that provides federated read-only
 * access to several underlying databases.
 * </p>
 *
 * @author Alex Robin
 * @date Mar 24, 2021
 */
public class FederatedCommandStreamStore extends ReadOnlyDataStore<CommandStreamKey, ICommandStreamInfo, CommandStreamInfoField, CommandStreamFilter> implements ICommandStreamStore
{
    final FederatedDatabase parentDb;
    
    
    FederatedCommandStreamStore(FederatedDatabase db)
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
        for (var db: parentDb.getAllObsDatabases())
            count += db.getCommandStore().getCommandStreams().getNumRecords();
        return count;
    }
    
    
    protected CommandStreamKey ensureCommandStreamKey(Object obj)
    {
        Asserts.checkArgument(obj instanceof CommandStreamKey, "key must be a CommandStreamKey");
        return (CommandStreamKey)obj;
    }


    @Override
    public boolean containsKey(Object obj)
    {
        var key = ensureCommandStreamKey(obj);
        var id = key.getInternalID();
        
        // delegate to database identified by id scope
        var db = parentDb.getObsSystemDatabase(id);
        if (db != null)
            return db.getCommandStore().getCommandStreams().containsKey(key);
        else
            return false;
    }


    @Override
    public boolean containsValue(Object value)
    {
        for (var db: parentDb.getAllObsDatabases())
        {
            if (db.getCommandStore().getCommandStreams().containsValue(value))
                return true;
        }
        
        return false;
    }


    @Override
    public ICommandStreamInfo get(Object obj)
    {
        var key = ensureCommandStreamKey(obj);
        var id = key.getInternalID();
        
        // delegate to database identified by id scope
        var db = parentDb.getObsSystemDatabase(id);
        if (db != null)
            return db.getCommandStore().getCommandStreams().get(key);
        else
            return null;
    }
    
    
    /*
     * Get dispatch map according to internal IDs used in filter
     */
    protected Map<Integer, ObsSystemDbFilterInfo> getFilterDispatchMap(CommandStreamFilter filter)
    {
        if (filter.getInternalIDs() != null)
        {
            var filterDispatchMap = parentDb.getObsDbFilterDispatchMap(filter.getInternalIDs());
            for (var filterInfo: filterDispatchMap.values())
            {
                filterInfo.filter = CommandStreamFilter.Builder
                    .from(filter)
                    .withInternalIDs(filterInfo.ids)
                    .build();
            }
            
            return filterDispatchMap;
        }
        
        else if (filter.getSystemFilter() != null)
        {
            // delegate to system store to get filter dispatch map
            var filterDispatchMap = parentDb.systemStore.getFilterDispatchMap(filter.getSystemFilter());
            if (filterDispatchMap != null)
            {
                for (var filterInfo: filterDispatchMap.values())
                {
                    filterInfo.filter = CommandStreamFilter.Builder
                        .from(filter)
                        .withSystems((SystemFilter)filterInfo.filter)
                        .build();
                }
            }
            
            return filterDispatchMap;
        }
        
        else if (filter.getCommandFilter() != null)
        {
            // delegate to command store handle command filter dispatch map
            var filterDispatchMap = parentDb.commandStore.getFilterDispatchMap(filter.getCommandFilter());
            if (filterDispatchMap != null)
            {
                for (var filterInfo: filterDispatchMap.values())
                {
                    filterInfo.filter = CommandStreamFilter.Builder
                        .from(filter)
                        .withCommands((CommandFilter)filterInfo.filter)
                        .build();
                }
            }
            
            return filterDispatchMap;
        }
        
        return null;
    }
    
    
    @Override
    public Stream<Entry<CommandStreamKey, ICommandStreamInfo>> selectEntries(CommandStreamFilter filter, Set<CommandStreamInfoField> fields)
    {
        // if any kind of internal IDs are used, we need to dispatch the correct filter
        // to the corresponding DB so we create this map
        var filterDispatchMap = getFilterDispatchMap(filter);
        
        if (filterDispatchMap != null)
        {
            return filterDispatchMap.values().stream()
                .flatMap(v -> {
                    return v.db.getCommandStore().getCommandStreams().selectEntries((CommandStreamFilter)v.filter, fields);
                })
                .limit(filter.getLimit());
        }
        
        // otherwise scan all DBs
        else
        {
            return parentDb.getAllObsDatabases().stream()
                .flatMap(db -> {
                    return db.getCommandStore().getCommandStreams().selectEntries(filter, fields);
                })
                .limit(filter.getLimit());
        }
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
