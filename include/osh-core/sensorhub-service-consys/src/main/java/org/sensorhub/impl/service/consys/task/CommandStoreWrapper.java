/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.task;

import java.util.stream.Stream;
import org.sensorhub.api.command.ICommandData;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.command.CommandFilter;
import org.sensorhub.api.datastore.command.CommandStats;
import org.sensorhub.api.datastore.command.CommandStatsQuery;
import org.sensorhub.api.datastore.command.ICommandStatusStore;
import org.sensorhub.api.datastore.command.ICommandStore;
import org.sensorhub.api.datastore.command.ICommandStore.CommandField;
import org.sensorhub.api.datastore.command.ICommandStreamStore;
import org.sensorhub.api.datastore.feature.IFoiStore;
import org.sensorhub.impl.service.consys.AbstractDataStoreWrapper;


public class CommandStoreWrapper extends AbstractDataStoreWrapper<BigId, ICommandData, CommandField, CommandFilter, ICommandStore> implements ICommandStore
{
    
    public CommandStoreWrapper(ICommandStore readStore, ICommandStore writeStore)
    {
        super(readStore, writeStore);
    }


    @Override
    public CommandFilter.Builder filterBuilder()
    {
        return new CommandFilter.Builder();
    }


    @Override
    public BigId add(ICommandData cmd)
    {
        return getWriteStore().add(cmd);
    }


    @Override
    public Stream<CommandStats> getStatistics(CommandStatsQuery query)
    {
        return getReadStore().getStatistics(query);
    }


    @Override
    public ICommandStreamStore getCommandStreams()
    {
        return getReadStore().getCommandStreams();
    }


    @Override
    public ICommandStatusStore getStatusReports()
    {
        return getReadStore().getStatusReports();
    }


    @Override
    public void linkTo(IFoiStore foiStore)
    {
        throw new UnsupportedOperationException();
    }

}
