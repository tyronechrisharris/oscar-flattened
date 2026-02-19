/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.swe;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.IQueryFilter;
import org.sensorhub.api.datastore.command.CommandFilter;
import org.sensorhub.api.datastore.obs.ObsFilter;
import org.sensorhub.api.module.ModuleEvent.ModuleState;
import org.sensorhub.api.service.IServiceModule;
import org.sensorhub.impl.database.registry.FilteredFederatedDatabase;
import org.sensorhub.impl.service.AbstractHttpServiceModule;
import org.sensorhub.utils.NamedThreadFactory;


/**
 * <p>
 * Base abstract class for both SOS and SPS service modules
 * </p>
 * 
 * @param <ConfigType> Type of config
 *
 * @author Alex Robin
 * @since Mar 31, 2021
 */
public abstract class SWEService<ConfigType extends SWEServiceConfig> extends AbstractHttpServiceModule<ConfigType> implements IServiceModule<ConfigType>
{
    protected SWEServlet servlet;
    protected ScheduledExecutorService threadPool;
    protected IObsSystemDatabase readDatabase;
    protected IObsSystemDatabase writeDatabase;
    
    
    protected abstract IQueryFilter getResourceFilter();
    

    @Override
    protected void doStart() throws SensorHubException
    {
        // get handle to write database
        // use the configured database
        if (config.databaseID != null)
        {
            writeDatabase = (IObsSystemDatabase)getParentHub().getModuleRegistry()
                .getModuleById(config.databaseID);
        }
        
        // or default to the system state DB
        else
        {
            writeDatabase = getParentHub().getSystemDriverRegistry().getSystemStateDatabase();
        }
        
        // if a filter was provided, use a filtered db implementation
        var filter = getResourceFilter();
        if (filter != null)
        {
            if (writeDatabase != null)
            {
                // if a writable database was provided, make sure we always expose
                // its content via this service by flagging it as unfiltered
                if (filter instanceof ObsFilter)
                {
                    readDatabase = new FilteredFederatedDatabase(
                        getParentHub().getDatabaseRegistry(),
                        (ObsFilter)filter, writeDatabase.getDatabaseNum());
                }
                else if (filter instanceof CommandFilter)
                {
                    readDatabase = new FilteredFederatedDatabase(
                        getParentHub().getDatabaseRegistry(),
                        (CommandFilter)filter, writeDatabase.getDatabaseNum());
                }
                else
                    throw new IllegalStateException();
            }
            else
                readDatabase = config.exposedResources.getFilteredView(getParentHub());
        }
        
        // else expose all systems on this hub
        else
            readDatabase = getParentHub().getDatabaseRegistry().getFederatedDatabase();

        // init thread pool
        var threadPool = new ScheduledThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            new NamedThreadFactory("SOSPool"));
        threadPool.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        threadPool.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        this.threadPool = threadPool;
    }


    @Override
    protected void doStop() throws SensorHubException
    {
        // undeploy servlet
        undeploy();
        if (servlet != null)
            servlet.destroy();
        servlet = null;
        
        // stop thread pool
        if (threadPool != null)
            threadPool.shutdown();

        setState(ModuleState.STOPPED);
    }


    protected void deploy() throws SensorHubException
    {
        // deploy ourself to HTTP server
        httpServer.deployServlet(servlet, config.endPoint);
        httpServer.addServletSecurity(config.endPoint, config.security.requireAuth);
    }


    protected void undeploy()
    {
        // return silently if HTTP server missing on stop
        if (httpServer == null || !httpServer.isStarted())
            return;

        httpServer.undeployServlet(servlet);
    }


    @Override
    public void cleanup() throws SensorHubException
    {
        // unregister security handler
        if (securityHandler != null)
            securityHandler.unregister();
    }


    public ScheduledExecutorService getThreadPool()
    {
        return threadPool;
    }


    public IObsSystemDatabase getReadDatabase()
    {
        return readDatabase;
    }


    public IObsSystemDatabase getWriteDatabase()
    {
        return writeDatabase;
    }
    
}
