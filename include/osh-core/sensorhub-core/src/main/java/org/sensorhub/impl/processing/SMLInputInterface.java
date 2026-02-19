/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2023 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.processing;

import java.util.concurrent.CompletableFuture;
import org.sensorhub.api.command.CommandException;
import org.sensorhub.api.command.CommandStatus;
import org.sensorhub.api.command.ICommandData;
import org.sensorhub.api.command.ICommandReceiver;
import org.sensorhub.api.command.ICommandStatus;
import org.sensorhub.api.command.ICommandStatus.CommandStatusCode;
import org.sensorhub.api.command.IStreamingControlInterface;
import org.sensorhub.api.event.IEventListener;
import org.sensorhub.api.processing.ProcessingException;
import org.vast.process.DataQueue;
import org.vast.process.ProcessException;
import org.vast.sensorML.SMLHelper;
import net.opengis.swe.v20.AbstractSWEIdentifiable;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;


public class SMLInputInterface implements IStreamingControlInterface
{
    final SMLProcessImpl parentProcess;
    final DataComponent inputDef;
    final DataComponent inputComponent;
    final InputQueue inputQueue;
    
    static class InputQueue extends DataQueue
    {
        @Override
        public synchronized boolean transferData(boolean block) throws InterruptedException
        {
            if (block || isDataAvailable())
            {
                DataBlock srcBlock = queue.take();
                destinationComponent.setData(srcBlock);
                return true;
            }
            else
                return false;
        }
        
        protected void addData(DataBlock data)
        {
            queue.add(data);
        }
    };
    
    
    public SMLInputInterface(SMLProcessImpl parentProcess, AbstractSWEIdentifiable inputDescriptor) throws ProcessingException
    {
        this.parentProcess = parentProcess;
        this.inputDef = SMLHelper.getIOComponent(inputDescriptor);
        this.inputComponent = parentProcess.wrapperProcess.getInputComponent(inputDef.getName());
        
        try
        {
            this.inputQueue = new InputQueue();
            DataComponent execInput = parentProcess.wrapperProcess.getInputComponent(inputDef.getName());
            parentProcess.wrapperProcess.connect(execInput, inputQueue);
        }
        catch (ProcessException e)
        {
            throw new ProcessingException("Error while connecting input " + inputDef.getName(), e);
        }
    }


    @Override
    public CompletableFuture<ICommandStatus> submitCommand(ICommandData command)
    {
        /*try
        {
            // execute process chain synchronously
            inputComponent.setData(command.getParams());
            parentProcess.wrapperProcess.execute();
        }
        catch (ProcessException e)
        {
            parentProcess.getLogger().error("Error executing process", e);
            
            return CompletableFuture.completedFuture(new CommandStatus.Builder()
                .withCommand(command.getID())
                .withStatusCode(CommandStatusCode.FAILED)
                .build());
        }*/
        inputQueue.addData(command.getParams());
        
        return CompletableFuture.completedFuture(new CommandStatus.Builder()
            .withCommand(command.getID())
            .withStatusCode(CommandStatusCode.COMPLETED)
            .build());
    }


    @Override
    public ICommandReceiver getParentProducer()
    {
        return parentProcess;
    }


    @Override
    public String getName()
    {
        return inputDef.getName();
    }


    @Override
    public DataComponent getCommandDescription()
    {
        return inputDef;
    }


    @Override
    public boolean isEnabled()
    {
        return true;
    }


    @Override
    public void validateCommand(ICommandData command) throws CommandException
    {
        // TODO Auto-generated method stub

    }


    @Override
    public void registerListener(IEventListener listener)
    {
        // TODO Auto-generated method stub

    }


    @Override
    public void unregisterListener(IEventListener listener)
    {
        // TODO Auto-generated method stub

    }

}
