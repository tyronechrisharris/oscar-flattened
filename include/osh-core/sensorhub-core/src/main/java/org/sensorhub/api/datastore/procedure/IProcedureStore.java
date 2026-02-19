/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore.procedure;

import org.sensorhub.api.datastore.feature.IFeatureStoreBase;
import org.sensorhub.api.datastore.feature.IFeatureStoreBase.FeatureField;
import org.sensorhub.api.datastore.procedure.IProcedureStore.ProcedureField;
import org.sensorhub.api.procedure.IProcedureWithDesc;


/**
 * <p>
 * Interface for data stores containing procedure SensorML descriptions
 * </p>
 *
 * @author Alex Robin
 * @date Oct 4, 2021
 */
public interface IProcedureStore extends IFeatureStoreBase<IProcedureWithDesc, ProcedureField, ProcedureFilter>
{
    
    public static class ProcedureField extends FeatureField
    {
        public static final ProcedureField TYPE_OF = new ProcedureField("typeOf");
        public static final ProcedureField KEYWORDS = new ProcedureField("keywords");
        public static final ProcedureField IDENTIFICATION = new ProcedureField("identification");
        public static final ProcedureField CLASSIFICATION = new ProcedureField("classification");
        public static final ProcedureField SECURITY_CONSTRAINTS = new ProcedureField("securityConstraints");
        public static final ProcedureField LEGAL_CONSTRAINTS = new ProcedureField("legalConstraints");
        public static final ProcedureField CHARACTERISTICS = new ProcedureField("characteristics");
        public static final ProcedureField CAPABILITIES = new ProcedureField("capabilities");
        public static final ProcedureField CONTACTS = new ProcedureField("contacts");
        public static final ProcedureField DOCUMENTATION = new ProcedureField("documentation");
        public static final ProcedureField CONFIGURATION = new ProcedureField("configuration");
        public static final ProcedureField INPUTS = new ProcedureField("inputs");
        public static final ProcedureField OUTPUTS = new ProcedureField("outputs");
        public static final ProcedureField PARAMETERS = new ProcedureField("parameters");
        public static final ProcedureField MODES = new ProcedureField("modes");
        public static final ProcedureField COMPONENTS = new ProcedureField("components");
        public static final ProcedureField CONNECTIONS = new ProcedureField("connections");
        
        public ProcedureField(String name)
        {
            super(name);
        }
    }
    
    
    @Override
    public default ProcedureFilter.Builder filterBuilder()
    {
        return new ProcedureFilter.Builder();
    }
    
}
