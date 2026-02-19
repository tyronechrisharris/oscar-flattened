/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import net.opengis.gml.v32.AbstractGeometry;
import net.opengis.gml.v32.LineString;
import net.opengis.gml.v32.LinearRing;
import net.opengis.gml.v32.Point;
import net.opengis.gml.v32.Polygon;
import net.opengis.gml.v32.impl.GMLFactory;


/**
 * <p>
 * Kryo serializer/deserializer for GML/JTS geometry objects.<br/>
 * </p>
 *
 * @author Alex Robin
 * @date Dec 2, 2020
 */
public class GeomSerializer extends Serializer<AbstractGeometry>
{
    static final String UNKNOWN_GEOM_ERROR = "Unsupported geometry type: ";
    static enum GeomType {NULL, POINT, LINESTRING, POLYGON}
    
    GMLFactory gmlFactory = new GMLFactory(true);
    
    
    public GeomSerializer()
    {
    }
    
    
    @Override
    public void write(Kryo kryo, Output output, AbstractGeometry g)
    {
        // geom type
        if (g == null)
        {
            output.writeByte(GeomType.NULL.ordinal());
        }
        else
        {
            if (g instanceof Point)
            {
                output.writeByte(GeomType.POINT.ordinal());
                writePosList(output, ((Point) g).getPos());
            }
            else if (g instanceof LineString)
            {
                output.writeByte(GeomType.LINESTRING.ordinal());
                writePosList(output, ((LineString) g).getPosList());
            }
            else if (g instanceof Polygon)
            {
                output.writeByte(GeomType.POLYGON.ordinal());
                // exterior
                double[] posList = ((Polygon) g).getExterior().getPosList();
                writePosList(output, posList);
                // holes
                int numHoles = ((Polygon) g).getNumInteriors();
                output.writeVarInt(numHoles, true);
                if (numHoles > 0)
                {
                    for (LinearRing hole: ((Polygon) g).getInteriorList())
                        writePosList(output, hole.getPosList());
                }
            }
            else
                throw new IllegalStateException(UNKNOWN_GEOM_ERROR + g.getClass().getCanonicalName());
            
            // write SRS name and dimensionality
            output.writeString(g.getSrsName());
            output.writeByte(g.getSrsDimension());
        }
    }
    
    
    @Override
    public AbstractGeometry read(Kryo kryo, Input input, Class<? extends AbstractGeometry> type)
    {
        int geomTypeIndex = input.readByte();
        if (geomTypeIndex == GeomType.NULL.ordinal())
            return null;
        if (geomTypeIndex > GeomType.values().length)
            throw new IllegalStateException(UNKNOWN_GEOM_ERROR + geomTypeIndex);
                
        var geomType = GeomType.values()[geomTypeIndex];
        AbstractGeometry g = null;
        switch (geomType)
        {
            case POINT:
                g = gmlFactory.newPoint();
                ((Point)g).setPos(readPosList(input));
                break;
                
            case LINESTRING:
                g = gmlFactory.newLineString();
                ((LineString)g).setPosList(readPosList(input));
                break;
                
            case POLYGON:
                g = gmlFactory.newPolygon();
                ((Polygon)g).setExterior(readLinearRing(input));
                int numHoles = input.readVarInt(true);
                for (int i=0; i<numHoles;i++)
                    ((Polygon)g).addInterior(readLinearRing(input));
                break;
                
            default:
                throw new IllegalStateException(UNKNOWN_GEOM_ERROR + geomType);
        }
        
        // SRS name and dimensionality
        var srsName = input.readString();
        if (srsName != null)
            g.setSrsName(srsName);
        
        var srsDims = input.readByte();
        if (srsDims != 0)
            g.setSrsDimension(srsDims);
        
        return g;
    }
    
    
    protected void writePosList(Output output, double[] posList)
    {
        output.writeVarInt(posList.length, true);
        output.writeDoubles(posList, 0, posList.length);
    }
    
    
    protected double[] readPosList(Input input)
    {
        int l = input.readVarInt(true);
        return input.readDoubles(l);
    }
    
    
    protected LinearRing readLinearRing(Input input)
    {
        LinearRing ring = gmlFactory.newLinearRing();
        ring.setPosList(readPosList(input));
        return ring;
    }
}
