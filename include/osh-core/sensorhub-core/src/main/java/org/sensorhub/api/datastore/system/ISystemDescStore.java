/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore.system;

import org.sensorhub.api.datastore.deployment.IDeploymentStore;
import org.sensorhub.api.datastore.feature.IFeatureStoreBase;
import org.sensorhub.api.datastore.obs.IDataStreamStore;
import org.sensorhub.api.datastore.procedure.IProcedureStore;
import org.sensorhub.api.datastore.procedure.IProcedureStore.ProcedureField;
import org.sensorhub.api.datastore.system.ISystemDescStore.SystemField;
import org.sensorhub.api.system.ISystemWithDesc;


/**
 * <p>
 * Interface for data stores containing system SensorML descriptions
 * </p>
 *
 * @author Alex Robin
 * @date Oct 4, 2020
 */
public interface ISystemDescStore extends IFeatureStoreBase<ISystemWithDesc, SystemField, SystemFilter>
{

    public static class SystemField extends ProcedureField
    {
        public static final SystemField ATTACHED_TO = new SystemField("attachedTo");
        public static final SystemField SPATIAL_REF_FRAMES = new SystemField("localReferenceFrame");
        public static final SystemField TIME_REF_FRAMES = new SystemField("localTimeFrame");
        public static final SystemField POSITION = new SystemField("position");
        public static final SystemField LOCAL_TIME = new SystemField("timePosition");
        
        
        public SystemField(String name)
        {
            super(name);
        }
    }
    
    
    @Override
    public default SystemFilter.Builder filterBuilder()
    {
        return new SystemFilter.Builder();
    }
    
    
    /**
     * Link this store to a datastream store to enable JOIN queries
     * @param dataStreamStore
     */
    public void linkTo(IDataStreamStore dataStreamStore);
    
    
    /**
     * Link this store to a procedure store to enable JOIN queries
     * @param procedureStore
     */
    public void linkTo(IProcedureStore procedureStore);
    
    
    /**
     * Link this store to a deployment store to enable JOIN queries
     * @param deploymentStore
     */
    public void linkTo(IDeploymentStore deploymentStore);
    
}
