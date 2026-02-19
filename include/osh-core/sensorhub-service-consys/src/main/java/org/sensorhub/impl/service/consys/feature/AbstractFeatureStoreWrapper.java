/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.feature;

import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.DataStoreException;
import org.sensorhub.api.datastore.feature.FeatureFilterBase;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.api.datastore.feature.IFeatureStoreBase;
import org.sensorhub.api.datastore.feature.FeatureFilterBase.FeatureFilterBaseBuilder;
import org.sensorhub.api.datastore.feature.IFeatureStoreBase.FeatureField;
import org.sensorhub.impl.service.consys.resource.AbstractResourceStoreWrapper;
import org.vast.ogc.gml.IFeature;
import org.vast.util.Bbox;


public abstract class AbstractFeatureStoreWrapper<V extends IFeature, VF extends FeatureField, F extends FeatureFilterBase<? super V>, S extends IFeatureStoreBase<V, VF, F>>
    extends AbstractResourceStoreWrapper<FeatureKey, V, VF, F, S> implements IFeatureStoreBase<V, VF, F>
{
    
    protected AbstractFeatureStoreWrapper(S readStore, S writeStore)
    {
        super(readStore, writeStore);
    }


    @Override
    public FeatureFilterBaseBuilder<?,?,F> filterBuilder()
    {
        return (FeatureFilterBaseBuilder<?,?,F>)super.filterBuilder();
    }


    @Override
    public FeatureKey add(BigId parentId, V value) throws DataStoreException
    {
        return getWriteStore().add(parentId, value);
    }
    
    
    @Override
    public BigId getParent(BigId internalID)
    {
        return getReadStore().getParent(internalID);
    }


    @Override
    public Bbox getFeaturesBbox()
    {
        return getReadStore().getFeaturesBbox();
    }

}
