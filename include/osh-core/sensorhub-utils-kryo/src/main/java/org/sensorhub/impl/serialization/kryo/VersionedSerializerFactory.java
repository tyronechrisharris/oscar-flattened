/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializerFactory;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer.FieldSerializerConfig;
import com.google.common.collect.ImmutableMap;


@SuppressWarnings({ "unchecked", "rawtypes" })
public class VersionedSerializerFactory  implements SerializerFactory<VersionedSerializer>
{
    int currentVersion;
    
    
    public VersionedSerializerFactory(int currentVersion)
    {
        this.currentVersion = currentVersion;
    }
    
    
    @Override
    public VersionedSerializer newSerializer(Kryo kryo, Class type)
    {
        var defaultSerializer = new FieldSerializer(kryo, type, new FieldSerializerConfig());
        var map = ImmutableMap.of(currentVersion, defaultSerializer);
        return new VersionedSerializer(map, currentVersion);
    }
    
    
    @Override
    public boolean isSupported(Class type)
    {
        return true;
    }

}
