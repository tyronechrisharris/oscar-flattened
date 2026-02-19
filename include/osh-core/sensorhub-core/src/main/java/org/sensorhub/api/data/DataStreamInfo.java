/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.

******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.data;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import org.sensorhub.api.feature.FeatureId;
import org.vast.util.Asserts;
import org.vast.util.BaseBuilder;
import org.vast.util.TimeExtent;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;


/**
 * <p>
 * Immutable object containing information about a data stream of observations.
 * </p><p>
 * This class is mainly useful for creating new datastreams as it does not
 * provide dynamic timing information (i.e. {@link #getPhenomenonTimeRange()}
 * and {@link #getResultTimeRange()} both return a fixed arbitrary value).
 * A full implementation must extend or wrap this class and implement these
 * methods to compute correct values based on actual data available from the
 * data store.
 * </p>
 *
 * @author Alex Robin
 * @date Sep 19, 2019
 */
public class DataStreamInfo implements IDataStreamInfo
{
    protected String name;
    protected String description;
    protected FeatureId systemID;
    protected FeatureId procedureID;
    protected FeatureId deploymentID;
    protected FeatureId featureOfInterestID;
    protected FeatureId samplingFeatureID;
    protected TimeExtent validTime;
    protected Duration phenomenonTimeInterval;
    protected Duration resultTimeInterval;
    protected DataComponent recordStruct;
    protected DataEncoding recordEncoding;
    
    
    @Override
    public String getName()
    {
        return name;
    }
    
    
    @Override
    public String getDescription()
    {
        return description;
    }
    
    
    @Override
    public FeatureId getSystemID()
    {
        return systemID;
    }


    @Override
    public String getOutputName()
    {
        return recordStruct.getName();
    }
    
    
    @Override
    public FeatureId getProcedureID()
    {
        return procedureID;
    }


    @Override
    public FeatureId getDeploymentID()
    {
        return deploymentID;
    }


    @Override
    public FeatureId getFeatureOfInterestID()
    {
        return featureOfInterestID;
    }


    @Override
    public FeatureId getSamplingFeatureID()
    {
        return samplingFeatureID;
    }


    @Override
    public TimeExtent getValidTime()
    {
        return validTime;
    }


    @Override
    public TimeExtent getPhenomenonTimeRange()
    {
        return null;
    }
    
    
    @Override
    public Duration getPhenomenonTimeInterval()
    {
        return phenomenonTimeInterval;
    }


    @Override
    public TimeExtent getResultTimeRange()
    {
        return null;
    }


    @Override
    public Duration getResultTimeInterval()
    {
        return resultTimeInterval;
    }


    @Override
    public boolean hasDiscreteResultTimes()
    {
        return false;
    }


    @Override
    public Map<Instant, TimeExtent> getDiscreteResultTimes()
    {
        return Collections.emptyMap();
    }


    @Override
    public DataComponent getRecordStructure()
    {
        return recordStruct;
    }


    @Override
    public DataEncoding getRecordEncoding()
    {
        return recordEncoding;
    }


    /*
     * Builder
     */
    public static class Builder extends DataStreamInfoBuilder<Builder, DataStreamInfo>
    {
        public Builder()
        {
            this.instance = new DataStreamInfo();
        }

        public static Builder from(IDataStreamInfo base)
        {
            return new Builder().copyFrom(base);
        }
    }


    @SuppressWarnings("unchecked")
    public static abstract class DataStreamInfoBuilder<B extends DataStreamInfoBuilder<B, T>, T extends DataStreamInfo>
        extends BaseBuilder<T>
    {
        protected DataStreamInfoBuilder()
        {
        }


        protected B copyFrom(IDataStreamInfo base)
        {
            instance.name = base.getName();
            instance.description = base.getDescription();
            instance.systemID = base.getSystemID();
            instance.recordStruct = base.getRecordStructure();
            instance.recordEncoding = base.getRecordEncoding();
            instance.validTime = base.getValidTime();
            instance.phenomenonTimeInterval = base.getPhenomenonTimeInterval();
            instance.resultTimeInterval = base.getResultTimeInterval();
            instance.procedureID = base.getProcedureID();
            instance.deploymentID = base.getDeploymentID();
            instance.featureOfInterestID = base.getFeatureOfInterestID();
            instance.samplingFeatureID = base.getSamplingFeatureID();
            return (B)this;
        }
        
        
        public B withName(String name)
        {
            instance.name = name;
            return (B)this;
        }
        
        
        public B withDescription(String desc)
        {
            instance.description = desc;
            return (B)this;
        }


        public B withSystem(FeatureId sysID)
        {
            instance.systemID = sysID;
            return (B)this;
        }


        public B withRecordDescription(DataComponent recordStruct)
        {
            instance.recordStruct = recordStruct;
            return (B)this;
        }


        public B withRecordEncoding(DataEncoding recordEncoding)
        {
            instance.recordEncoding = recordEncoding;
            return (B)this;
        }


        public B withValidTime(TimeExtent validTime)
        {
            instance.validTime = validTime;
            return (B)this;
        }


        public B withPhenomenonTimeInterval(Duration interval)
        {
            instance.phenomenonTimeInterval = interval;
            return (B)this;
        }


        public B withResultTimeInterval(Duration interval)
        {
            instance.resultTimeInterval = interval;
            return (B)this;
        }


        public B withDeployment(FeatureId deplID)
        {
            instance.deploymentID = deplID;
            return (B)this;
        }


        public B withProcedure(FeatureId procID)
        {
            instance.procedureID = procID;
            return (B)this;
        }


        public B withFeatureOfInterest(FeatureId foiID)
        {
            instance.featureOfInterestID = foiID;
            return (B)this;
        }


        public B withSamplingFeature(FeatureId sfID)
        {
            instance.samplingFeatureID = sfID;
            return (B)this;
        }


        @Override
        public T build()
        {
            Asserts.checkNotNullOrEmpty(instance.name, "name");
            Asserts.checkNotNull(instance.systemID, "systemID");
            Asserts.checkNotNull(instance.recordStruct, "recordStruct");
            Asserts.checkNotNullOrEmpty(instance.getOutputName(), "outputName");
            Asserts.checkNotNull(instance.recordEncoding, "recordEncoding");
            return super.build();
        }
    }
}
