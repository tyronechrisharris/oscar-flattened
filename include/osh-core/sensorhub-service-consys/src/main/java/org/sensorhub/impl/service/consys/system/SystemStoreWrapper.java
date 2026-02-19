/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.system;

import org.sensorhub.api.datastore.deployment.IDeploymentStore;
import org.sensorhub.api.datastore.obs.IDataStreamStore;
import org.sensorhub.api.datastore.procedure.IProcedureStore;
import org.sensorhub.api.datastore.system.ISystemDescStore;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.api.datastore.system.ISystemDescStore.SystemField;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.impl.service.consys.feature.AbstractFeatureStoreWrapper;


public class SystemStoreWrapper extends AbstractFeatureStoreWrapper<ISystemWithDesc, SystemField, SystemFilter, ISystemDescStore> implements ISystemDescStore
{
    
    public SystemStoreWrapper(ISystemDescStore readStore, ISystemDescStore writeStore)
    {
        super(readStore, writeStore);
    }
    
    
    @Override
    public SystemFilter.Builder filterBuilder()
    {
        return (SystemFilter.Builder)super.filterBuilder();
    }


    @Override
    public void linkTo(IDataStreamStore dataStreamStore)
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public void linkTo(IProcedureStore procedureStore)
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public void linkTo(IDeploymentStore deploymentStore)
    {
        throw new UnsupportedOperationException();
    }

}
