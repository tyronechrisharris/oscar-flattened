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
import org.sensorhub.api.datastore.feature.FeatureFilter;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.api.datastore.feature.IFeatureStore;
import org.sensorhub.api.datastore.feature.IFeatureStoreBase.FeatureField;
import org.sensorhub.impl.datastore.DataStoreUtils;
import org.vast.ogc.gml.IFeature;


/**
 * <p>
 * In-memory implementation of feature store backed by a {@link java.util.NavigableMap}.
 * This implementation is only used to store the latest feature state and thus
 * doesn't support versioning/history of feature descriptions.
 * </p>
 *
 * @author Alex Robin
 * @date Sep 28, 2019
 */
public class InMemoryFeatureStore extends InMemoryBaseFeatureStore<IFeature, FeatureField, FeatureFilter> implements IFeatureStore
{
    
    public InMemoryFeatureStore(int idScope)
    {
        super(idScope, DataStoreUtils.getFeatureHashIdProvider(831496768));
    }
    
    
    public InMemoryFeatureStore(int idScope, IdProvider<? super IFeature> idProvider)
    {
        super(idScope, idProvider);
    }
    
    
    @Override
    protected Stream<Entry<FeatureKey, IFeature>> getIndexedStream(FeatureFilter filter)
    {
        var resultStream = super.getIndexedStream(filter);
        
        if (filter.getParentFilter() != null)
        {
            var parentIDStream = DataStoreUtils.selectFeatureIDs(this, filter.getParentFilter());
            return postFilterOnParents(resultStream, parentIDStream);
        }
        
        return resultStream;
    }
}
