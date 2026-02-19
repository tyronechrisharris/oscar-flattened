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

import java.awt.geom.Point2D;
import org.junit.Test;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.sensorhub.utils.ObjectUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.google.common.collect.ImmutableMap;


public class TestVersionedSerializers
{
    static {
        //System.setProperty("kryo.unsafe", "false");
    }
    
    void writeV1(TestObjectV1 obj, Output output)
    {
        var kryo = new Kryo();
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        kryo.setDefaultSerializer(new VersionedSerializerFactory(1));
        
        kryo.register(TestObjectV1.class, 10);
        kryo.register(double[].class);
        kryo.register(Point2D.Float.class);
                
        kryo.writeClassAndObject(output, obj);
    }
    
    
    void writeV2(TestObjectV2 obj, Output output)
    {
        var kryo = new Kryo();
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        kryo.setDefaultSerializer(new VersionedSerializerFactory(2));
        
        kryo.register(TestObjectV2.class, 10);
        kryo.register(double[].class);
        kryo.register(Point2D.Float.class);
                
        kryo.writeClassAndObject(output, obj);
    }
    
    
    TestObjectV1 readToV1(Input input)
    {
        var kryo = new Kryo();
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        kryo.setDefaultSerializer(new VersionedSerializerFactory(1));
        
        kryo.register(TestObjectV1.class, 10);
        kryo.register(double[].class);
        kryo.register(Point2D.Float.class);
        
        return (TestObjectV1)kryo.readClassAndObject(input);
    }
    
    
    TestObjectV2 readToV2(Input input)
    {
        var kryo = new Kryo();
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        kryo.setDefaultSerializer(new VersionedSerializerFactory(2));
        
        // configure compatibility serializer
        var v1CompatSerializer = new VersionedSerializer<TestObjectV2>(
            ImmutableMap.<Integer, Serializer<TestObjectV2>>builder()
                .put(1, new TestObjectV1CompatSerializer(kryo))
                .put(2, new FieldSerializer<TestObjectV2>(kryo, TestObjectV2.class))
                .build(), 2);
        
        kryo.addDefaultSerializer(TestObjectV2.class, v1CompatSerializer);
        
        kryo.register(TestObjectV2.class, 10);
        kryo.register(double[].class);
        kryo.register(Point2D.Float.class);
        
        return (TestObjectV2)kryo.readClassAndObject(input);
    }
    
    
    @Test
    public void testWriteV1ReadV2()
    {
        System.out.println();
        System.out.println("-----------------------");
        
        Output output = new Output(1024, 1024);
        var obj = new TestObjectV1();
        writeV1(obj, output);
        
        Input input = new Input(output.getBuffer());
        var objv1 = readToV1(input);
        System.out.println(ObjectUtils.toString(objv1, true));
        
        input.setPosition(0);
        var objv2 = readToV2(input);
        System.out.println(ObjectUtils.toString(objv2, true));
    }
    
    
    @Test
    public void testWriteV1V2ReadV2()
    {
        System.out.println();
        System.out.println("-----------------------");
        
        Output output = new Output(1024, 1024);
        var obj1 = new TestObjectV1();
        writeV1(obj1, output);
        var obj2 = new TestObjectV2();
        writeV2(obj2, output);
        
        Input input = new Input(output.getBuffer());
        for (int i=0; i<2;i++)
        {
            var objv2 = readToV2(input);
            System.out.println(ObjectUtils.toString(objv2, true));
        }
    }

}
