/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.view;

import java.util.concurrent.Callable;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.database.IProcedureDatabase;
import org.sensorhub.api.datastore.procedure.IProcedureStore;
import org.sensorhub.api.datastore.procedure.ProcedureFilter;
import org.sensorhub.api.datastore.property.IPropertyStore;
import org.sensorhub.impl.datastore.ReadOnlyDataStore;
import org.vast.util.Asserts;


public class ProcedureDatabaseView implements IProcedureDatabase
{
    IProcedureDatabase delegate;
    ProcedureStoreView procStoreView;
    PropertyStoreView propStoreView;
    
    
    public ProcedureDatabaseView(IProcedureDatabase delegate, ProcedureFilter procFilter)
    {
        this.delegate = Asserts.checkNotNull(delegate, IProcedureDatabase.class);
        
        if (procFilter == null)
        {
            // create filter that will never match anything
            procFilter = new ProcedureFilter.Builder()
                .withInternalIDs(BigId.NONE)
                .build();
        }
        
        this.procStoreView = new ProcedureStoreView(delegate.getProcedureStore(), procFilter);
        this.propStoreView = new PropertyStoreView(delegate.getPropertyStore(), delegate.getPropertyStore().selectAllFilter());
    }
    
    
    @Override
    public Integer getDatabaseNum()
    {
        // need to return the underlying database num so public IDs are
        // computed correctly
        return delegate.getDatabaseNum();
    }


    @Override
    public IProcedureStore getProcedureStore()
    {
        return procStoreView;
    }


    @Override
    public IPropertyStore getPropertyStore()
    {
        return propStoreView;
    }


    @Override
    public <T> T executeTransaction(Callable<T> transaction) throws Exception
    {
        throw new UnsupportedOperationException(ReadOnlyDataStore.READ_ONLY_ERROR_MSG);
    }


    @Override
    public void commit()
    {
        throw new UnsupportedOperationException(ReadOnlyDataStore.READ_ONLY_ERROR_MSG);
    }


    @Override
    public boolean isOpen()
    {
        return delegate.isOpen();
    }


    @Override
    public boolean isReadOnly()
    {
        return true;
    }

}
