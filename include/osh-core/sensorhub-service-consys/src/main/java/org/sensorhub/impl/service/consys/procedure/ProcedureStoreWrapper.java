/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.procedure;

import org.sensorhub.api.datastore.procedure.IProcedureStore;
import org.sensorhub.api.datastore.procedure.IProcedureStore.ProcedureField;
import org.sensorhub.api.datastore.procedure.ProcedureFilter;
import org.sensorhub.api.procedure.IProcedureWithDesc;
import org.sensorhub.impl.service.consys.feature.AbstractFeatureStoreWrapper;


public class ProcedureStoreWrapper extends AbstractFeatureStoreWrapper<IProcedureWithDesc, ProcedureField, ProcedureFilter, IProcedureStore> implements IProcedureStore
{
    
    public ProcedureStoreWrapper(IProcedureStore readStore, IProcedureStore writeStore)
    {
        super(readStore, writeStore);
    }
    
    
    @Override
    public ProcedureFilter.Builder filterBuilder()
    {
        return (ProcedureFilter.Builder)super.filterBuilder();
    }

}
