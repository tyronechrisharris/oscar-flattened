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
import org.sensorhub.api.command.ICommandStreamInfo;
import org.sensorhub.api.datastore.DataStoreException;
import org.sensorhub.api.datastore.command.CommandStreamFilter;
import org.sensorhub.api.datastore.command.CommandStreamKey;
import org.sensorhub.api.datastore.command.ICommandStreamStore;
import org.sensorhub.api.datastore.command.ICommandStreamStore.CommandStreamInfoField;
import org.sensorhub.api.datastore.system.ISystemDescStore;
import org.sensorhub.impl.datastore.ReadOnlyDataStore;


public class EmptyCommandStreamStore extends ReadOnlyDataStore<CommandStreamKey, ICommandStreamInfo, CommandStreamInfoField, CommandStreamFilter> implements ICommandStreamStore
{
    
    public EmptyCommandStreamStore()
    {
    }


    @Override
    public String getDatastoreName()
    {
        return "Empty CommandStream Store";
    }


    @Override
    public Stream<Entry<CommandStreamKey, ICommandStreamInfo>> selectEntries(CommandStreamFilter query, Set<CommandStreamInfoField> fields)
    {
        return Stream.empty();
    }


    @Override
    public ICommandStreamInfo get(Object key)
    {
        return null;
    }


    @Override
    public CommandStreamKey add(ICommandStreamInfo dsInfo) throws DataStoreException
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }


    @Override
    public void linkTo(ISystemDescStore systemStore)
    {
    }
}
