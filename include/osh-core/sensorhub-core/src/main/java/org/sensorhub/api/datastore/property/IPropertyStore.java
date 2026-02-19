/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore.property;

import org.sensorhub.api.datastore.property.IPropertyStore.PropertyField;
import org.sensorhub.api.datastore.resource.IResourceStore;
import org.sensorhub.api.datastore.resource.IResourceStore.ResourceField;
import org.sensorhub.api.semantic.IDerivedProperty;


/**
 * <p>
 * Interface for data stores containing derived property definitions
 * </p>
 *
 * @author Alex Robin
 * @since June 22, 2023
 */
public interface IPropertyStore extends IResourceStore<PropertyKey, IDerivedProperty, PropertyField, PropertyFilter>
{
    
    public static class PropertyField extends ResourceField
    {
        public static final PropertyField BASE_PROPERTY = new PropertyField("baseProperty");
        public static final PropertyField OBJECT = new PropertyField("object");
        public static final PropertyField STATISTIC = new PropertyField("statistic");
        public static final PropertyField QUALIFIERS = new PropertyField("qualifiers");
        
        public PropertyField(String name)
        {
            super(name);
        }
    }
    
    
    @Override
    public default PropertyFilter.Builder filterBuilder()
    {
        return new PropertyFilter.Builder();
    }
    
}
