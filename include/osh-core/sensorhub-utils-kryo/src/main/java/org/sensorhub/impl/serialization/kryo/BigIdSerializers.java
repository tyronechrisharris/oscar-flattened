/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.serialization.kryo;

import java.util.Arrays;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.common.BigIdLong;
import org.sensorhub.api.common.BigIdZero;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.SerializerFactory;
import com.esotericsoftware.kryo.SerializerFactory.BaseSerializerFactory;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.ByteArraySerializer;


public class BigIdSerializers
{
    private BigIdSerializers() {}
    
    
    static class BigIdBytesSerializer extends Serializer<BigId>
    {
        ByteArraySerializer byteArraySerializer = new ByteArraySerializer();
        int idScope;
        
        
        public BigIdBytesSerializer(int idScope)
        {
            this.idScope = idScope;
        }
        
        
        @Override
        public void write(Kryo kryo, Output output, BigId id)
        {
            byteArraySerializer.write(kryo, output, id.getIdAsBytes());
        }
        
        
        @Override
        public BigId read(Kryo kryo, Input input, Class<? extends BigId> type)
        {
            var bytes = byteArraySerializer.read(kryo, input, null);
            if (Arrays.equals(bytes, BigId.NONE.getIdAsBytes()))
                return BigId.NONE;
            else
                return BigId.fromBytes(idScope, bytes);
        }
    }
    
    
    static class BigIdLongSerializer extends Serializer<BigId>
    {
        int idScope;
        
        
        public BigIdLongSerializer(int idScope)
        {
            this.idScope = idScope;
        }
        
        
        @Override
        public void write(Kryo kryo, Output output, BigId id)
        {
            output.writeVarLong(id.getIdAsLong(), false);
        }
        
        
        @Override
        public BigId read(Kryo kryo, Input input, Class<? extends BigId> type)
        {
            var id = input.readVarLong(false);
            return id <= 0 ? BigId.NONE : BigId.fromLong(idScope, id);
        }
    }
    
    
    static class BigIdZeroSerializer extends Serializer<BigId>
    {
        @Override
        public void write(Kryo kryo, Output output, BigId id)
        {
            // nothing to write, just class ID is enough
        }
        
        
        @Override
        public BigId read(Kryo kryo, Input input, Class<? extends BigId> type)
        {
            return BigId.NONE;
        }
    }
    
    
    public static SerializerFactory<Serializer<BigId>> factory(int idScope)
    {
        return new BaseSerializerFactory<>() {
            @SuppressWarnings({ "rawtypes" })
            public Serializer<BigId> newSerializer(Kryo kryo, Class type)
            {
                if (type.equals(BigIdLong.class))
                    return new BigIdLongSerializer(idScope);
                else if (type.equals(BigIdZero.class))
                    return new BigIdZeroSerializer();
                else
                    return new BigIdBytesSerializer(idScope);
            }
        };
    }
}
