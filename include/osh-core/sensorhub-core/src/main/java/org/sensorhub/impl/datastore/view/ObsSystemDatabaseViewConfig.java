/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.view;

import org.sensorhub.api.ISensorHub;
import org.sensorhub.api.config.DisplayInfo;
import org.sensorhub.api.config.DisplayInfo.FieldType;
import org.sensorhub.api.config.DisplayInfo.FieldType.Type;
import org.sensorhub.api.config.DisplayInfo.ModuleType;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.IQueryFilter;
import org.sensorhub.api.datastore.command.CommandFilter;
import org.sensorhub.api.datastore.command.CommandStreamFilter;
import org.sensorhub.api.datastore.obs.DataStreamFilter;
import org.sensorhub.api.datastore.obs.ObsFilter;
import org.sensorhub.api.datastore.system.SystemFilter;


/**
 * <p>
 * Configuration class used to define a new filtered view or reference an 
 * existing one.
 * </p>
 *
 * @author Alex Robin
 * @date Oct 1, 2020
 */
public class ObsSystemDatabaseViewConfig
{
    @FieldType(Type.MODULE_ID)
    @ModuleType(IObsSystemDatabase.class)
    @DisplayInfo(label="Source Database ID", desc="ID of database module to read data from (Federated database will be used if not set")
    public String sourceDatabaseId;
    
    public IQueryFilter includeFilter;
    
    //public IQueryFilter excludeFilter;
    
        
    public IObsSystemDatabase getFilteredView(ISensorHub hub)
    {
        IObsSystemDatabase srcDatabase = sourceDatabaseId != null ?
            hub.getDatabaseRegistry().getObsDatabaseByModuleID(sourceDatabaseId) :
            hub.getDatabaseRegistry().getFederatedDatabase();
         
        if (includeFilter != null)
            return new ObsSystemDatabaseView(srcDatabase, getObsFilter(), getCommandFilter());
        else
            return srcDatabase;
    }
    
    
    public ObsFilter getObsFilter()
    {
        if (includeFilter instanceof ObsFilter)
        {
            return (ObsFilter)includeFilter;
        }
        else if (includeFilter instanceof DataStreamFilter)
        {
            return new ObsFilter.Builder()
                .withDataStreams((DataStreamFilter)includeFilter)
                .build();
        }
        else if (includeFilter instanceof SystemFilter)
        {
            return new ObsFilter.Builder()
                .withSystems((SystemFilter)includeFilter)
                .build();
        }
        else if (includeFilter instanceof CommandStreamFilter)
        {
            var sysFilter = ((CommandStreamFilter)includeFilter).getSystemFilter();
            if (sysFilter != null)
            {
                return new ObsFilter.Builder()
                    .withSystems(((CommandStreamFilter)includeFilter).getSystemFilter())
                    .build();
            }
        }
        
        return new ObsFilter.Builder()
            .build();
    }
    
    
    public CommandFilter getCommandFilter()
    {
        if (includeFilter instanceof CommandFilter)
        {
            return (CommandFilter)includeFilter;
        }
        else if (includeFilter instanceof CommandStreamFilter)
        {
            return new CommandFilter.Builder()
                .withCommandStreams((CommandStreamFilter)includeFilter)
                .build();
        }
        else if (includeFilter instanceof SystemFilter)
        {
            return new CommandFilter.Builder()
                .withSystems((SystemFilter)includeFilter)
                .build();
        }
        else if (includeFilter instanceof DataStreamFilter)
        {
            return new CommandFilter.Builder()
                .withSystems()
                    .withDataStreams((DataStreamFilter)includeFilter)
                    .done()
                .build();
        }
        else if (includeFilter instanceof ObsFilter)
        {
            var dsFilter = ((ObsFilter)includeFilter).getDataStreamFilter();
            if (dsFilter != null)
            {
                return new CommandFilter.Builder()
                    .withSystems()
                        .withDataStreams(dsFilter)
                        .done()
                    .build();
            }
        }
        
        return new CommandFilter.Builder()
            .build();
    }
}
