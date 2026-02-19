/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.serialization.kryo.compat.v1;

import javax.xml.namespace.QName;
import org.vast.ogc.gml.GenericTemporalFeatureImpl;
import org.vast.ogc.gml.IFeature;
import org.vast.util.TimeExtent;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import net.opengis.gml.v32.AbstractGeometry;
import net.opengis.gml.v32.impl.GMLFactory;


/**
 * <p>
 * Kryo serializer/deserializer for Feature objects.<br/>
 * </p>
 *
 * @author Alex Robin
 * @date Dec 2, 2020
 */
public class FeatureSerializerV1 extends Serializer<IFeature>
{
    static final int VERSION = 1;
    
    GMLFactory gmlFactory = new GMLFactory(true);
    
    
    public FeatureSerializerV1()
    {
    }
    
    
    @Override
    public void write(Kryo kryo, Output output, IFeature f)
    {
        //output.writeString(f.getId());
        output.writeString(f.getType());
        
        // common properties
        output.writeString(f.getUniqueIdentifier());
        output.writeString(f.getName());
        output.writeString(f.getDescription());
        
        // geometry
        var geom = !f.hasCustomGeomProperty() ? f.getGeometry() : null;
        kryo.writeClassAndObject(output, geom);
        
        // valid time
        var validTime = !f.hasCustomTimeProperty() ? f.getValidTime() : null;
        kryo.writeClassAndObject(output, validTime);
        
        // properties
        output.writeVarInt(f.getProperties().size(), true);
        for (var prop: f.getProperties().entrySet())
        {
            output.writeString(prop.getKey().toString());
            kryo.writeClassAndObject(output, prop.getValue());
        }
    }
    
    
    @Override
    public IFeature read(Kryo kryo, Input input, Class<? extends IFeature> type)
    {
        String fType = input.readString();
        var f = new GenericTemporalFeatureImpl(fType);
        
        // common properties
        f.setUniqueIdentifier(input.readString());
        f.setName(input.readString());
        f.setDescription(input.readString());
        
        // geom
        var geom = (AbstractGeometry)kryo.readClassAndObject(input);
        if (geom != null)
            f.setGeometry(geom);
        
        // valid time
        var validTime = (TimeExtent)kryo.readClassAndObject(input);
        if (validTime != null)
            f.setValidTime(validTime);
        
        // properties
        int numProperties = input.readVarInt(true);
        for (int i = 0; i < numProperties; i++)
        {
            var qName = QName.valueOf(input.readString());
            var value = kryo.readClassAndObject(input);
            f.setProperty(qName, value);
        }
        
        return f;
    }
}
