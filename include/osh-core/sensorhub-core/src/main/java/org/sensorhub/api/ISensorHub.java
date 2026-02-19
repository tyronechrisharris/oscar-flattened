/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2017 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api;

import org.osgi.framework.BundleContext;
import org.sensorhub.api.comm.INetworkManager;
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.database.IDatabaseRegistry;
import org.sensorhub.api.event.IEventBus;
import org.sensorhub.api.processing.IProcessingManager;
import org.sensorhub.api.security.ISecurityManager;
import org.sensorhub.api.system.ISystemDriverRegistry;
import org.sensorhub.impl.module.ModuleRegistry;


/**
 * <p>
 * Interface for the main sensor hub object 
 * </p>
 *
 * @author Alex Robin
 * @since Jun 23, 2017
 */
public interface ISensorHub
{    
    
    public ISensorHubConfig getConfig();
    
    
    public BundleContext getOsgiContext();


    public IEventBus getEventBus();

    
    public ModuleRegistry getModuleRegistry();


    public ISystemDriverRegistry getSystemDriverRegistry();


    public IDatabaseRegistry getDatabaseRegistry();
    

    public INetworkManager getNetworkManager();


    public ISecurityManager getSecurityManager();


    public IProcessingManager getProcessingManager();
    
    
    public IdEncoders getIdEncoders();
    
    
    public void start() throws SensorHubException;


    public void saveAndStop();


    public void stop();


    public void stop(boolean saveConfig, boolean saveState);

}