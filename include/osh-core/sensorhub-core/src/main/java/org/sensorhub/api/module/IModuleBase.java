/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2025 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.module;

import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.module.ModuleEvent.ModuleState;


/**
 * <p>
 * Base interface for main modules and submodules
 * </p>
 *
 * @author Alex Robin
 * @since Feb 28, 2025
 */
public interface IModuleBase<T extends ModuleConfigBase>
{
    
    
    /**
     * Initializes the module with the specified configuration.<br/>
     * This is equivalent to calling {@link #setConfiguration(ModuleConfig)}
     * and then {@link #init()} with no arguments.<br/>
     * @param config
     * @throws SensorHubException if an error occurs during synchronous execution. 
     * If an error occurs asynchronously, it can be retrieved with {@link #getCurrentError()}
     */
    public void init(T config) throws SensorHubException;
    
    
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
     * Retrieves a copy of the module configuration
     * (i.e. for reading only since changes won't have any effect until updateConfig is called)
     * @return a copy of the configuration object associated to this module
     */
    public T getConfiguration();
    
    
    /**
     * Helper method to get the module's name
     * @return name string
     */
    public String getName();
}
