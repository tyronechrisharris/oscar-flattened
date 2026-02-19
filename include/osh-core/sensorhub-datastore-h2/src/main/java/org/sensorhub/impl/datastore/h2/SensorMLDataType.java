/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.h2;

import javax.xml.namespace.QName;
import org.h2.mvstore.MVMap;
import org.sensorhub.impl.datastore.h2.kryo.KryoDataType;
import org.sensorhub.impl.datastore.h2.kryo.PersistentClassResolver;
import org.sensorhub.impl.serialization.kryo.QNameSerializer;
import org.sensorhub.impl.serialization.kryo.VersionedSerializer;
import org.sensorhub.impl.serialization.kryo.compat.v3.CapabilityListSerializerV3;
import org.sensorhub.impl.serialization.kryo.compat.v3.CharacteristicListSerializerV3;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import net.opengis.OgcPropertyList;
import net.opengis.sensorml.v20.impl.CapabilityListImpl;
import net.opengis.sensorml.v20.impl.CharacteristicListImpl;


/**
 * <p>
 * H2 DataType implementation for ISystemWithDesc objects
 * </p>
 *
 * @author Alex Robin
 * @date Apr 7, 2018
 */
public class SensorMLDataType extends KryoDataType
{
    SensorMLDataType(MVMap<String, Integer> kryoClassMap)
    {
        this.classResolver = () -> new PersistentClassResolver(kryoClassMap);
        this.configurator = kryo -> {
            kryo.addDefaultSerializer(QName.class, QNameSerializer.class);
            
            // avoid using collection serializer on OgcPropertyList because
            // the add method doesn't behave as expected
            kryo.addDefaultSerializer(OgcPropertyList.class, FieldSerializer.class);
            
            // register custom serializers w/ backward compatibility
            kryo.addDefaultSerializer(CapabilityListImpl.class,
                VersionedSerializer.<CapabilityListImpl>factory(H2Utils.CURRENT_VERSION)
                    .put(3, new CapabilityListSerializerV3(kryo))
                    .build());
            
            kryo.addDefaultSerializer(CharacteristicListImpl.class,
                VersionedSerializer.<CharacteristicListImpl>factory(H2Utils.CURRENT_VERSION)
                    .put(3, new CharacteristicListSerializerV3(kryo))
                    .build());
        };
    }
}