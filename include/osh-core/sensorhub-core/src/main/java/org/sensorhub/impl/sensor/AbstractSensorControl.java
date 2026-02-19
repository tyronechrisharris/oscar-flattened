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

import java.util.concurrent.CompletableFuture;
import org.sensorhub.api.command.CommandException;
import org.sensorhub.api.command.CommandStatus;
import org.sensorhub.api.command.ICommandData;
import org.sensorhub.api.command.ICommandReceiver;
import org.sensorhub.api.command.ICommandStatus;
import org.sensorhub.impl.command.AbstractControlInterface;
import net.opengis.swe.v20.DataBlock;


/**
 * <p>
 * Default implementation of common sensor control interface API methods.
 * By default, async exec, scheduling and status history are reported as
 * unsupported.
 * </p>
 *
 * @author Alex Robin
 * @param <T> Type of parent system
 * @since Nov 22, 2014
 */
public abstract class AbstractSensorControl<T extends ICommandReceiver> extends AbstractControlInterface<T>
{
    protected final T parentSensor; // for backward compatibility
    
    
    protected AbstractSensorControl(String name, T parentSensor)
    {
        super(name, parentSensor);
        this.parentSensor = parent;
        
    }
    

    protected boolean execCommand(DataBlock cmdData) throws CommandException
    {
        throw new UnsupportedOperationException();
    }
    
    
    @Override
    public CompletableFuture<ICommandStatus> submitCommand(ICommandData command)
    {
        return CompletableFuture.supplyAsync(() -> {
            ICommandStatus status;
            try
            {
                var ok = execCommand(command.getParams());
                status = ok ? CommandStatus.completed(command.getID()) : CommandStatus.failed(command.getID(), null);
            }
            catch (CommandException e)
            {
                getLogger().error("Error processing command", e);
                status = CommandStatus.failed(command.getID(), e.getMessage());
            }
            
            return status;
        });
    }
}
