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

import java.util.Set;
import java.util.stream.Stream;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.data.IObsData;
import org.sensorhub.api.datastore.EmptyFilterIntersection;
import org.sensorhub.api.datastore.feature.IFoiStore;
import org.sensorhub.api.datastore.obs.IDataStreamStore;
import org.sensorhub.api.datastore.obs.IObsStore;
import org.sensorhub.api.datastore.obs.ObsFilter;
import org.sensorhub.api.datastore.obs.ObsStats;
import org.sensorhub.api.datastore.obs.ObsStatsQuery;
import org.sensorhub.api.datastore.obs.IObsStore.ObsField;
import org.sensorhub.impl.datastore.ReadOnlyDataStore;
import org.vast.util.Asserts;


/**
 * <p>
 * Filtered view implemented as a wrapper of the underlying {@link IObsStore}
 * </p>
 *
 * @author Alex Robin
 * @date Nov 3, 2020
 */
public class ObsStoreView extends ReadOnlyDataStore<BigId, IObsData, ObsField, ObsFilter> implements IObsStore
{
    IObsStore delegate;
    DataStreamStoreView dataStreamStoreView;
    ObsFilter viewFilter;
    
    
    public ObsStoreView(IObsStore delegate, ObsFilter viewFilter)
    {
        this.delegate = Asserts.checkNotNull(delegate, IObsStore.class);
        this.dataStreamStoreView = new DataStreamStoreView(delegate.getDataStreams(), viewFilter.getDataStreamFilter());
        this.viewFilter = viewFilter;
    }


    @Override
    public Stream<Entry<BigId, IObsData>> selectEntries(ObsFilter filter, Set<ObsField> fields)
    {
        try
        {
            if (viewFilter != null)
                filter = viewFilter.intersect(filter);
            return delegate.selectEntries(filter, fields);
        }
        catch (EmptyFilterIntersection e)
        {
            return Stream.empty();
        }
    }
    
    
    @Override
    public long countMatchingEntries(ObsFilter filter)
    {
        try
        {
            if (viewFilter != null)
                filter = viewFilter.intersect(filter);
            return delegate.countMatchingEntries(filter);
        }
        catch (EmptyFilterIntersection e)
        {
            return 0L;
        } 
    }


    @Override
    public IObsData get(Object key)
    {
        return delegate.get(key);
    }


    @Override
    public Stream<ObsStats> getStatistics(ObsStatsQuery query)
    {
        try
        {
            if (viewFilter != null)
            {
                var filter = viewFilter.intersect(query.getObsFilter());
                query = ObsStatsQuery.Builder.from(query)
                    .selectObservations(filter)
                    .build();
            }
            return delegate.getStatistics(query);
        }
        catch (EmptyFilterIntersection e)
        {
            return Stream.empty();
        }
    }
    
    
    @Override
    public String getDatastoreName()
    {
        return delegate.getDatastoreName();
    }


    @Override
    public IDataStreamStore getDataStreams()
    {
        return dataStreamStoreView;
    }


    @Override
    public BigId add(IObsData obs)
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }


    @Override
    public void linkTo(IFoiStore foiStore)
    {
        throw new UnsupportedOperationException();        
    }
}
