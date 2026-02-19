/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.system;

import java.util.concurrent.Executor;
import org.sensorhub.api.ISensorHub;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.feature.FeatureKey;


/**
 * Extension of {@link SystemDatabaseTransactionHandler} used in conjunction
 * with {@link SystemDriverTransactionHandler}.<br/>
 * This class is used internally by the system registry.
 **/
class SystemRegistryTransactionHandler extends SystemDatabaseTransactionHandler
{
    Executor parentExecutor;
    

    SystemRegistryTransactionHandler(ISensorHub hub, IObsSystemDatabase db, Executor parentExecutor)
    {
        super(hub.getEventBus(), db);
        this.parentExecutor = parentExecutor;
    }
    
    
    protected SystemDriverTransactionHandler createSystemHandler(FeatureKey procKey, String sysUID)
    {
        return new SystemDriverTransactionHandler(procKey, sysUID, null, this, parentExecutor);
    }

}
