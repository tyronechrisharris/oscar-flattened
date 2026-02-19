/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.command;

import org.sensorhub.api.command.CommandException;
import org.sensorhub.api.command.ICommandData;
import org.sensorhub.api.command.ICommandReceiver;
import org.sensorhub.api.command.IStreamingControlInterface;
import org.sensorhub.api.event.IEventHandler;
import org.sensorhub.api.event.IEventListener;
import org.sensorhub.impl.event.BasicEventHandler;
import org.sensorhub.impl.module.AbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.util.Asserts;


/**
 * <p>
 * Default implementation of common control interface API methods.
 * </p>
 *
 * @author Alex Robin
 * @param <T> Type of parent entity
 * @since Sep 13, 2022
 */
public abstract class AbstractControlInterface<T extends ICommandReceiver> implements IStreamingControlInterface
{
    protected final T parent;
    protected final IEventHandler eventHandler;
    protected final String name;
    protected final Logger log;
    
    
    protected AbstractControlInterface(String name, T parent)
    {
        this(name, parent, null);
    }
    
    
    /**
     * Constructs a new control input with the given name and attached to the
     * provided parent sensor.<br/>
     * @param name
     * @param parentSensor
     * @param eventSrcInfo
     * @param log
     */
    protected AbstractControlInterface(String name, T parent, Logger log)
    {
        this.name = Asserts.checkNotNull(name, "name");
        this.parent = Asserts.checkNotNull(parent, "parent");
        this.eventHandler = new BasicEventHandler();
        
        // setup logger
        if (log == null)
        {
            if (parent instanceof AbstractModule)
                this.log = ((AbstractModule<?>)parent).getLogger();
            else
                this.log = LoggerFactory.getLogger(getClass().getCanonicalName());
        }
        else
            this.log = log;
    }
    
    
    @Override
    public T getParentProducer()
    {
        return parent;
    }
    
    
    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public boolean isEnabled()
    {
        return true;
    };


    @Override
    public void validateCommand(ICommandData command) throws CommandException
    {
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
    
    
    protected Logger getLogger()
    {
        return log; 
    }
    
}
