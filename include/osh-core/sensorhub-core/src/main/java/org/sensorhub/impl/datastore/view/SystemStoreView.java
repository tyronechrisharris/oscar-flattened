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

import org.sensorhub.api.datastore.deployment.IDeploymentStore;
import org.sensorhub.api.datastore.obs.IDataStreamStore;
import org.sensorhub.api.datastore.procedure.IProcedureStore;
import org.sensorhub.api.datastore.system.ISystemDescStore;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.api.datastore.system.ISystemDescStore.SystemField;
import org.sensorhub.api.system.ISystemWithDesc;
import org.vast.util.Asserts;


/**
 * <p>
 * Filtered view implemented as a wrapper of the underlying {@link ISystemDescStore}
 * </p>
 *
 * @author Alex Robin
 * @date Nov 3, 2020
 */
public class SystemStoreView extends FeatureStoreViewBase<ISystemWithDesc, SystemField, SystemFilter, ISystemDescStore> implements ISystemDescStore
{
    
    public SystemStoreView(ISystemDescStore delegate, SystemFilter viewFilter)
    {
        super(Asserts.checkNotNull(delegate, ISystemDescStore.class), viewFilter);
    }
    
    
    @Override
    public void linkTo(IDataStreamStore dataStreamStore)
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }


    @Override
    public void linkTo(IProcedureStore procedureStore)
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }


    @Override
    public void linkTo(IDeploymentStore deploymentStore)
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }
}