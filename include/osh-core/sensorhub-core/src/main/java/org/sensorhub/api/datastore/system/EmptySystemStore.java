/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2023 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore.system;

import org.sensorhub.api.datastore.deployment.IDeploymentStore;
import org.sensorhub.api.datastore.feature.EmptyFeatureBaseStore;
import org.sensorhub.api.datastore.obs.IDataStreamStore;
import org.sensorhub.api.datastore.procedure.IProcedureStore;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.api.datastore.system.ISystemDescStore.SystemField;


/**
 * <p>
 * Helper class to implement databases that don't support all datastores
 * </p>
**/
public class EmptySystemStore extends EmptyFeatureBaseStore<ISystemWithDesc, SystemField, SystemFilter> implements ISystemDescStore
{

    @Override
    public void linkTo(IDataStreamStore dataStreamStore)
    {
    }

    @Override
    public void linkTo(IProcedureStore procedureStore)
    {
    }

    @Override
    public void linkTo(IDeploymentStore deploymentStore)
    {
    }

}
