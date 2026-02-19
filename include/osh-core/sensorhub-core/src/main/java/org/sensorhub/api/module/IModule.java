/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.module;

import org.sensorhub.api.ISensorHub;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.event.IEventListener;
import org.sensorhub.api.event.IEventProducer;
import org.sensorhub.api.module.ModuleEvent.ModuleState;
import org.slf4j.Logger;


/**
 * <p>
 * Generic interface for all modules in the system.
 * </p>
 *
 * @author Alex Robin
 * @param <T> Type of module config 
 * @since Nov 12, 2010
 */
public interface IModule<T extends ModuleConfig> extends IModuleBase<T>, IEventProducer
{
    public static final String CANNOT_LOAD_MSG = "Cannot load module ";
    public static final String CANNOT_INIT_MSG = "Cannot initialize module ";
    public static final String CANNOT_START_MSG = "Cannot start module ";
    public static final String CANNOT_STOP_MSG = "Cannot stop module ";
    public static final String CANNOT_UPDATE_MSG = "Cannot update module configuration";
    
    
    /**
     * Sets the parent hub that is used to access hub-wise functionality.<br/>
     * This is automatically called once when the module is loaded with the
     * ModuleRegistry associated to a sensor hub. When instantiating the module
     * manually, it must be called before any other method.
     * @param hub
     */
    public void setParentHub(ISensorHub hub);
    
    
    /**
     * @return parent hub that loaded this module
     */
    public ISensorHub getParentHub();
    
    
    /**
     * Sets the parent sensor hub and module configuration
     * @param config
     */
    public void setConfiguration(T config);
    
    
    /**
     * Helper method to get the module's description
     * @return description string
     */
    public String getDescription();
    
    
    /**
     * Helper method to get the module's local ID
     * @return id string
     */
    public String getLocalID();
    
    
    /**
     * Checks if module is initialized
     * @return true if module is initialized, false otherwise
     */
    public boolean isInitialized();
 
    
    /**
     * Checks if module is started
     * @return true if module is started, false otherwise
     */
    public boolean isStarted();
    
    
    /**
     * @return the current state of the module
     */
    public ModuleState getCurrentState();
    
    
    /**
     * Waits until the module reaches the specified state or times out.<br/>
     * This method will return immediately if the state has already been reached.
     * @param state state to wait for
     * @param timeout maximum time to wait in milliseconds or 0 to wait forever
     * @return true if module state has been reached, false in case of timeout or error
     * @throws SensorHubException if an error occurs before the desired state is reached
     */
    public boolean waitForState(ModuleState state, long timeout) throws SensorHubException;
    
    
    /**
     * @return the current status message
     */
    public String getStatusMessage();
    
    
    /**
     * @return the last error that occured executing the module
     */
    public Throwable getCurrentError();
    
    
    /**
     * @return the logger associated to this module
     */
    public Logger getLogger();
    
    
    /**
     * Requests the module to initialize with the current configuration.<br/>
     * Implementations of this method can be synchronous or asynchronous, but when 
     * this method returns without error, the module state is guaranteed to be
     * either {@link ModuleState#INITIALIZING} or {@link ModuleState#INITIALIZED}.<br/>
     * A configuration must be set before calling this method.<br/>
     * @throws SensorHubException if an error occurs during synchronous execution. 
     * If an error occurs asynchronously, it can be retrieved with {@link #getCurrentError()}
     */
    public void init() throws SensorHubException;
    
    
    /**
     * Updates the module's configuration dynamically.<br/>
     * The module must honor this new configuration unless an error is detected.
     * It is the responsability of the module to initiate a restart if the new
     * configuration requires it.
     * @param config
     * @throws SensorHubException 
     */
    public void updateConfig(T config) throws SensorHubException;
    
    
    /**
     * Requests the module to start.<br/>
     * Implementations of this method can be synchronous or asynchronous, but when 
     * this method returns without error, the module state is guaranteed to be
     * either {@link ModuleState#STARTING} or {@link ModuleState#STARTED}.<br/>
     * Module should be in {@link ModuleState#INITIALIZED} state before calling this method.
     * @throws SensorHubException if an error occurs during synchronous execution. 
     * If an error occurs asynchronously, it can be retrieved with {@link #getCurrentError()}
     */
    public void start() throws SensorHubException;
    
    
    /**
     * Requests the module to stop.<br/>
     * Implementations of this method can be synchronous or asynchronous, but when 
     * this method returns without error, the module state is guaranteed to be
     * either {@link ModuleState#STOPPING} or {@link ModuleState#STOPPED}.<br/>
     * @throws SensorHubException if an error occurs during synchronous execution. 
     * If an error occurs asynchronously, it can be retrieved with {@link #getCurrentError()}
     */
    public void stop() throws SensorHubException;
    
    
    /**
     * Saves the state of this module.<br/> 
     * Implementations of this method must block until the module state is
     * successfully saved or send an exception.
     * @param saver
     * @throws SensorHubException 
     */
    public void saveState(IModuleStateManager saver) throws SensorHubException;
    
    
    /**
     * Restores the state of this module<br/>
     * Implementations of this method must block until the module state is
     * successfully loaded or send an exception.
     * @param loader
     * @throws SensorHubException 
     */
    public void loadState(IModuleStateManager loader) throws SensorHubException;
    
    
    /**
     * Cleans up all ressources used by the module when deleted
     * All persistent resources created by the module should be cleaned
     * when this is called
     * @throws SensorHubException
     */
    public void cleanup() throws SensorHubException;
    
    
    /**
     * Registers a listener to receive events generated by this module.<br/>
     * When this method is called, the current state of the module is also notified
     * synchronously to guarantee that the listener always receives it.
     * @param listener
     */
    @Override
    public void registerListener(IEventListener listener);
    
    
    /**
     * Unregisters a listener and thus stop receiving events generayed by this module
     * @param listener
     */
    @Override
    public void unregisterListener(IEventListener listener);    
}
