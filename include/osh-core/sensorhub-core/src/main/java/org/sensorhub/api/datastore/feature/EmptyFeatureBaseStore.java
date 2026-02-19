/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2023 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore.feature;

import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.DataStoreException;
import org.sensorhub.api.datastore.feature.IFeatureStoreBase.FeatureField;
import org.sensorhub.api.datastore.resource.EmptyResourceStore;
import org.vast.ogc.gml.IFeature;
import org.vast.util.Bbox;


/**
 * <p>
 * Helper class to implement databases that don't support all datastores
 * </p>
 * 
 * @param <V> Feature type
 * @param <VF> Feature field enum type
 * @param <F> Feature filter type
 * 
 * @author Alex Robin
 * @since Jun 22, 2023
 */
public abstract class EmptyFeatureBaseStore<V extends IFeature, VF extends FeatureField, F extends FeatureFilterBase<? super V>>
    extends EmptyResourceStore<FeatureKey, V, VF, F> implements IFeatureStoreBase<V, VF, F>
{

    @Override
    public FeatureKey add(BigId parentID, V value) throws DataStoreException
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }


    @Override
    public BigId getParent(BigId internalID)
    {
        return null;
    }
    
    
    public Bbox getFeaturesBbox()
    {
        return new Bbox();
    }
}
