/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.system.wrapper;

import java.io.Serializable;
import java.util.List;
import org.vast.ogc.geopose.Pose;
import net.opengis.OgcProperty;
import net.opengis.OgcPropertyList;
import net.opengis.gml.v32.Point;
import net.opengis.gml.v32.Reference;
import net.opengis.sensorml.v20.AbstractProcess;
import net.opengis.sensorml.v20.PhysicalComponent;
import net.opengis.sensorml.v20.ProcessMethod;
import net.opengis.sensorml.v20.SpatialFrame;
import net.opengis.sensorml.v20.TemporalFrame;
import net.opengis.swe.v20.DataArray;
import net.opengis.swe.v20.DataRecord;
import net.opengis.swe.v20.Text;
import net.opengis.swe.v20.Time;
import net.opengis.swe.v20.Vector;


public class PhysicalComponentWrapper extends ProcessWrapper<PhysicalComponent> implements PhysicalComponent
{
    private static final long serialVersionUID = -2616107473336342728L;


    PhysicalComponentWrapper(PhysicalComponent delegate)
    {
        super(delegate);
    }
    
    
    @Override
    public ProcessMethod getMethod()
    {
        return delegate.getMethod();
    }
    
    
    @Override
    public OgcProperty<ProcessMethod> getMethodProperty()
    {
        return delegate.getMethodProperty();
    }
    
    
    @Override
    public boolean isSetMethod()
    {
        return delegate.isSetMethod();
    }


    @Override
    public Reference getAttachedTo()
    {
        return delegate.getAttachedTo();
    }


    @Override
    public List<SpatialFrame> getLocalReferenceFrameList()
    {
        return delegate.getLocalReferenceFrameList();
    }


    @Override
    public List<TemporalFrame> getLocalTimeFrameList()
    {
        return delegate.getLocalTimeFrameList();
    }


    @Override
    public int getNumLocalReferenceFrames()
    {
        return delegate.getNumLocalReferenceFrames();
    }


    @Override
    public int getNumLocalTimeFrames()
    {
        return delegate.getNumLocalTimeFrames();
    }


    @Override
    public int getNumPositions()
    {
        return delegate.getNumPositions();
    }


    @Override
    public int getNumTimePositions()
    {
        return delegate.getNumTimePositions();
    }


    @Override
    public OgcPropertyList<Serializable> getPositionList()
    {
        return delegate.getPositionList();
    }


    @Override
    public OgcPropertyList<Time> getTimePositionList()
    {
        return delegate.getTimePositionList();
    }


    @Override
    public boolean isSetAttachedTo()
    {
        return delegate.isSetAttachedTo();
    }


    @Override
    public void addLocalReferenceFrame(SpatialFrame localReferenceFrame)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    @Override
    public void addLocalTimeFrame(TemporalFrame localTimeFrame)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    @Override
    public void addPositionAsAbstractProcess(AbstractProcess position)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    @Override
    public void addPositionAsDataArray1(DataArray position)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    @Override
    public void addPositionAsDataRecord(DataRecord position)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    @Override
    public void addPositionAsPoint(Point position)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    @Override
    public void addPositionAsPose(Pose pose)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    @Override
    public void addPositionAsText(Text position)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    @Override
    public void addPositionAsVector(Vector position)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    @Override
    public void addTimePosition(Time timePosition)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
        
    }
    
    
    @Override
    public void setMethod(ProcessMethod method)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    @Override
    public void setAttachedTo(Reference attachedTo)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }
}
