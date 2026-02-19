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
import org.sensorhub.api.datastore.EmptyFilterIntersection;
import org.sensorhub.api.datastore.feature.FeatureFilterBase;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.api.datastore.feature.IFeatureStoreBase;
import org.sensorhub.api.datastore.feature.IFeatureStoreBase.FeatureField;
import org.sensorhub.api.resource.ResourceFilter;
import org.sensorhub.impl.datastore.ReadOnlyDataStore;
import org.vast.ogc.gml.IFeature;
import org.vast.util.Asserts;
import org.vast.util.Bbox;


public abstract class FeatureStoreViewBase<
        V extends IFeature,
        VF extends FeatureField,
        F extends FeatureFilterBase<? super V>,
        S extends IFeatureStoreBase<V, VF, F>>
    extends ReadOnlyDataStore<FeatureKey, V, VF, F>
    implements IFeatureStoreBase<V, VF, F>
{
    S delegate;
    F viewFilter;
    
    
    protected FeatureStoreViewBase(S delegate, F viewFilter)
    {
        this.delegate = delegate;
        this.viewFilter = viewFilter;
    }
    

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Stream<Entry<FeatureKey, V>> selectEntries(F filter, Set<VF> fields)
    {
        try
        {
            if (viewFilter != null)
                filter = (F)viewFilter.intersect((ResourceFilter)filter);
            return delegate.selectEntries(filter, fields);
        }
        catch (EmptyFilterIntersection e)
        {
            return Stream.empty();
        }
    }
    
    
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public long countMatchingEntries(F filter)
    {
        try
        {
            if (viewFilter != null)
                filter = (F)viewFilter.intersect((ResourceFilter)filter);
            return delegate.countMatchingEntries(filter);
        }
        catch (EmptyFilterIntersection e)
        {
            return 0L;
        } 
    }


    @Override
    public V get(Object key)
    {
        Asserts.checkArgument(key instanceof FeatureKey);
                
        if (viewFilter == null)
            return delegate.get(key);
        
        var fk = (FeatureKey)key;
        if (viewFilter.getInternalIDs() != null && !viewFilter.getInternalIDs().contains(fk.getInternalID()))
            return null;
        
        var proc = delegate.get(key);
        return viewFilter.test(proc) ? proc : null;
    }
    
    
    @Override
    public BigId getParent(BigId internalID)
    {
        if (contains(internalID))
            return delegate.getParent(internalID);
        else
            return null;
    }


    @Override
    public Bbox getFeaturesBbox()
    {
        return delegate.getFeaturesBbox();
    }
    
    
    @Override
    public String getDatastoreName()
    {
        return delegate.getDatastoreName();
    }


    @Override
    public FeatureKey add(V feature)
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }


    @Override
    public FeatureKey add(BigId parentId, V value)
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }
}
