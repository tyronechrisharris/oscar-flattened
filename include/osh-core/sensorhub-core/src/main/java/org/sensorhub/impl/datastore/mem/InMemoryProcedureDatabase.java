/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.mem;

import java.util.concurrent.Callable;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.database.DatabaseConfig;
import org.sensorhub.api.database.IProcedureDatabase;
import org.sensorhub.api.datastore.procedure.IProcedureStore;
import org.sensorhub.api.datastore.property.IPropertyStore;
import org.sensorhub.impl.module.AbstractModule;


/**
 * <p>
 * In-memory implementation of a procedure database.
 * </p>
 *
 * @author Alex Robin
 * @date Mar 30, 2022
 */
public class InMemoryProcedureDatabase extends AbstractModule<DatabaseConfig> implements IProcedureDatabase
{
    int databaseNum = 0;
    IProcedureStore procStore;
    IPropertyStore propStore;
    

    public InMemoryProcedureDatabase()
    {
        this((byte)0);
    }
    
    
    public InMemoryProcedureDatabase(int databaseNum)
    {
        this.databaseNum = databaseNum;
        this.procStore = new InMemoryProcedureStore(databaseNum);
    }
    
    
    @Override
    public IProcedureStore getProcedureStore()
    {
        return procStore;
    }


    @Override
    public IPropertyStore getPropertyStore()
    {
        return propStore;
    }


    @Override
    public Integer getDatabaseNum()
    {
        return databaseNum;
    }
    
    
    public <T> T executeTransaction(Callable<T> transaction) throws Exception
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public void commit()
    {
    }


    @Override
    protected void doStart() throws SensorHubException
    {
    }


    @Override
    protected void doStop() throws SensorHubException
    {
    }


    @Override
    public boolean isOpen()
    {
        return true;
    }


    @Override
    public boolean isReadOnly()
    {
        return false;
    }

}
