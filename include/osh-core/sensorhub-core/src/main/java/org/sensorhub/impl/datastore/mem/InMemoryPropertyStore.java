/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUObsData WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.

******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.mem;

import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.DataStoreException;
import org.sensorhub.api.datastore.IdProvider;
import org.sensorhub.api.datastore.property.IPropertyStore;
import org.sensorhub.api.datastore.property.PropertyFilter;
import org.sensorhub.api.datastore.property.PropertyKey;
import org.sensorhub.api.datastore.property.IPropertyStore.PropertyField;
import org.sensorhub.api.semantic.IDerivedProperty;
import org.sensorhub.impl.datastore.DataStoreUtils;
import org.sensorhub.utils.CallbackException;
import static org.sensorhub.utils.Lambdas.*;


/**
 * <p>
 * In-memory implementation of a property store backed by a {@link NavigableMap}.
 * </p>
 *
 * @author Alex Robin
 * @date Sep 28, 2019
 */
public class InMemoryPropertyStore
    extends InMemoryResourceStore<PropertyKey, IDerivedProperty, PropertyField, PropertyFilter> implements IPropertyStore
{
    final NavigableMap<String, PropertyKey> uidMap = new ConcurrentSkipListMap<>();
    final IdProvider<IDerivedProperty> idProvider;


    public InMemoryPropertyStore(int idScope)
    {
        super(idScope);
        this.idProvider = DataStoreUtils.getConceptHashIdProvider(51485541);
    }
    
    
    @Override
    protected PropertyKey checkKey(Object key)
    {
        return DataStoreUtils.checkPropertyKey(key);
    }
    
    
    @Override
    protected IDerivedProperty checkValue(IDerivedProperty prop) throws DataStoreException
    {
        DataStoreUtils.checkPropertyDef(prop);
        return prop;
    }
    
    
    protected PropertyKey generateKey(IDerivedProperty prop)
    {
        var hash = idProvider.newInternalID(prop);
        return new PropertyKey(idScope, hash);
    }


    @Override
    protected PropertyKey getKey(BigId id)
    {
        return new PropertyKey(id);
    }
    
    
    @Override
    protected synchronized IDerivedProperty put(PropertyKey dsKey, IDerivedProperty dsInfo, boolean replace) throws DataStoreException
    {
        try
        {
            var prop = map.compute(dsKey, checked((k, v) -> {
                if (!replace && uidMap.containsKey(dsInfo.getURI()))
                    throw new DataStoreException(DataStoreUtils.ERROR_EXISTING_PROPERTY + dsInfo.getURI());
                return dsInfo;
            }));
            
            // also update UID index
            uidMap.put(dsInfo.getURI(), dsKey);
            return prop;
        }
        catch (CallbackException e)
        {
            throw (DataStoreException)unwrap(e);
        }
    }


    @Override
    public synchronized IDerivedProperty remove(Object key)
    {
        return map.compute(checkKey(key), (k, v) -> {
            if (v != null)
                uidMap.remove(v.getURI());
            return null;
        });
    }
}
