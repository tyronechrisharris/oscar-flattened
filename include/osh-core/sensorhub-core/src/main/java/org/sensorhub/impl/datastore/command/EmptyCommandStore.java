/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.command;

import java.util.Set;
import java.util.stream.Stream;
import org.sensorhub.api.command.ICommandData;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.command.CommandFilter;
import org.sensorhub.api.datastore.command.CommandStats;
import org.sensorhub.api.datastore.command.CommandStatsQuery;
import org.sensorhub.api.datastore.command.ICommandStatusStore;
import org.sensorhub.api.datastore.command.ICommandStore;
import org.sensorhub.api.datastore.command.ICommandStore.CommandField;
import org.sensorhub.api.datastore.feature.IFoiStore;
import org.sensorhub.api.datastore.command.ICommandStreamStore;
import org.sensorhub.impl.datastore.ReadOnlyDataStore;


public class EmptyCommandStore extends ReadOnlyDataStore<BigId, ICommandData, CommandField, CommandFilter> implements ICommandStore
{
    ICommandStreamStore commandStreamStore = new EmptyCommandStreamStore();
    ICommandStatusStore commandStatusStore = new EmptyCommandStatusStore();
    
    
    public EmptyCommandStore()
    {
    }


    @Override
    public String getDatastoreName()
    {
        return "Empty Command Store";
    }


    @Override
    public Stream<Entry<BigId, ICommandData>> selectEntries(CommandFilter query, Set<CommandField> fields)
    {
         return Stream.empty();
    }


    @Override
    public ICommandData get(Object key)
    {
        return null;
    }


    @Override
    public Stream<CommandStats> getStatistics(CommandStatsQuery query)
    {
        return Stream.empty();
    }


    @Override
    public BigId add(ICommandData obs)
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }


    @Override
    public ICommandStreamStore getCommandStreams()
    {
        return commandStreamStore;
    }


    @Override
    public ICommandStatusStore getStatusReports()
    {
        return commandStatusStore;
    }


    @Override
    public void linkTo(IFoiStore foiStore)
    {
        throw new UnsupportedOperationException();
    }

}
