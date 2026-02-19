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

import org.sensorhub.api.command.ICommandStreamInfo;
import org.sensorhub.api.datastore.command.CommandStreamFilter;
import org.sensorhub.api.datastore.command.CommandStreamKey;
import org.sensorhub.api.datastore.command.ICommandStreamStore;
import org.sensorhub.api.datastore.command.ICommandStreamStore.CommandStreamInfoField;
import org.sensorhub.api.datastore.system.ISystemDescStore;
import org.sensorhub.impl.service.consys.resource.AbstractResourceStoreWrapper;


public class CommandStreamStoreWrapper extends AbstractResourceStoreWrapper<CommandStreamKey, ICommandStreamInfo, CommandStreamInfoField, CommandStreamFilter, ICommandStreamStore> implements ICommandStreamStore
{
    
    public CommandStreamStoreWrapper(ICommandStreamStore readStore, ICommandStreamStore writeStore)
    {
        super(readStore, writeStore);
    }


    @Override
    public CommandStreamFilter.Builder filterBuilder()
    {
        return (CommandStreamFilter.Builder)super.filterBuilder();
    }


    @Override
    public void linkTo(ISystemDescStore systemStore)
    {
        throw new UnsupportedOperationException();
    }

}
