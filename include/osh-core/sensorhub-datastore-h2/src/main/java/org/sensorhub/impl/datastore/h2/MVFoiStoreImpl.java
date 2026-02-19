/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.h2;

import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.h2.mvstore.MVStore;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.DataStoreException;
import org.sensorhub.api.datastore.feature.FoiFilter;
import org.sensorhub.api.datastore.feature.IFeatureStore;
import org.sensorhub.api.datastore.feature.IFoiStore;
import org.sensorhub.api.datastore.feature.IFoiStore.FoiField;
import org.sensorhub.api.datastore.obs.IObsStore;
import org.sensorhub.api.datastore.system.ISystemDescStore;
import org.sensorhub.impl.datastore.DataStoreUtils;
import org.sensorhub.impl.datastore.h2.MVDatabaseConfig.IdProviderType;
import org.vast.ogc.gml.IFeature;
import org.vast.util.Asserts;


/**
 * <p>
 * FOI Store implementation based on H2 MVStore.<br/>
 * Most of the work is done in {@link MVBaseFeatureStoreImpl} 
 * </p>
 *
 * @author Alex Robin
 * @date Apr 8, 2018
 */
public class MVFoiStoreImpl extends MVBaseFeatureStoreImpl<IFeature, FoiField, FoiFilter> implements IFoiStore
{
    ISystemDescStore systemStore;
    IObsStore obsStore;
        
    
    protected MVFoiStoreImpl()
    {
    }
    
    
    /**
     * Opens an existing foi store or create a new one with the specified name
     * @param mvStore MVStore instance containing the required maps
     * @param idScope Internal ID scope (database num)
     * @param idProviderType Type of ID provider to use to generate new IDs
     * @param newStoreInfo Data store info to use if a new store needs to be created
     * @return The existing datastore instance 
     */
    public static MVFoiStoreImpl open(MVStore mvStore, int idScope, IdProviderType idProviderType, MVDataStoreInfo newStoreInfo)
    {
        var dataStoreInfo = H2Utils.getDataStoreInfo(mvStore, newStoreInfo.getName());
        if (dataStoreInfo == null)
        {
            dataStoreInfo = newStoreInfo;
            H2Utils.addDataStoreInfo(mvStore, dataStoreInfo);
        }
        
        return (MVFoiStoreImpl)new MVFoiStoreImpl().init(mvStore, idScope, idProviderType, dataStoreInfo);
    }
    
    
    @Override
    protected void checkParentFeatureExists(BigId parentID) throws DataStoreException
    {
        if (systemStore != null)
            DataStoreUtils.checkParentFeatureExists(parentID, systemStore, this);
        else
            DataStoreUtils.checkParentFeatureExists(parentID, this);
    }
    
    
    @Override
    protected Stream<Entry<MVFeatureParentKey, IFeature>> getIndexedStream(FoiFilter filter)
    {
        var resultStream = super.getIndexedStream(filter);
        
        if (filter.getParentFilter() != null)
        {
            var parentIDStream = DataStoreUtils.selectSystemIDs(systemStore, filter.getParentFilter());
            
            if (resultStream == null)
            {
                resultStream = parentIDStream
                    .flatMap(id -> getParentResultStream(id, filter.getValidTime()));
            }
            else
            {
                var parentIDs = parentIDStream
                    .map(id -> id.getIdAsLong())
                    .collect(Collectors.toSet());
                
                resultStream = resultStream.filter(
                    e -> parentIDs.contains(((MVFeatureParentKey)e.getKey()).getParentID()));
                
                // post filter using keys valid time if needed
                if (filter.getValidTime() != null)
                    resultStream = postFilterKeyValidTime(resultStream, filter.getValidTime());
            }
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
        
        return resultStream;
    }
    
    
    @Override
    protected IFeature getFeatureWithAdjustedValidTime(MVFeatureParentKey fk, IFeature f)
    {
        return new FeatureValidTimeAdapter<IFeature>(fk, f, featuresIndex);
    }
    
    
    @Override
    public long countMatchingEntries(FoiFilter filter)
    {
        if (filter.getValuePredicate() == null &&
            filter.getLocationFilter() == null &&
            filter.getFullTextFilter() == null &&
            filter.getValidTime() == null &&
            filter.getObservationFilter() == null)
        {
            if (filter.getParentFilter() != null)
            {
                return DataStoreUtils.selectSystemIDs(systemStore, filter.getParentFilter())
                    .mapToLong(id -> {
                        long parentID = id.getIdAsLong();
                        var k0 = new MVFeatureParentKey(0, parentID, 1, Instant.MIN);
                        var k1 = new MVFeatureParentKey(0, parentID, Long.MAX_VALUE, Instant.MAX);
                        var first = featuresIndex.ceilingKey(k0);
                        var last = featuresIndex.floorKey(k1);
                        
                        if (first == null || last == null || first.parentID != parentID)
                            return 0;
                        else
                            return featuresIndex.getKeyIndex(last) - featuresIndex.getKeyIndex(first) + 1;
                    })
                    .reduce(0, Long::sum);
            }
            else
                return featuresIndex.sizeAsLong();
        }
        
        return selectEntries(filter).count();
    }


    @Override
    public void linkTo(ISystemDescStore systemStore)
    {
        this.systemStore = Asserts.checkNotNull(systemStore, ISystemDescStore.class);
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
