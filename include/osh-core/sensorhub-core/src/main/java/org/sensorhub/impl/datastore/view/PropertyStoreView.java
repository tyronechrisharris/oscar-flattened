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
import org.sensorhub.api.datastore.EmptyFilterIntersection;
import org.sensorhub.api.datastore.property.IPropertyStore;
import org.sensorhub.api.datastore.property.IPropertyStore.PropertyField;
import org.sensorhub.api.datastore.property.PropertyFilter;
import org.sensorhub.api.datastore.property.PropertyKey;
import org.sensorhub.api.resource.ResourceFilter;
import org.sensorhub.api.semantic.IDerivedProperty;
import org.sensorhub.impl.datastore.ReadOnlyDataStore;
import org.vast.util.Asserts;


/**
 * <p>
 * Filtered view implemented as a wrapper of the underlying {@link IPropertyStore}
 * </p>
 *
 * @author Alex Robin
 * @since June 22, 2023
 */
public class PropertyStoreView extends ReadOnlyDataStore<PropertyKey, IDerivedProperty, PropertyField, PropertyFilter> implements IPropertyStore
{    
    IPropertyStore delegate;
    PropertyFilter viewFilter;
    
    
    public PropertyStoreView(IPropertyStore delegate, PropertyFilter viewFilter)
    {
        this.delegate = Asserts.checkNotNull(delegate, IPropertyStore.class);
        this.viewFilter = viewFilter;
    }
    

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Stream<Entry<PropertyKey, IDerivedProperty>> selectEntries(PropertyFilter filter, Set<PropertyField> fields)
    {
        try
        {
            if (viewFilter != null)
                filter = viewFilter.intersect((ResourceFilter)filter);
            return delegate.selectEntries(filter, fields);
        }
        catch (EmptyFilterIntersection e)
        {
            return Stream.empty();
        }
    }
    
    
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public long countMatchingEntries(PropertyFilter filter)
    {
        try
        {
            if (viewFilter != null)
                filter = viewFilter.intersect((ResourceFilter)filter);
            return delegate.countMatchingEntries(filter);
        }
        catch (EmptyFilterIntersection e)
        {
            return 0L;
        } 
    }


    @Override
    public IDerivedProperty get(Object key)
    {
        Asserts.checkArgument(key instanceof PropertyKey);
        
        if (viewFilter == null)
            return delegate.get(key);
        
        var pk = (PropertyKey)key;
        if (viewFilter.getInternalIDs() != null && !viewFilter.getInternalIDs().contains(pk.getInternalID()))
            return null;
        
        var proc = delegate.get(key);
        return viewFilter.test(proc) ? proc : null;
    }
    
    
    @Override
    public String getDatastoreName()
    {
        return delegate.getDatastoreName();
    }


    @Override
    public PropertyKey add(IDerivedProperty dsInfo)
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }
}
