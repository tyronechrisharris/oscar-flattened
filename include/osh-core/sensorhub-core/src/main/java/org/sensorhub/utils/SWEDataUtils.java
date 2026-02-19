/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.utils;

import java.time.Instant;
import org.vast.swe.SWEConstants;
import org.vast.swe.SWEHelper;
import org.vast.swe.ScalarIndexer;
import org.vast.util.Asserts;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.ScalarComponent;
import net.opengis.swe.v20.Vector;


/**
 * <p>
 * Helper class to accessand write SWE Common data stuctures and records
 * </p>
 *
 * @author Alex Robin
 * @since Jun 10, 2019
 */
public class SWEDataUtils
{    
    
    public static class VectorIndexer
    {
        int numDims;
        ScalarIndexer comp1Indexer;
        
        public VectorIndexer(DataComponent rootComponent, Vector target)
        {
            numDims = target.getComponentCount();
            comp1Indexer = new ScalarIndexer(rootComponent, target.getCoordinateList().get(0)); 
        }
        
        public double getCoordinateAsDouble(int i, DataBlock dataBlk)
        {
            Asserts.checkElementIndex(i, numDims);
            int offset = comp1Indexer.getDataIndex(dataBlk);
            return dataBlk.getDoubleValue(offset+i);
        }
        
        public double getCoordinateAsFloat(int i, DataBlock dataBlk)
        {
            Asserts.checkElementIndex(i, numDims);
            int offset = comp1Indexer.getDataIndex(dataBlk);
            return dataBlk.getFloatValue(offset+i);
        }
    }    
    
    
    /**
     * Retrieves an indexer for the first time stamp component found in the parent structure
     * @param parent
     * @return indexer instance for the time stamp component or null if none could be found
     */
    public static ScalarIndexer getTimeStampIndexer(DataComponent parent)
    {
        ScalarComponent timeStamp = (ScalarComponent)SWEHelper.findComponentByDefinition(parent, SWEConstants.DEF_SAMPLING_TIME);
        if (timeStamp == null)
            timeStamp = (ScalarComponent)SWEHelper.findComponentByDefinition(parent, SWEConstants.DEF_PHENOMENON_TIME);
        if (timeStamp == null)
            return null;
        return new ScalarIndexer(parent, timeStamp);
    }
    
        
    /**
     * Retrieves a vector indexer for the first location component found in the parent structure
     * @param parent
     * @return indexer instance for the location vector component or null if none could be found
     */
    public static VectorIndexer getLocationIndexer(DataComponent parent)
    {
        Vector locVector = (Vector)SWEHelper.findComponentByDefinition(parent, SWEConstants.DEF_SAMPLING_LOC);
        if (locVector == null)
            locVector = (Vector)SWEHelper.findComponentByDefinition(parent, SWEConstants.DEF_SENSOR_LOC);
        if (locVector == null)
            locVector = (Vector)SWEHelper.findComponentByDefinition(parent, SWEConstants.DEF_PLATFORM_LOC);
        
        if (locVector == null)
            return null;
        return new VectorIndexer(parent, locVector);
    }
    
    
    /**
     * Convert a SWE time stamp (julian time in seconds past 1970-01-01) to a Java 8 Instant
     * @param julianTime The time stamp as a double
     * @return The instant object (UTC time zone)
     */
    public static Instant toInstant(double julianTime)
    {
        long epochSeconds = (long)julianTime;
        // improve rounding error by pre-multiplying by 1000
        int nanos = (int)((julianTime*1000. - epochSeconds*1000L)*1e6);
        return Instant.ofEpochSecond(epochSeconds, nanos);
    }
    
    
    public static String toNCName(String str)
    {
        return str.toLowerCase().replaceAll("\\s+", "_");
    }
}
