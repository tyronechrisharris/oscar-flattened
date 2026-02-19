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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.util.BaseBuilder;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.SerializerFactory;
import com.esotericsoftware.kryo.SerializerFactory.BaseSerializerFactory;
import com.esotericsoftware.kryo.SerializerFactory.SingletonSerializerFactory;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.ImmutableMap;


public class VersionedSerializer<T> extends Serializer<T>
{
    static Logger log = LoggerFactory.getLogger(VersionedSerializer.class);
    
    final SerializerEntry[] serializers;
    final int currentVersion;
    final Serializer<T> currentVersionSerializer;
    
    @SuppressWarnings("rawtypes")
    static class SerializerEntry
    {
        int version;
        Serializer serializer;
    }
    
    
    public VersionedSerializer(Map<Integer, ? extends Serializer<T>> serializers, int currentVersion)
    {
        this.currentVersion = currentVersion;
        this.currentVersionSerializer = serializers.get(currentVersion);
        
        if (currentVersionSerializer == null)
            throw new IllegalArgumentException("A serializer must be provided for the current version");
        
        // create list of serializers sorted by version
        // do it only if we have older version serializers
        if (serializers.size() > 1)
        {
            this.serializers = new SerializerEntry[serializers.size()];
            var it = serializers.entrySet().iterator();
            int i = 0;
            while (it.hasNext())
            {
                var mapEntry = it.next();
                if (mapEntry.getKey() > currentVersion)
                    throw new IllegalArgumentException("Serializer version cannot be greater than current version");
                    
                var arrayEntry = this.serializers[i++] = new SerializerEntry();
                arrayEntry.version = mapEntry.getKey();
                arrayEntry.serializer = mapEntry.getValue();
            }
            Arrays.sort(this.serializers, (a,b) -> Integer.compare(a.version, b.version));
        }
        else
            this.serializers = null;
    }
    
    
    @Override
    public void write(Kryo kryo, Output output, T object)
    {
        if (log.isTraceEnabled())
            log.trace("Writing {}, version {}", object.getClass(), currentVersion);
        output.writeVarInt(currentVersion, true);
        currentVersionSerializer.write(kryo, output, object);
    }
    

    @Override
    @SuppressWarnings("unchecked")
    public T read(Kryo kryo, Input input, Class<? extends T> type)
    {
        int readVersion = input.readVarInt(true);
        if (readVersion > currentVersion)
            throw new IllegalStateException("Invalid class version: " + readVersion + " > currentVersion");
        
        Serializer<T> reader = currentVersionSerializer;
        if (readVersion < currentVersion && serializers != null)
        {
            // scan list of serializers to find the one compatible with the read version
            // i.e. the 1st with version greater or equal to the read version
            for (int i = serializers.length-2; i >= 0; i--)
            {
                var prevEntry = serializers[i];
                if (prevEntry.version >= readVersion)
                    reader = prevEntry.serializer;
                if (prevEntry.version <= readVersion)
                    break;
            }
        }
        
        if (log.isTraceEnabled())
            log.trace("Reading {}, version {}", type, readVersion);
        return reader.read(kryo, input, type);
    }
    
    
    public static <T> SerializerFactory<VersionedSerializer<T>>factory(Map<Integer, ? extends Serializer<T>> serializers, int currentVersion)
    {
        return new SingletonSerializerFactory<>(new VersionedSerializer<T>(serializers, currentVersion));
    }
    
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> SerializerFactory<VersionedSerializer<T>>factory2(Map<Integer, ? extends SerializerFactory<? extends Serializer>> factories, int currentVersion)
    {
        return new BaseSerializerFactory<>() {
            public VersionedSerializer<T> newSerializer(Kryo kryo, Class type)
            {
                // create serializer map
                var mapBuilder = ImmutableMap.<Integer, Serializer<T>>builder();
                factories.forEach((version, fac) -> {
                    var serializer = fac.newSerializer(kryo, type);
                    mapBuilder.put(version, serializer);
                });
                var serializers = mapBuilder.build();
                
                if (log.isTraceEnabled())
                {
                    var buf = new StringBuilder();
                    for (var s: serializers.entrySet())
                        buf.append("Version " + s.getKey() + " -> " + s.getValue().getClass());
                    log.trace("Serializers for " + type + ": " + buf.toString());
                }
                
                return new VersionedSerializer<T>(serializers, currentVersion);
            }
        };
    }
    
    
    public static <T> VersionedSerializerFactoryBuilder<T> factory(int currentVersion)
    {
        return new VersionedSerializerFactoryBuilder<>(currentVersion);
    }
    
    
    public static class VersionedSerializerFactoryBuilder<T> extends BaseBuilder<SerializerFactory<VersionedSerializer<T>>>
    {
        @SuppressWarnings("rawtypes")
        Map<Integer, SerializerFactory> factories = new HashMap<>();
        int currentVersion;
        
        VersionedSerializerFactoryBuilder(int currentVersion)
        {
            this.currentVersion = currentVersion;
        }
        
        public VersionedSerializerFactoryBuilder<T> put(int version, SerializerFactory<? extends Serializer<T>> fac)
        {
            factories.put(version, fac);
            return this;
        }
        
        public VersionedSerializerFactoryBuilder<T> put(int version, Serializer<T> ser)
        {
            factories.put(version, new SingletonSerializerFactory<>(ser));
            return this;
        }
        
        @Override
        public SerializerFactory<VersionedSerializer<T>> build()
        {
            this.instance = new BaseSerializerFactory<>() {
                @SuppressWarnings({ "rawtypes", "unchecked" })
                public VersionedSerializer<T> newSerializer(Kryo kryo, Class type)
                {
                    // add default serializer if none was set for current version
                    factories.putIfAbsent(currentVersion, new FieldSerializerFactory());
                    
                    // create serializer map
                    var mapBuilder = ImmutableMap.<Integer, Serializer<T>>builder();
                    factories.forEach((version, fac) -> {
                        var serializer = fac.newSerializer(kryo, type);
                        mapBuilder.put(version, serializer);
                    });
                    var serializers = mapBuilder.build();
                    
                    if (log.isTraceEnabled())
                    {
                        var buf = new StringBuilder();
                        for (var s: serializers.entrySet())
                            buf.append("Version " + s.getKey() + " -> " + s.getValue().getClass());
                        log.trace("Serializers for " + type + ": " + buf.toString());
                    }
                    
                    return new VersionedSerializer<>(serializers, currentVersion);
                }
            };
            
            return super.build();
        }
    }

}
