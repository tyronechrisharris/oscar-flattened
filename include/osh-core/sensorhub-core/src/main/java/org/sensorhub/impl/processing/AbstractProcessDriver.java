/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.processing;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.opengis.sensorml.v20.AbstractProcess;
import net.opengis.swe.v20.DataComponent;
import org.sensorhub.api.command.IStreamingControlInterface;
import org.sensorhub.api.data.IStreamingDataInterface;
import org.sensorhub.api.event.IEventHandler;
import org.sensorhub.api.event.IEventListener;
import org.sensorhub.api.processing.IDataProcess;
import org.sensorhub.api.system.ISystemDriver;
import org.sensorhub.api.system.ISystemGroupDriver;
import org.sensorhub.impl.event.BasicEventHandler;
import org.vast.ogc.gml.IFeature;
import org.vast.util.Asserts;


/**
 * <p>
 * Base class for processing components
 * </p>
 * 
 * @author Alex Robin
 * @since Sept 19, 2022
 */
public abstract class AbstractProcessDriver implements IDataProcess
{
    protected final Map<String, DataComponent> inputs = new LinkedHashMap<>();
    protected final Map<String, DataComponent> outputs = new LinkedHashMap<>();
    protected final Map<String, DataComponent> parameters = new LinkedHashMap<>();
    protected final Map<String, IStreamingDataInterface> outputInterfaces = new LinkedHashMap<>();
    protected final Map<String, IStreamingControlInterface> controlInterfaces = new LinkedHashMap<>();

    protected final IEventHandler eventHandler;
    protected final ISystemGroupDriver<?> parentSystem;
    protected AbstractProcess processDescription;
    protected long lastUpdatedProcess = Long.MIN_VALUE;
    protected boolean paused = false;
    protected int errorCount = 0;
    
    
    protected AbstractProcessDriver(ISystemGroupDriver<?> parent)
    {
        this.eventHandler = new BasicEventHandler();
        this.parentSystem = parent;
    }
    
    
    /**
     * Helper method to make sure derived classes add outputs consistently in the different maps
     * @param outputInterface
     */
    protected void addOutput(IStreamingDataInterface outputInterface)
    {
        Asserts.checkNotNull(outputInterface, IStreamingDataInterface.class);
        
        synchronized(outputInterfaces)
        {
            String outputName = outputInterface.getName();
            outputs.put(outputName, outputInterface.getRecordDescription());
            outputInterfaces.put(outputName, outputInterface);
        }
    }
    
    
    /**
     * Helper method to make sure derived classes add inputs consistently in the different maps
     * @param controlInterface
     */
    protected void addInput(IStreamingControlInterface controlInterface)
    {
        Asserts.checkNotNull(controlInterface, IStreamingDataInterface.class);
        
        synchronized(controlInterfaces)
        {
            String inputName = controlInterface.getName();
            inputs.put(inputName, controlInterface.getCommandDescription());
            controlInterfaces.put(inputName, controlInterface);
        }
    }
    
    
    /**
     * Helper method to make sure derived classes add parameters consistently in the different maps
     * @param controlInterface
     */
    protected void addParams(IStreamingControlInterface controlInterface)
    {
        Asserts.checkNotNull(controlInterface, IStreamingDataInterface.class);
        
        synchronized(controlInterfaces)
        {
            String paramName = controlInterface.getName();
            parameters.put(paramName, controlInterface.getCommandDescription());
            controlInterfaces.put(paramName, controlInterface);
        }
    }
    
    
    @Override
    public String getName()
    {
        return getCurrentDescription().getName();
    }
    
    
    @Override
    public String getDescription()
    {
        return processDescription != null ? processDescription.getDescription() : null;
    }


    @Override
    public synchronized AbstractProcess getCurrentDescription()
    {
        return processDescription;
    }


    @Override
    public synchronized long getLatestDescriptionUpdate()
    {
        return lastUpdatedProcess;
    }


    @Override
    public String getParentSystemUID()
    {
        return parentSystem != null ? parentSystem.getUniqueIdentifier() : null;
    }


    @Override
    public ISystemGroupDriver<? extends ISystemDriver> getParentSystem()
    {
        return parentSystem;
    }


    @Override
    public Map<String, DataComponent> getInputDescriptors()
    {
        synchronized(controlInterfaces)
        {
            return Collections.unmodifiableMap(inputs);
        }
    }


    @Override
    public Map<String, DataComponent> getOutputDescriptors()
    {
        synchronized(outputInterfaces)
        {
            return Collections.unmodifiableMap(outputs);
        }
    }
    
    
    @Override
    public Map<String, DataComponent> getParameterDescriptors()
    {
        synchronized(controlInterfaces)
        {
            return Collections.unmodifiableMap(parameters);
        }
    }
    
    
    @Override
    public Map<String, IStreamingDataInterface> getOutputs()
    {
        synchronized(outputInterfaces)
        {
            return Collections.unmodifiableMap(outputInterfaces);
        }
    }


    @Override
    public Map<String, ? extends IStreamingControlInterface> getCommandInputs()
    {
        synchronized(controlInterfaces)
        {
            return Collections.unmodifiableMap(controlInterfaces);
        }
    }
    
    
    @Override
    public String getUniqueIdentifier()
    {
        return processDescription.getUniqueIdentifier();
    }


    @Override
    public Map<String, ? extends IFeature> getCurrentFeaturesOfInterest()
    {
        return Collections.emptyMap();
    }
    
    
    @Override
    public void registerListener(IEventListener listener)
    {
        eventHandler.registerListener(listener);
    }


    @Override
    public void unregisterListener(IEventListener listener)
    {
        eventHandler.unregisterListener(listener);
    }


    @Override
    public boolean isEnabled()
    {
        return true;
    }
}
