/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.sensor.swe;

import net.opengis.swe.v20.BinaryEncoding;
import net.opengis.swe.v20.ByteEncoding;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.data.DataEvent;
import org.sensorhub.impl.client.sos.SOSClient;
import org.sensorhub.impl.client.sos.SOSClient.StreamingListener;
import org.sensorhub.impl.client.sos.SOSClient.StreamingStopReason;
import org.sensorhub.impl.sensor.VarRateSensorOutput;
import org.vast.util.Asserts;


public class SWEVirtualSensorOutput extends VarRateSensorOutput<SWEVirtualSensor>
{
    DataComponent recordStructure;
    DataEncoding recordEncoding;
    SOSClient sosClient;
    
    
    public SWEVirtualSensorOutput(SWEVirtualSensor sensor, DataComponent recordStructure, DataEncoding recordEncoding, SOSClient sosClient)
    {
        super(recordStructure.getName(), sensor, 1.0);
        this.recordStructure = Asserts.checkNotNull(recordStructure, DataComponent.class);
        this.recordEncoding = Asserts.checkNotNull(recordEncoding, DataEncoding.class);
        this.sosClient = Asserts.checkNotNull(sosClient, SOSClient.class);
        
        // force raw binary encoding (no reason to recommend base64)
        // switching to base64 is automatic when writing or parsing from XML
        if (recordEncoding instanceof BinaryEncoding)
            ((BinaryEncoding) recordEncoding).setByteEncoding(ByteEncoding.RAW);
    }


    @Override
    public DataComponent getRecordDescription()
    {
        return recordStructure;
    }


    @Override
    public DataEncoding getRecommendedEncoding()
    {
        return recordEncoding;
    }
    
    
    public void publishNewRecord(DataBlock dataBlock)
    {
        long now = System.currentTimeMillis();
        updateSamplingPeriod(now);
        
        // publish new sensor data event
        latestRecord = dataBlock;
        latestRecordTime = now;
        eventHandler.publish(new DataEvent(latestRecordTime, this, dataBlock));
    }
    
    
    public void start() throws SensorHubException
    {
        sosClient.startStream(new StreamingListener() {
            @Override
            public void recordReceived(DataBlock data) {
                publishNewRecord(data);
            }

			@Override
			public void stopped(StreamingStopReason reason, Throwable cause) {
				try {
					log.info("Stopping sensor due to output failure: {}", reason);
					parentSensor.stop();
				} catch (Exception e) {
					log.error("Unable to stop sensor");
				}
			}
        });
    }
    
    
    public void stop()
    {
        sosClient.stopStream();
    }
}
