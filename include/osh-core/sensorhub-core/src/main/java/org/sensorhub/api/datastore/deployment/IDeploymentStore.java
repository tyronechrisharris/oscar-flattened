/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore.deployment;

import org.sensorhub.api.datastore.feature.IFeatureStoreBase;
import org.sensorhub.api.datastore.feature.IFeatureStoreBase.FeatureField;
import org.sensorhub.api.datastore.system.ISystemDescStore;
import org.sensorhub.api.datastore.deployment.IDeploymentStore.DeploymentField;
import org.sensorhub.api.system.IDeploymentWithDesc;


/**
 * <p>
 * Interface for data stores containing SensorML descriptions of deployments
 * </p>
 *
 * @author Alex Robin
 * @since June 22, 2023
 */
public interface IDeploymentStore extends IFeatureStoreBase<IDeploymentWithDesc, DeploymentField, DeploymentFilter>
{
    
    public static class DeploymentField extends FeatureField
    {
        public static final DeploymentField TYPE_OF = new DeploymentField("typeOf");
        public static final DeploymentField KEYWORDS = new DeploymentField("keywords");
        public static final DeploymentField IDENTIFICATION = new DeploymentField("identification");
        public static final DeploymentField CLASSIFICATION = new DeploymentField("classification");
        public static final DeploymentField SECURITY_CONSTRAINTS = new DeploymentField("securityConstraints");
        public static final DeploymentField LEGAL_CONSTRAINTS = new DeploymentField("legalConstraints");
        public static final DeploymentField CHARACTERISTICS = new DeploymentField("characteristics");
        public static final DeploymentField CAPABILITIES = new DeploymentField("capabilities");
        public static final DeploymentField CONTACTS = new DeploymentField("contacts");
        public static final DeploymentField DOCUMENTATION = new DeploymentField("documentation");
        public static final DeploymentField COMPONENTS = new DeploymentField("components");
        
        public DeploymentField(String name)
        {
            super(name);
        }
    }
    
    
    @Override
    public default DeploymentFilter.Builder filterBuilder()
    {
        return new DeploymentFilter.Builder();
    }
    
    
    /**
     * Link this store to a system store to enable JOIN queries
     * @param systemStore
     */
    public void linkTo(ISystemDescStore systemStore);
    
}
