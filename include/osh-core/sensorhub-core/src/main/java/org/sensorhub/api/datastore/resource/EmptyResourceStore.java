/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2023 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore.resource;

import org.sensorhub.api.datastore.DataStoreException;
import org.sensorhub.api.datastore.resource.IResourceStore.ResourceField;
import org.sensorhub.api.resource.ResourceFilter;
import org.sensorhub.impl.datastore.EmptyDataStore;
import org.vast.util.IResource;


/**
 * <p>
 * Helper class to implement databases that don't support all datastores
 * </p>
 * 
 * @param <K> Key type
 * @param <V> Resource type 
 * @param <VF> Resource value field enum type
 * @param <F> Resource filter type
 * 
 * @author Alex Robin
 * @since Jun 22, 2023
 */
public abstract class EmptyResourceStore<K extends Comparable<? super K>, V extends IResource, VF extends ResourceField, F extends ResourceFilter<? super V>>
    extends EmptyDataStore<K, V, VF, F> implements IResourceStore<K, V, VF, F>
{
    @Override
    public K add(V value) throws DataStoreException
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }
}
