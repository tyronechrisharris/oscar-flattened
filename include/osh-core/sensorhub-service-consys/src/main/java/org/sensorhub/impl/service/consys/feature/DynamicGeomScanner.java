/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.

******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.feature;

import java.io.IOException;
import org.vast.swe.fast.DataBlockProcessor;
import net.opengis.gml.v32.AbstractGeometry;
import net.opengis.gml.v32.impl.PointImpl;
import net.opengis.swe.v20.Boolean;
import net.opengis.swe.v20.Category;
import net.opengis.swe.v20.Count;
import net.opengis.swe.v20.DataArray;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataChoice;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.Quantity;
import net.opengis.swe.v20.Text;
import net.opengis.swe.v20.Time;
import net.opengis.swe.v20.Vector;


/**
 * <p>
 * Component tree processor for extracting variable feature geometry data
 * from observation results
 * </p>
 *
 * @author Alex Robin
 * @since Jun 24, 2022
 */
public class DynamicGeomScanner extends DataBlockProcessor
{
    AbstractGeometry geom;
    double[] posList;
    
    
    protected class ValueSkipper extends BaseProcessor
    {
        @Override
        public int process(DataBlock data, int index) throws IOException
        {
            return ++index;
        }
    }
    
    
    protected class PointScanner extends RecordProcessor
    {
        int numDims;
        
        protected PointScanner(Vector v)
        {
            this.numDims = v.getComponentCount();
            posList = new double[numDims];
            geom = new PointImpl(0);
            geom.setSrsName(v.getReferenceFrame());
            ((PointImpl)geom).setPos(posList);
        }
        
        @Override
        public int process(DataBlock data, int index) throws IOException
        {
            for (int i = 0; i < numDims; i++)
                posList[i] = data.getDoubleValue(index++);
            return index;
        }
    }
    
    
    public DynamicGeomScanner()
    {
    }
    
    
    public void initProcessTree()
    {
        checkEnabled(dataComponents);
        dataComponents.accept(this);
        processorTreeReady = true;
    }
    
    
    public static boolean isGeom(DataComponent comp)
    {
        return comp.getDefinition() != null && comp.getDefinition().toLowerCase().contains("location");
    }
    
    
    public boolean hasGeom()
    {
        return posList != null;
    }
    
    
    public AbstractGeometry getGeom(DataBlock data)
    {
        try
        {
            rootProcessor.process(data, 0);
            return geom;
        }
        catch (IOException e)
        {
            return null;
        }
    }


    @Override
    protected void init()
    {
    }


    @Override
    protected RecordProcessor getVectorProcessor(Vector vect)
    {
        if (isGeom(vect))
            return new PointScanner(vect);
        else
            return super.getVectorProcessor(vect);
    }


    @Override
    public void visit(Boolean component)
    {
        addToProcessorTree(new ValueSkipper());
    }


    @Override
    public void visit(Count component)
    {
        addToProcessorTree(new ValueSkipper());
    }


    @Override
    public void visit(Quantity component)
    {
        addToProcessorTree(new ValueSkipper());
    }


    @Override
    public void visit(Time component)
    {
        addToProcessorTree(new ValueSkipper());
    }


    @Override
    public void visit(Category component)
    {
        addToProcessorTree(new ValueSkipper());
    }


    @Override
    public void visit(Text component)
    {
        addToProcessorTree(new ValueSkipper());
    }


    @Override
    protected ChoiceProcessor getChoiceProcessor(DataChoice choice)
    {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    protected ImplicitSizeProcessor getImplicitSizeProcessor(DataArray array)
    {
        return new ImplicitSizeProcessor();
    }


    @Override
    protected ArraySizeSupplier getArraySizeSupplier(String refId)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
