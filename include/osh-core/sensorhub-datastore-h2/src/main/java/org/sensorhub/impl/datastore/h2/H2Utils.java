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

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Map;
import org.h2.mvstore.DataUtils;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.WriteBuffer;
import org.h2.mvstore.rtree.SpatialKey;
import org.sensorhub.api.datastore.TemporalFilter;
import org.sensorhub.impl.datastore.h2.kryo.KryoDataType;
import org.vast.util.Asserts;
import com.google.common.collect.Range;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import net.opengis.gml.v32.AbstractGeometry;
import net.opengis.gml.v32.LineString;
import net.opengis.gml.v32.Point;
import net.opengis.gml.v32.Polygon;


public class H2Utils
{
    public static final int CURRENT_VERSION = 5;
    
    static final String DATASTORES_MAP_NAME = "@datastores";
    static final String GEOM_DIM_ERROR = "Only 2D and 3D geometries are supported";
    
    public static final Range<Instant> ALL_TIMES_RANGE = Range.closed(Instant.MIN, Instant.MAX);
    public static final TemporalFilter ALL_TIMES_FILTER = new TemporalFilter.Builder()
                                                                    .withRange(Instant.MIN, Instant.MAX)
                                                                    .build();
    
    static class Holder<T>
    {
        public T value;
    }
    
    
    static class StoreInfoDataType extends KryoDataType
    {
        StoreInfoDataType()
        {
            this.configurator = kryo -> {
                
                // pre-register known types with Kryo
                kryo.register(MVDataStoreInfo.class, 20);
            };
        }
    }
    
    
    public static MVDataStoreInfo getDataStoreInfo(MVStore mvStore, String dataStoreName)
    {
        Asserts.checkNotNull(mvStore, MVStore.class);
        Asserts.checkNotNull(dataStoreName, "dataStoreName");
        
        // load datastore info
        Map<String, MVDataStoreInfo> dataStoresMap = mvStore.openMap(DATASTORES_MAP_NAME, new MVMap.Builder<String, MVDataStoreInfo>()
                .valueType(new StoreInfoDataType()));
        return dataStoresMap.get(dataStoreName);
    }
    
    
    public static void addDataStoreInfo(MVStore mvStore, MVDataStoreInfo dataStoreInfo)
    {
        Asserts.checkNotNull(mvStore, MVStore.class);
        Asserts.checkNotNull(dataStoreInfo, MVDataStoreInfo.class);
        
        Map<String, MVDataStoreInfo> dataStoresMap = mvStore.openMap(DATASTORES_MAP_NAME, 
                new MVMap.Builder<String, MVDataStoreInfo>()
                         .valueType(new StoreInfoDataType()));
        
        if (dataStoresMap.containsKey(dataStoreInfo.name))
            throw new IllegalStateException("Data store " + dataStoreInfo.name + " already exists");
        
        dataStoresMap.put(dataStoreInfo.name, dataStoreInfo);
    }
    
    
    public static void writeAsciiString(WriteBuffer wbuf, String s)
    {
        if (s == null)
        {
            wbuf.putVarInt(0);
            return;
        }
        
        // write length
        wbuf.putVarInt(s.length());
        
        // write ASCII chars (max 255 for ASCII)
        for (int i = 0; i < s.length(); i++)
        {
            int c = s.charAt(i);
            wbuf.put((byte)c);
        }
    }
    
    
    public static String readAsciiString(ByteBuffer buf)
    {
        // read length
        int len = DataUtils.readVarInt(buf);
        if (len == 0)
            return null;
        
        // read ASCII chars
        char[] chars = new char[len];
        for (int i = 0; i < len; i++)
        {
            int x = buf.get() & 0xff;
            chars[i] = (char)x;
        }
        
        return new String(chars);
    }
    
    
    public static void writeUnicodeString(ByteBuffer buf, String s, int len)
    {
        DataUtils.writeVarInt(buf, len);
        DataUtils.writeStringData(buf, s, len);
    }
    
    
    public static String readUnicodeString(ByteBuffer buf)
    {
        int len = DataUtils.readVarInt(buf);
        return DataUtils.readString(buf, len);
    }
    
    
    public static void writeInstant(WriteBuffer wbuf, Instant instant)
    {
        wbuf.putVarLong(instant.getEpochSecond());
        wbuf.putInt(instant.getNano());
    }
    
    
    public static int getInstantEncodedLen(Instant instant)
    {
        return DataUtils.getVarLongLen(instant.getEpochSecond()) + 4;
    }
    
    
    public static Instant readInstant(ByteBuffer buf)
    {
        long epochSeconds = DataUtils.readVarLong(buf);
        int nanos = buf.getInt();
        
        if (epochSeconds == Instant.MIN.getEpochSecond() && nanos == 0)
            return Instant.MIN;
        else if (epochSeconds == Instant.MAX.getEpochSecond() && nanos == 0)
            return Instant.MAX;
        else
            return Instant.ofEpochSecond(epochSeconds, nanos);
    }
    
    
    public static void writeTimeRange(WriteBuffer wbuf, Range<Instant> timeRange)
    {
        // flags (determines presence of range and its bounds)
        byte flags = 0;
        if (timeRange == null)
        {
            wbuf.put(flags);
            return;
        }
        
        if (timeRange.hasLowerBound())
            flags += 1;
        if (timeRange.hasUpperBound())
            flags += 2;
        wbuf.put(flags);
        
        // lower bound
        if (timeRange.hasLowerBound())
            writeInstant(wbuf, timeRange.lowerEndpoint());
        
        // upper bound
        if (timeRange.hasUpperBound())
            writeInstant(wbuf, timeRange.upperEndpoint());
    }
    
    
    public static Range<Instant> readTimeRange(ByteBuffer buf)
    {
        // read flags
        byte flags = buf.get();
        if (flags == 0)
            return null;
        
        // read bound lower and upper bounds
        Instant lower =  ((flags & 1) != 0) ? readInstant(buf) : null;
        Instant upper =  ((flags & 2) != 0) ? readInstant(buf) : null;
        
        if (upper == null)
            return Range.atLeast(lower);
        else if (lower == null)
            return Range.atMost(upper);
        else
            return Range.closed(lower, upper);
    }
    
    
    public static SpatialKey getBoundingRectangle(long id, AbstractGeometry geom)
    {
        float[] minMaxCoords = null;
        int numDims = -1;
        
        // get geom dimension if specified
        if (geom.isSetSrsDimension())
        {
            numDims = geom.getSrsDimension();
            if (numDims != 2 && numDims != 3)
                throw new IllegalArgumentException(GEOM_DIM_ERROR);
        }        
        
        // case of JTS geom
        /*if (geom instanceof Geometry)
        {
            Envelope env = ((Geometry) geom).getEnvelopeInternal();
            bboxCoords = new double[] {env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY()};
        }*/
        
        // case of points
        if (geom instanceof Point)
        {
            double[] pos = ((Point)geom).getPos();
            minMaxCoords = getBoundingRectangle(numDims, pos);
        }
        
        // case of polylines
        else if (geom instanceof LineString)
        {
            double[] posList = ((LineString)geom).getPosList();
            minMaxCoords = getBoundingRectangle(numDims, posList);
        }
        
        // case of polygons
        else if (geom instanceof Polygon)
        {
            double[] posList = ((Polygon)geom).getExterior().getPosList();
            minMaxCoords = getBoundingRectangle(numDims, posList);
        }
        
        if (minMaxCoords != null)
            return new SpatialKey(id, minMaxCoords);
        else
            return null;
    }
    
    
    public static SpatialKey getBoundingRectangle(long id, Geometry geom)
    {
        return getBoundingRectangle(id, geom.getEnvelopeInternal());
    }
    
    
    public static SpatialKey getBoundingRectangle(long id, Envelope env)
    {
        return new SpatialKey(id,
            Math.nextDown((float)env.getMinX()),
            Math.nextUp((float)env.getMaxX()),
            Math.nextDown((float)env.getMinY()),
            Math.nextUp((float)env.getMaxY()),
            0f, 0f);
    }
    
    
    public static float[] getBoundingRectangle(int numDims, double[] geomCoords)
    {
        int numPoints = geomCoords.length / numDims;
        float[] minMaxCoords = new float[6];
        minMaxCoords[4] = 0.0f;
        minMaxCoords[5] = 0.0f;
        
        // try to guess number of dimensions if not specified
        if (numDims < 2 && geomCoords.length % 2 == 0)
            numDims = 2;
        else if (numDims < 2 && geomCoords.length % 3 == 0)
            numDims = 3;
        
        int c = 0;
        for (int p = 0; p < numPoints; p++)
        {
            for (int i = 0; i < numDims; i++, c++)
            {
                double val = geomCoords[c];
                int imin = i*2;
                int imax = imin+1;
                
                float downVal = Math.nextDown((float)val);
                float upVal = Math.nextUp((float)val);
                
                if (p == 0)
                {
                    minMaxCoords[imin] = downVal;
                    minMaxCoords[imax] = upVal;
                }
                else
                {
                    if (downVal < minMaxCoords[imin])
                        minMaxCoords[imin] = downVal;
                    if (upVal > minMaxCoords[imax])
                        minMaxCoords[imax] = upVal;
                }
            }
        }
        
        return minMaxCoords;
    }
}
