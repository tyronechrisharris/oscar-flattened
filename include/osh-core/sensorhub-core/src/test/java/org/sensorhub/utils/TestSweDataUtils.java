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

import static org.junit.Assert.*;
import org.junit.Test;
import org.sensorhub.utils.SWEDataUtils.VectorIndexer;
import org.vast.swe.SWEConstants;
import org.vast.swe.ScalarIndexer;
import org.vast.swe.helper.GeoPosHelper;
import org.vast.swe.helper.VectorHelper;
import org.vast.util.DateTimeFormat;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataRecord;


public class TestSweDataUtils
{

    private void checkTimeStampIndexer(DataComponent rootComponent, int timeIndex) throws Exception
    {
        // generate data block
        double timeStamp = new DateTimeFormat().parseIso("2019-06-11Z");
        DataBlock data = rootComponent.createDataBlock();
        data.setDoubleValue(timeIndex, timeStamp);

        ScalarIndexer indexer = SWEDataUtils.getTimeStampIndexer(rootComponent);
        assertEquals(timeStamp, indexer.getDoubleValue(data), 0.0);
    }


    @Test
    public void testTimeStampIndexerFirst() throws Exception
    {
        VectorHelper fac = new VectorHelper();
        DataRecord rec = fac.createRecord()
            .addSamplingTimeIsoUTC("ts")
            .addField("vec", fac.newLocationVectorXYZ("def", "crs", "km"))
            .build();
        checkTimeStampIndexer(rec, 0);
    }


    @Test
    public void testTimeStampIndexerLast() throws Exception
    {
        VectorHelper fac = new VectorHelper();
        DataRecord rec =fac.createRecord()
            .addField("vec", fac.newLocationVectorXYZ("def", "crs", "km"))
            .addField("time", fac.createTime()
                .asPhenomenonTimeIsoUTC()
                .build())
            .build();
        checkTimeStampIndexer(rec, 3);
    }


    private void checkVectorIndexer(DataComponent rootComponent, int locStartIndex, int numDims) throws Exception
    {
        // generate data block
        DataBlock data = rootComponent.createDataBlock();
        for (int i=0; i<numDims; i++)
            data.setDoubleValue(locStartIndex+i, 10.0+i);

        VectorIndexer indexer = SWEDataUtils.getLocationIndexer(rootComponent);
        for (int i=0; i<numDims; i++)
            assertEquals(10.0+i, indexer.getCoordinateAsDouble(i, data), 0.0);
    }


    @Test
    public void testLocationIndexerSamplingPoint2D() throws Exception
    {
        GeoPosHelper fac = new GeoPosHelper();
        DataRecord rec = fac.createRecord()
            .addField("time", fac.createTime()
                .asPhenomenonTimeIsoUTC()
                .build())
            .addField("loc", fac.newLocationVectorLatLon(SWEConstants.DEF_SAMPLING_LOC))
            .build();
        checkVectorIndexer(rec, 1, 2);
    }


    @Test
    public void testLocationIndexerSamplingPoint3D() throws Exception
    {
        GeoPosHelper fac = new GeoPosHelper();
        DataRecord rec = fac.createRecord()
            .addField("time", fac.createTime()
                .asPhenomenonTimeIsoUTC()
                .build())
            .addField("loc", fac.newLocationVectorLLA(SWEConstants.DEF_SAMPLING_LOC))
            .build();
        checkVectorIndexer(rec, 1, 3);
    }


    @Test
    public void testLocationIndexerSensorLoc2D() throws Exception
    {
        GeoPosHelper fac = new GeoPosHelper();
        DataRecord rec = fac.createRecord()
            .addField("time", fac.createTime()
                .asPhenomenonTimeIsoUTC()
                .build())
            .addField("loc", fac.newLocationVectorLatLon(SWEConstants.DEF_SENSOR_LOC))
            .build();
        checkVectorIndexer(rec, 1, 2);
    }


    @Test
    public void testLocationIndexerSensorLoc3D() throws Exception
    {
        GeoPosHelper fac = new GeoPosHelper();
        DataRecord rec = fac.createRecord()
            .addField("time", fac.createTime()
                .asPhenomenonTimeIsoUTC()
                .build())
            .addField("loc", fac.newLocationVectorLLA(SWEConstants.DEF_SENSOR_LOC))
            .build();
        checkVectorIndexer(rec, 1, 3);
    }

}
