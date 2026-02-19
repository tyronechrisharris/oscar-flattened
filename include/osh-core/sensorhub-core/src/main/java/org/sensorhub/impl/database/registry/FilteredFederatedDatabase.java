/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.database.registry;

import java.util.Collection;
import java.util.Set;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.database.IDatabaseRegistry;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.database.IProcedureDatabase;
import org.sensorhub.api.datastore.command.CommandFilter;
import org.sensorhub.api.datastore.obs.ObsFilter;
import org.sensorhub.api.datastore.procedure.ProcedureFilter;
import org.sensorhub.impl.datastore.view.ObsSystemDatabaseView;
import org.sensorhub.impl.datastore.view.ProcedureDatabaseView;
import org.vast.util.Asserts;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;


/**
 * <p>
 * Extension of federated obs database allowing to filter out certain
 * databases or add new ones.
 * </p>
 *
 * @author Alex Robin
 * @since Dec 11, 2020
 */
public class FilteredFederatedDatabase extends FederatedDatabase
{
    final Set<Integer> unfilteredDatabases = Sets.newHashSet();
    final ObsFilter obsFilter;
    final CommandFilter cmdFilter;
    final ProcedureFilter procFilter;
    
    
    public FilteredFederatedDatabase(IDatabaseRegistry registry, ObsFilter obsFilter, int... unfilteredDatabases)
    {
        super(registry);
        this.obsFilter = Asserts.checkNotNull(obsFilter, ObsFilter.class);
        this.cmdFilter = null;
        this.procFilter = null;
        
        for (int dbNum: unfilteredDatabases)
            this.unfilteredDatabases.add(dbNum);
    }
    
    
    public FilteredFederatedDatabase(IDatabaseRegistry registry, CommandFilter cmdFilter, int... unfilteredDatabases)
    {
        super(registry);
        this.obsFilter = null;
        this.cmdFilter = Asserts.checkNotNull(cmdFilter, CommandFilter.class);
        this.procFilter = null;
        
        for (int dbNum: unfilteredDatabases)
            this.unfilteredDatabases.add(dbNum);
    }
    
    
    public FilteredFederatedDatabase(IDatabaseRegistry registry, ObsFilter obsFilter, CommandFilter cmdFilter, int... unfilteredDatabases)
    {
        super(registry);
        this.obsFilter = Asserts.checkNotNull(obsFilter, ObsFilter.class);
        this.cmdFilter = Asserts.checkNotNull(cmdFilter, CommandFilter.class);
        this.procFilter = null;
        
        for (int dbNum: unfilteredDatabases)
            this.unfilteredDatabases.add(dbNum);
    }
    
    
    public FilteredFederatedDatabase(IDatabaseRegistry registry, ObsFilter obsFilter, CommandFilter cmdFilter, ProcedureFilter procFilter, int... unfilteredDatabases)
    {
        super(registry);
        this.obsFilter = Asserts.checkNotNull(obsFilter, ObsFilter.class);
        this.cmdFilter = Asserts.checkNotNull(cmdFilter, CommandFilter.class);
        this.procFilter = Asserts.checkNotNull(procFilter, ProcedureFilter.class);
        
        for (int dbNum: unfilteredDatabases)
            this.unfilteredDatabases.add(dbNum);
    }
    
    
    protected IObsSystemDatabase getObsSystemDatabase(BigId id)
    {
        var db = super.getObsSystemDatabase(id);
        if (db != null)
            return new ObsSystemDatabaseView(db, obsFilter, cmdFilter);
        else
            return null;
    }
    
    
    protected IProcedureDatabase getProcedureDatabase(BigId id)
    {
        var db = super.getProcedureDatabase(id);
        if (db != null)
            return new ProcedureDatabaseView(db, procFilter);
        else
            return null;
    }
    

    @Override
    protected Collection<IObsSystemDatabase> getAllObsDatabases()
    {
        var allDbs = super.getAllObsDatabases();
        return Collections2.transform(allDbs, db -> {
            var dbNum = db.getDatabaseNum();
            if (!unfilteredDatabases.contains(dbNum))
                return new ObsSystemDatabaseView(db, obsFilter, cmdFilter);
            return db;
        });
    }
    
    
    @Override
    protected Collection<IProcedureDatabase> getAllProcDatabases()
    {
        var allDbs = super.getAllProcDatabases();
        return Collections2.transform(allDbs, db -> {
            var dbNum = db.getDatabaseNum();
            if (!unfilteredDatabases.contains(dbNum))
                return new ProcedureDatabaseView(db, procFilter);
            return db;
        });
    }

}
