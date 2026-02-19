/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.processing;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.sensorhub.api.command.CommandResult;
import org.sensorhub.api.command.CommandStatus;
import org.sensorhub.api.command.ICommandData;
import org.sensorhub.api.command.ICommandStatus;
import org.sensorhub.api.command.IStreamingControlInterfaceWithResult;
import org.sensorhub.api.processing.IOnDemandProcess;
import org.sensorhub.api.system.ISystemGroupDriver;
import org.sensorhub.impl.command.AbstractControlInterface;
import org.sensorhub.impl.data.AbstractDataInterface;
import org.vast.data.TextEncodingImpl;
import org.vast.util.Asserts;
import org.vast.util.TimeExtent;
import net.opengis.sensorml.v20.AbstractProcess;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;


/**
 * <p>
 * Helper class to create synchronous on-demand process modules.
 * Implementations only have to implement the execute method
 * </p>
 *
 * @author Alex Robin
 * @since Sep 13, 2022
 */
public abstract class OnDemandProcess extends AbstractProcessDriver implements IOnDemandProcess
{
    protected String name;
    protected DataComponent input;  // used to carry live data
    protected DataComponent output; // used to carry live data
    protected DataComponent params; // used to carry live data
    
    
    static class SyncInput extends AbstractControlInterface<OnDemandProcess> implements IStreamingControlInterfaceWithResult
    {
        final DataComponent inputDesc;
        final DataComponent outputDesc;
        boolean async;
        
        protected SyncInput(String name, OnDemandProcess parent)
        {
            super(name, parent);
            this.inputDesc = parent.input.copy();
            this.outputDesc = parent.output.copy();
        }
        
        @Override
        public DataComponent getCommandDescription()
        {
            return inputDesc;
        }
        
        @Override
        public CompletableFuture<ICommandStatus> submitCommand(ICommandData command)
        {
            // set cmd to process input
            parent.input.setData(command.getParams());
            
            Supplier<ICommandStatus> exec = () -> {
                // call execute
                var execTime = TimeExtent.currentTime();
                parent.execute();
                
                // return status synchronously with result
                var result = CommandResult.withData(parent.output.getData().clone());
                return CommandStatus.completed(command.getID(), execTime, result);
            };
            
            if (async)
                return CompletableFuture.supplyAsync(exec);
            else
                return CompletableFuture.completedFuture(exec.get());
        }

        @Override
        public DataComponent getResultDescription()
        {
            return outputDesc;
        }
    }
    
    
    static class SyncOutput extends AbstractDataInterface<OnDemandProcess>
    {
        DataComponent outputDesc;
        
        protected SyncOutput(String name, DataComponent outputDesc, OnDemandProcess parent)
        {
            super(name, parent);
            this.outputDesc = outputDesc;
        }

        @Override
        public DataComponent getRecordDescription()
        {
            return outputDesc;
        }

        @Override
        public DataEncoding getRecommendedEncoding()
        {
            return new TextEncodingImpl();
        }

        @Override
        public double getAverageSamplingPeriod()
        {
            return 0;
        }
    }
    

    protected OnDemandProcess(String name, ISystemGroupDriver<?> parent)
    {
        super(parent);
        this.name = Asserts.checkNotNullOrBlank(name, "name");
        this.processDescription = initProcess();
        this.processDescription.setDefinition(ON_DEMAND_PROCESS_DEF);
        
        Asserts.checkNotNull(input, "input").assignNewDataBlock();
        addInput(new SyncInput(input.getName(), this));
        Asserts.checkNotNull(output, "output").assignNewDataBlock();
        
        if (params != null)
        {
            params.assignNewDataBlock();
            this.parameters.put(params.getName(), params);
        }
    }
    
    
    /**
     * Initializes process descriptions and inputs/outputs
     * @return The SensorML description 
     */
    protected abstract AbstractProcess initProcess();


    @Override
    public void execute(Collection<String> foiUIDs)
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public String getDescription()
    {
        return null;
    }
}
