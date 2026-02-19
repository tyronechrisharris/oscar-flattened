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

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sensorhub.api.datastore.IdProvider;
import org.sensorhub.api.datastore.deployment.IDeploymentStore;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.api.datastore.obs.IDataStreamStore;
import org.sensorhub.api.datastore.procedure.IProcedureStore;
import org.sensorhub.api.datastore.system.ISystemDescStore;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.api.datastore.system.ISystemDescStore.SystemField;
import org.sensorhub.api.system.ISystemWithDesc;
import org.vast.util.Asserts;
import org.sensorhub.impl.datastore.DataStoreUtils;


/**
 * <p>
 * In-memory implementation of system store backed by a {@link java.util.NavigableMap}.
 * This implementation is only used to store the latest system state and thus
 * doesn't support system description history.
 * </p>
 *
 * @author Alex Robin
 * @date Sep 28, 2019
 */
public class InMemorySystemStore extends InMemoryBaseFeatureStore<ISystemWithDesc, SystemField, SystemFilter> implements ISystemDescStore
{
    IDataStreamStore dataStreamStore;
    IProcedureStore procedureStore;
    IDeploymentStore deploymentStore;
    
    
    public InMemorySystemStore(int idScope)
    {
        super(idScope, DataStoreUtils.getFeatureHashIdProvider(1353704900));
    }
    
    
    public InMemorySystemStore(int idScope, IdProvider<? super ISystemWithDesc> idProvider)
    {
        super(idScope, idProvider);
    }
    
    
    @Override
    protected Stream<Entry<FeatureKey, ISystemWithDesc>> getIndexedStream(SystemFilter filter)
    {
        var resultStream = super.getIndexedStream(filter);
        
        if (filter.getParentFilter() != null)
        {
            var parentIDStream = DataStoreUtils.selectSystemIDs(this, filter.getParentFilter());
            resultStream = postFilterOnParents(resultStream, parentIDStream);
        }
        
        if (filter.getDataStreamFilter() != null)
        {
            var sysIDs = DataStoreUtils.selectDataStreams(dataStreamStore, filter.getDataStreamFilter())
                .map(ds -> ds.getSystemID().getInternalID())
                .collect(Collectors.toSet());
            
            if (resultStream == null)
            {            
                return super.getIndexedStream(SystemFilter.Builder.from(filter)
                    .withInternalIDs(sysIDs)
                    .build());
            }
            else
            {
                resultStream = resultStream
                    .filter(e -> sysIDs.contains(e.getKey().getInternalID()));
            }
        }
        
        return resultStream;
    }
    
    
    @Override
    public void linkTo(IDataStreamStore dataStreamStore)
    {
        this.dataStreamStore = Asserts.checkNotNull(dataStreamStore, IDataStreamStore.class);
    }


    @Override
    public void linkTo(IProcedureStore procedureStore)
    {
        this.procedureStore = Asserts.checkNotNull(procedureStore, IProcedureStore.class);
    }


    @Override
    public void linkTo(IDeploymentStore deploymentStore)
    {
        this.deploymentStore = Asserts.checkNotNull(deploymentStore, IDeploymentStore.class);
    }
}
