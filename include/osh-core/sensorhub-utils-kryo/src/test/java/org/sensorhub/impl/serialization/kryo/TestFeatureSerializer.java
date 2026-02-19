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

import static org.junit.Assert.*;
import javax.xml.namespace.QName;
import org.junit.Test;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.sensorhub.impl.serialization.kryo.compat.v1.FeatureSerializerV1;
import org.vast.ogc.gml.GenericFeatureImpl;
import org.vast.ogc.gml.IFeature;
import org.vast.ogc.om.MovingFeature;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import net.opengis.gml.v32.AbstractFeature;
import net.opengis.gml.v32.impl.GMLFactory;


public class TestFeatureSerializer
{
    
    private Kryo getKryo()
    {
        var kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setReferences(true);
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        //kryo.addDefaultSerializer(AbstractGeometry.class, new GeomSerializer());
        return kryo;
    }
    
    
    void writeReadAndCompare(IFeature f1)
    {
        var kryo = getKryo();
        var ser = new FeatureSerializerV1();
        
        // write
        var output = new Output(1024, 1024*1024);
        ser.write(kryo, output, f1);

        // read back
        var input = new Input(output.getBuffer(), 0, output.position());
        var f2 = ser.read(kryo, input, IFeature.class);
        
        // compare
        assertEquals(f1.getType(), f2.getType());
        assertEquals(f1.getUniqueIdentifier(), f2.getUniqueIdentifier());
        assertEquals(f1.getName(), f2.getName());
        assertEquals(f1.getDescription(), f2.getDescription());
        assertEquals(f1.getProperties(), f2.getProperties());
        assertEquals(f1.getGeometry(), f2.getGeometry());
        assertEquals(f1.getValidTime(), f2.getValidTime());
        
        if (f1 instanceof AbstractFeature)
        {
            assertTrue(AbstractFeature.class.isAssignableFrom(f2.getClass()));
            assertEquals(((AbstractFeature) f1).getQName(), ((AbstractFeature) f2).getQName());
        }
    }
    
    
    @Test
    public void testGenericFeature()
    {
        var f = new GenericFeatureImpl(new QName("urn:nsuri", "MyFeatureType"));
        f.setUniqueIdentifier("urn:myfeatures:GF001");
        f.setName("GF01");
        f.setDescription("gf bla bla bla");
        f.setGeometry(new GMLFactory(true).newPoint(-84, 12.3, -21.5));
        writeReadAndCompare(f);
    }
    
    
    @Test
    public void testMovingFeature()
    {
        var f = new MovingFeature();
        f.setUniqueIdentifier("urn:myfeatures:MF001");
        f.setName("MF01");
        f.setDescription("mf bla bla bla");
        writeReadAndCompare(f);
    }
    
    
    /*@Test
    public void testSamplingFeature()
    {
        var f = new SamplingPoint();
        f.setUniqueIdentifier("urn:myfeatures:SF001");
        f.setName("SF01");
        f.setDescription("sf bla bla bla");
        f.setShape(new GMLFactory(true).newPoint(45.0, 1.3, 100));
        writeReadAndCompare(f);
    }*/

}
