/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore.command;

import org.sensorhub.api.command.ICommandStatus;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.IDataStore;
import org.sensorhub.api.datastore.ValueField;
import org.sensorhub.api.datastore.command.ICommandStatusStore.CommandStatusField;


/**
 * <p>
 * Generic interface for data stores containing commands status.
 * </p><p>
 * Status reports retrieved by select methods are sorted by report time.
 * </p>
 *
 * @author Alex Robin
 * @date Dec 21, 2021
 */
public interface ICommandStatusStore extends IDataStore<BigId, ICommandStatus, CommandStatusField, CommandStatusFilter>
{
    public static class CommandStatusField extends ValueField
    {
        public static final CommandStatusField COMMAND_ID = new CommandStatusField("commandID");
        public static final CommandStatusField REPORT_TIME = new CommandStatusField("reportTime");
        public static final CommandStatusField EXEC_TIME = new CommandStatusField("executionTime");
        public static final CommandStatusField STATUS_CODE  = new CommandStatusField("statusCode");
        public static final CommandStatusField ERROR_MSG  = new CommandStatusField("error");
        
        public CommandStatusField(String name)
        {
            super(name);
        }
    }
    
    
    /**
     * Add a status report to the datastore.
     * @param rec Status report
     * @return The auto-generated ID
     */
    public BigId add(ICommandStatus rec);


    /**
     * @return A builder for a filter compatible with this datastore
     */
    public default CommandStatusFilter.Builder filterBuilder()
    {
        return new CommandStatusFilter.Builder();
    }
    
    
    @Override
    public default CommandStatusFilter selectAllFilter()
    {
        return filterBuilder().build();
    }
    
}
