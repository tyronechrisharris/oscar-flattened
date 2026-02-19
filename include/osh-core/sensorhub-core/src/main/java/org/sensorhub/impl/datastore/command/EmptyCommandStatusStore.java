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
import org.sensorhub.api.command.ICommandStatus;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.command.CommandStatusFilter;
import org.sensorhub.api.datastore.command.ICommandStatusStore;
import org.sensorhub.api.datastore.command.ICommandStatusStore.CommandStatusField;
import org.sensorhub.impl.datastore.ReadOnlyDataStore;


public class EmptyCommandStatusStore extends ReadOnlyDataStore<BigId, ICommandStatus, CommandStatusField, CommandStatusFilter> implements ICommandStatusStore
{
    
    public EmptyCommandStatusStore()
    {
    }


    @Override
    public String getDatastoreName()
    {
        return "Empty Command Status Store";
    }


    @Override
    public Stream<Entry<BigId, ICommandStatus>> selectEntries(CommandStatusFilter query, Set<CommandStatusField> fields)
    {
         return Stream.empty();
    }


    @Override
    public ICommandStatus get(Object key)
    {
        return null;
    }


    @Override
    public BigId add(ICommandStatus obs)
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }

}
