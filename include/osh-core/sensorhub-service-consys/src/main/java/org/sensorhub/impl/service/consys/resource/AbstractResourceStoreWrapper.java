/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.resource;

import org.sensorhub.api.datastore.DataStoreException;
import org.sensorhub.api.datastore.resource.IResourceStore;
import org.sensorhub.api.datastore.resource.IResourceStore.ResourceField;
import org.sensorhub.api.resource.ResourceFilter;
import org.sensorhub.api.resource.ResourceFilter.ResourceFilterBuilder;
import org.sensorhub.impl.service.consys.AbstractDataStoreWrapper;
import org.sensorhub.api.resource.ResourceKey;
import org.vast.util.IResource;


public abstract class AbstractResourceStoreWrapper<
    K extends ResourceKey<K>,
    V extends IResource,
    VF extends ResourceField,
    F extends ResourceFilter<? super V>,
    S extends IResourceStore<K, V, VF, F>>
    extends AbstractDataStoreWrapper<K, V, VF, F, S> implements IResourceStore<K, V, VF, F>
{

    protected AbstractResourceStoreWrapper(S readStore, S writeStore)
    {
        super(readStore, writeStore);
    }
    
    
    @Override
    public K add(V value) throws DataStoreException
    {
        return getWriteStore().add(value);
    }


    @Override
    public ResourceFilterBuilder<?,?,F> filterBuilder()
    {
        return getReadStore().filterBuilder();
    }

}
