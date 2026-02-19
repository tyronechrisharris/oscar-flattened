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

import org.sensorhub.api.sensor.ISensorDriver;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;


/**
 * <p>
 * Default location output for sensor drivers outputting their own location.
 * </p>
 *
 * @author Alex Robin
 * @since May 19, 2015
 */
public abstract class DefaultLocationOutput extends AbstractSensorOutput<ISensorDriver>
{
    protected DataComponent outputStruct;
    DataEncoding outputEncoding;
    protected double updatePeriod;


    public DefaultLocationOutput(ISensorDriver parentSensor, double updatePeriod)
    {
        super(AbstractSensorModule.LOCATION_OUTPUT_NAME, parentSensor);
        this.updatePeriod = updatePeriod;
    }


    @Override
    public DataComponent getRecordDescription()
    {
        return outputStruct;
    }


    @Override
    public DataEncoding getRecommendedEncoding()
    {
        return outputEncoding;
    }


    @Override
    public double getAverageSamplingPeriod()
    {
        return updatePeriod;
    }
    
    
    public abstract void updateLocation(double time, double x, double y, double z, boolean forceUpdate);

}