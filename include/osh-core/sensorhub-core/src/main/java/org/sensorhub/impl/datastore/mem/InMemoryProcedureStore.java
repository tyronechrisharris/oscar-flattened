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

import java.util.stream.Stream;
import org.sensorhub.api.datastore.IdProvider;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.api.datastore.procedure.IProcedureStore;
import org.sensorhub.api.datastore.procedure.IProcedureStore.ProcedureField;
import org.sensorhub.api.datastore.procedure.ProcedureFilter;
import org.sensorhub.api.procedure.IProcedureWithDesc;
import org.sensorhub.impl.datastore.DataStoreUtils;


/**
 * <p>
 * In-memory implementation of procedure store backed by a {@link java.util.NavigableMap}.
 * This implementation only maintains the latest version of the procedure
 * description and thus does not support procedure description history.
 * </p>
 *
 * @author Alex Robin
 * @date Mar 30, 2022
 */
public class InMemoryProcedureStore extends InMemoryBaseFeatureStore<IProcedureWithDesc, ProcedureField, ProcedureFilter> implements IProcedureStore
{
    
    public InMemoryProcedureStore(int idScope)
    {
        super(idScope, DataStoreUtils.getFeatureHashIdProvider(471489332));
    }
    
    
    public InMemoryProcedureStore(int idScope, IdProvider<? super IProcedureWithDesc> idProvider)
    {
        super(idScope, idProvider);
    }
    
    
    @Override
    protected Stream<Entry<FeatureKey, IProcedureWithDesc>> getIndexedStream(ProcedureFilter filter)
    {
        var resultStream = super.getIndexedStream(filter);
        
        if (filter.getParentFilter() != null)
        {
            var parentIDStream = DataStoreUtils.selectProcedureIDs(this, filter.getParentFilter());
            resultStream = postFilterOnParents(resultStream, parentIDStream);
        }
        
        return resultStream;
    }
}
