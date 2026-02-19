/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.obs;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.api.feature.FeatureId;
import org.vast.util.Asserts;
import org.vast.util.TimeExtent;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;


/**
 * <p>
 * Base wrapper class for {@link IDataStreamInfo} objects
 * </p>
 *
 * @author Alex Robin
 * @date Oct 2, 2020
 */
public abstract class DataStreamInfoWrapper implements IDataStreamInfo
{
    IDataStreamInfo delegate;
    

    public DataStreamInfoWrapper(IDataStreamInfo dsInfo)
    {
        this.delegate = Asserts.checkNotNull(dsInfo, IDataStreamInfo.class);
    }
    
    
    @Override
    public FeatureId getSystemID()
    {
        return delegate.getSystemID();
    }


    @Override
    public String getOutputName()
    {
        return delegate.getOutputName();
    }


    @Override
    public String getName()
    {
        return delegate.getName();
    }


    @Override
    public String getDescription()
    {
        return delegate.getDescription();
    }


    @Override
    public DataComponent getRecordStructure()
    {
        return delegate.getRecordStructure();
    }


    @Override
    public DataEncoding getRecordEncoding()
    {
        return delegate.getRecordEncoding();
    }


    @Override
    public TimeExtent getValidTime()
    {
        return delegate.getValidTime();
    }


    @Override
    public TimeExtent getPhenomenonTimeRange()
    {
        return delegate.getPhenomenonTimeRange();
    }


    @Override
    public TimeExtent getResultTimeRange()
    {
        return delegate.getResultTimeRange();
    }


    @Override
    public boolean hasDiscreteResultTimes()
    {
        return delegate.hasDiscreteResultTimes();
    }


    @Override
    public Map<Instant, TimeExtent> getDiscreteResultTimes()
    {
        return delegate.getDiscreteResultTimes();
    }


    public Duration getPhenomenonTimeInterval()
    {
        return delegate.getPhenomenonTimeInterval();
    }


    public Duration getResultTimeInterval()
    {
        return delegate.getResultTimeInterval();
    }


    public FeatureId getFeatureOfInterestID()
    {
        return delegate.getFeatureOfInterestID();
    }


    public FeatureId getSamplingFeatureID()
    {
        return delegate.getSamplingFeatureID();
    }


    public FeatureId getProcedureID()
    {
        return delegate.getProcedureID();
    }


    public FeatureId getDeploymentID()
    {
        return delegate.getDeploymentID();
    }
}
