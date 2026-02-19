/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are copyright (C) 2018, Sensia Software LLC
 All Rights Reserved. This software is the property of Sensia Software LLC.
 It cannot be duplicated, used, or distributed without the express written
 consent of Sensia Software LLC.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore.resource;

import org.sensorhub.api.datastore.DataStoreException;
import org.sensorhub.api.datastore.IDataStore;
import org.sensorhub.api.datastore.ValueField;
import org.sensorhub.api.datastore.feature.IFeatureStoreBase.FeatureField;
import org.sensorhub.api.datastore.resource.IResourceStore.ResourceField;
import org.sensorhub.api.resource.ResourceFilter;
import org.sensorhub.api.resource.ResourceFilter.ResourceFilterBuilder;
import org.vast.util.IResource;


/**
 * <p>
 * Generic interface for all resource stores
 * </p>
 * @param <K> Key type
 * @param <V> Resource type 
 * @param <VF> Resource value field enum type
 * @param <F> Resource filter type
 *
 * @author Alex Robin
 * @date Oct 8, 2023
 */
public interface IResourceStore<K extends Comparable<? super K>, V extends IResource, VF extends ResourceField, F extends ResourceFilter<? super V>> extends IDataStore<K, V, VF, F>
{

    public static class ResourceField extends ValueField
    {
        public static final FeatureField ID = new FeatureField("id");
        public static final FeatureField NAME = new FeatureField("name");
        public static final FeatureField DESCRIPTION = new FeatureField("description");
        
        public ResourceField(String name)
        {
            super(name);
        }
    }
    
    
    /**
     * Add a new resource to the store, generating a new key for it
     * @param value New resource object
     * @return The newly allocated key (internal ID)
     * @throws DataStoreException if the resource already exists
     */
    K add(V value) throws DataStoreException;
    
    
    /**
     * @return A builder for a filter compatible with this datastore
     */
    public ResourceFilterBuilder<?,?,F> filterBuilder();
    
    
    public default F selectAllFilter()
    {
        return filterBuilder().build();
    }
}
