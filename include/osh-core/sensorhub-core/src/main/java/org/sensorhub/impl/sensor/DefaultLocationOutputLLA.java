/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.

******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.sensor;

import net.opengis.swe.v20.DataBlock;
import org.sensorhub.api.sensor.ISensorDriver;
import org.sensorhub.api.data.DataEvent;
import org.vast.swe.SWEConstants;
import org.vast.swe.helper.GeoPosHelper;


/**
 * <p>
 * Default WGS84 location output with latitude, longitude, altitude coordinates.
 * Use of this class is highly recommended even for static sensors.
 * </p>
 *
 * @author Alex Robin
 * @since May 19, 2015
 */
public class DefaultLocationOutputLLA extends DefaultLocationOutput
{

    public DefaultLocationOutputLLA(ISensorDriver parentSensor, String sensorFrameID, double updatePeriod)
    {
        super(parentSensor, updatePeriod);

        GeoPosHelper fac = new GeoPosHelper();

        outputStruct = fac.createRecord()
            .label("Sensor Location")
            .addSamplingTimeIsoUTC("time")
            .addField("location", fac.createVector()
                .from(fac.newLocationVectorLLA(SWEConstants.DEF_SENSOR_LOC))
                .localFrame('#' + sensorFrameID)
                .build())
            .build();

        outputStruct.setName(getName());
        outputStruct.setId(AbstractSensorModule.LOCATION_OUTPUT_ID);
        outputEncoding = fac.newTextEncoding();
    }


    @Override
    public void updateLocation(double time, double x, double y, double z, boolean forceUpdate)
    {
        // build new datablock
        DataBlock dataBlock = (latestRecord == null) ? outputStruct.createDataBlock() : latestRecord.renew();
        dataBlock.setDoubleValue(0, time);
        dataBlock.setDoubleValue(1, y);
        dataBlock.setDoubleValue(2, x);
        dataBlock.setDoubleValue(3, z);
        
        var locationChanged = forceUpdate || latestRecord == null ||
            latestRecord.getDoubleValue(1) != dataBlock.getDoubleValue(1) ||
            latestRecord.getDoubleValue(2) != dataBlock.getDoubleValue(2) ||
            latestRecord.getDoubleValue(3) != dataBlock.getDoubleValue(3);

        // if location has actually changed, update latest record and send event
        if (locationChanged)
        {
            latestRecord = dataBlock;
            latestRecordTime = System.currentTimeMillis();
            eventHandler.publish(new DataEvent(latestRecordTime, this, dataBlock));
        }
    }

}
