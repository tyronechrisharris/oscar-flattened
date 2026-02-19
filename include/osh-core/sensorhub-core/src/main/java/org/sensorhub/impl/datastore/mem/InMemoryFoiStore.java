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
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.DataStoreException;
import org.sensorhub.api.datastore.IdProvider;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.api.datastore.feature.FoiFilter;
import org.sensorhub.api.datastore.feature.IFeatureStore;
import org.sensorhub.api.datastore.feature.IFoiStore;
import org.sensorhub.api.datastore.feature.IFoiStore.FoiField;
import org.sensorhub.api.datastore.obs.IObsStore;
import org.sensorhub.api.datastore.system.ISystemDescStore;
import org.sensorhub.impl.datastore.DataStoreUtils;
import org.vast.ogc.gml.IFeature;
import org.vast.util.Asserts;


/**
 * <p>
 * In-memory implementation of FOI store backed by a {@link java.util.NavigableMap}.
 * This implementation is only used to store the latest feature state and thus
 * doesn't support versioning/history of FOI descriptions.
 * </p>
 *
 * @author Alex Robin
 * @date Sep 28, 2019
 */
public class InMemoryFoiStore extends InMemoryBaseFeatureStore<IFeature, FoiField, FoiFilter> implements IFoiStore
{
    ISystemDescStore procStore;
    IObsStore obsStore;
    
    
    public InMemoryFoiStore(int idScope)
    {
        super(idScope, DataStoreUtils.getFeatureHashIdProvider(806335237));
    }
    
    
    public InMemoryFoiStore(int idScope, IdProvider<? super IFeature> idProvider)
    {
        super(idScope, idProvider);
    }
    
    
    @Override
    protected void checkParentFeatureExists(BigId parentID) throws DataStoreException
    {
        if (procStore != null)
            DataStoreUtils.checkParentFeatureExists(parentID, procStore, this);
        else
            DataStoreUtils.checkParentFeatureExists(parentID, this);
    }
    
    
    @Override
    protected Stream<Entry<FeatureKey, IFeature>> getIndexedStream(FoiFilter filter)
    {
        var resultStream = super.getIndexedStream(filter);
        
        if (filter.getParentFilter() != null)
        {
            var parentIDStream = DataStoreUtils.selectSystemIDs(procStore, filter.getParentFilter());
            resultStream = postFilterOnParents(resultStream, parentIDStream);
        }
        
        if (filter.getObservationFilter() != null)
        {
            var foiIDs = obsStore.selectObservedFois(filter.getObservationFilter())
                .collect(Collectors.toSet());
            
            if (resultStream == null)
            {            
                resultStream = super.getIndexedStream(FoiFilter.Builder.from(filter)
                    .withInternalIDs(foiIDs)
                    .build());
            }
            else
            {
                resultStream = resultStream
                    .filter(e -> foiIDs.contains(e.getKey().getInternalID()));
            }
        }
        
        if (filter.getSampledFeatureFilter() != null)
        {
            // TODO
        }
        
        return resultStream;
    }


    @Override
    public void linkTo(ISystemDescStore procStore)
    {
        this.procStore = Asserts.checkNotNull(procStore, ISystemDescStore.class);        
    }
    

    @Override
    public void linkTo(IObsStore obsStore)
    {
        this.obsStore = Asserts.checkNotNull(obsStore, IObsStore.class);
    }


    @Override
    public void linkTo(IFeatureStore featureStore)
    {
        throw new UnsupportedOperationException();
    }
}
