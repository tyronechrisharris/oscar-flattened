/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.property;

import org.sensorhub.api.datastore.property.IPropertyStore;
import org.sensorhub.api.datastore.property.IPropertyStore.PropertyField;
import org.sensorhub.api.datastore.property.PropertyFilter;
import org.sensorhub.api.datastore.property.PropertyKey;
import org.sensorhub.api.semantic.IDerivedProperty;
import org.sensorhub.impl.service.consys.resource.AbstractResourceStoreWrapper;


public class PropertyStoreWrapper extends AbstractResourceStoreWrapper<PropertyKey, IDerivedProperty, PropertyField, PropertyFilter, IPropertyStore> implements IPropertyStore
{
    
    public PropertyStoreWrapper(IPropertyStore readStore, IPropertyStore writeStore)
    {
        super(readStore, writeStore);
    }
    
    
    @Override
    public PropertyFilter.Builder filterBuilder()
    {
        return (PropertyFilter.Builder)super.filterBuilder();
    }

}
