/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.module;

import static org.junit.Assert.*;
import org.junit.Test;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.module.IModule;
import org.sensorhub.api.module.ModuleEvent;
import org.sensorhub.api.module.ModuleEvent.ModuleState;
import org.sensorhub.impl.SensorHub;


public class TestAsyncModule
{
    
    
    private AsyncModule loadModule(final AsyncModuleConfig conf) throws SensorHubException
    {        
        var hub = new SensorHub();
        hub.start();
     
        AsyncModuleConfig config = new AsyncModuleConfig();
        AsyncModule module = (AsyncModule)hub.getModuleRegistry().loadModule(config);
        module.setConfiguration(conf);
        
        module.registerListener(e -> {
            switch (((ModuleEvent)e).getNewState())
            {
                case INITIALIZED:
                    conf.initEventReceived = true;
                    break;
                    
                case STARTED:
                    conf.startEventReceived = true;
                    break;
                    
                case STOPPED:
                    conf.stopEventReceived = true;
                    break;
                    
                default:
            }            
        });
        
        return module;
    }    
    
    
    @Test
    public void testAsyncInit() throws Exception
    {
        final AsyncModuleConfig conf = new AsyncModuleConfig();
        conf.moduleClass = AsyncModule.class.getCanonicalName();
        conf.id = "MOD_ASYNC2";
        conf.autoStart = false;
        conf.name = "ModuleAsync2";
        conf.useThreadForInit = true;
        conf.initDelay = 100;
        conf.initExecTime = 200;
        long timeOut = 5000;
        
        IModule<?> module = loadModule(conf);
        module.init();
        
        long t0 = System.currentTimeMillis();
        module.waitForState(ModuleState.INITIALIZED, timeOut);
        long t1 = System.currentTimeMillis();
        
        long expectedDelay = conf.initDelay + conf.initExecTime;
        long delay = t1 - t0;
        //assertTrue("No INITIALIZED event received", conf.initEventReceived);
        assertTrue("Init never executed", delay >= expectedDelay);
        assertTrue("Init timeout reached", delay < timeOut);
    }
    
    
    @Test
    public void testAsyncStart() throws Exception
    {
        final AsyncModuleConfig conf = new AsyncModuleConfig();
        conf.moduleClass = AsyncModule.class.getCanonicalName();
        conf.id = "MOD_ASYNC2";
        conf.autoStart = false;
        conf.name = "ModuleAsync2";
        conf.useThreadForInit = true;
        conf.useThreadForStart = true;
        conf.initDelay = 100;
        conf.initExecTime = 150;
        conf.startDelay = 50;
        conf.startExecTime = 100;
        long timeOut = 5000;
        
        IModule<?> module = loadModule(conf);
        long t0 = System.currentTimeMillis();
        
        module.init();        
        module.waitForState(ModuleState.INITIALIZED, timeOut);
        long t1 = System.currentTimeMillis();
        
        module.start();
        module.waitForState(ModuleState.STARTED, timeOut);
        long t2 = System.currentTimeMillis();
        
        long expectedDelay = conf.initDelay + conf.initExecTime;
        long delay = t1 - t0;
        //assertTrue("No INITIALIZED event received", conf.initEventReceived);
        assertTrue("Init never executed", delay >= expectedDelay);
        assertTrue("Init timeout reached", delay < timeOut);
        
        expectedDelay = conf.startDelay + conf.startExecTime;
        delay = t2 - t1;
        //assertTrue("No STARTED event received", conf.startEventReceived);
        assertTrue("Start never executed", delay >= expectedDelay);
        assertTrue("Start timeout reached", delay < timeOut);
    }
    
    
    @Test
    public void testAsyncStop() throws Exception
    {
        final AsyncModuleConfig conf = new AsyncModuleConfig();
        conf.moduleClass = AsyncModule.class.getCanonicalName();
        conf.id = "MOD_ASYNC2";
        conf.autoStart = false;
        conf.name = "ModuleAsync2";
        conf.useThreadForInit = true;
        conf.useThreadForStart = true;
        conf.stopDelay = 50;
        conf.stopExecTime = 100;
        conf.useThreadForStop = true;
        long timeOut = 5000;
        
        IModule<?> module = loadModule(conf);
        module.init();
        module.waitForState(ModuleState.INITIALIZED, timeOut);
        module.start();
        module.waitForState(ModuleState.STARTED, timeOut);
        
        long t0 = System.currentTimeMillis();
        module.stop();
        assertEquals("Module is not stopping", ModuleState.STOPPING, module.getCurrentState());
        module.waitForState(ModuleState.STOPPED, timeOut);
        long t1 = System.currentTimeMillis();
        
        assertEquals("Module was not stopped", ModuleState.STOPPED, module.getCurrentState());
        long expectedDelay = conf.stopDelay + conf.stopExecTime;
        long delay = t1 - t0;
        //assertTrue("No STOPPED event received", conf.stopEventReceived);
        assertTrue("Stop never executed", delay >= expectedDelay);
        assertTrue("Stop timeout reached", delay < timeOut);
    }
    
    
    @Test
    public void testAsyncRestart() throws Exception
    {
        final AsyncModuleConfig conf = new AsyncModuleConfig();
        conf.moduleClass = AsyncModule.class.getCanonicalName();
        conf.id = "MOD_ASYNC2";
        conf.autoStart = false;
        conf.name = "ModuleAsync2";
        conf.initDelay = 500;
        conf.initExecTime = 500;
        conf.startDelay = 50;
        conf.startExecTime = 100;
        long timeOut = 5000;
        
        IModule<?> module = loadModule(conf);
        module.init();
        
        // start module once
        module.start();
        module.waitForState(ModuleState.STARTED, timeOut);
        assertEquals("Module was not started", ModuleState.STARTED, module.getCurrentState());
        
        // now restart it
        conf.startEventReceived = false;
        long t0 = System.currentTimeMillis();
        module.stop();
        module.start();
        module.waitForState(ModuleState.STARTED, timeOut);
        long t1 = System.currentTimeMillis();
        
        long expectedDelay = conf.startDelay + conf.startExecTime;
        long delay = t1 - t0;
        //assertTrue("No STOPPED event received", conf.stopEventReceived);
        //assertTrue("No STARTED event received", conf.startEventReceived);
        assertTrue("Start never executed", delay >= expectedDelay);
        assertTrue("Start timeout reached", delay < timeOut);
    }
    
    
    @Test
    public void testAsyncInitTimeout() throws Exception
    {
        final AsyncModuleConfig conf = new AsyncModuleConfig();
        conf.moduleClass = AsyncModule.class.getCanonicalName();
        conf.id = "MOD_ASYNC2";
        conf.autoStart = false;
        conf.name = "ModuleAsync2";
        conf.useThreadForInit = true;
        conf.initDelay = 100;
        conf.initExecTime = 200;
        long timeOut = 100;
        
        IModule<?> module = loadModule(conf);        
        module.init();
        boolean noTimeOut = module.waitForState(ModuleState.INITIALIZED, timeOut);
        
        assertFalse("Init should have timeout", noTimeOut);
    }
    
    
    @Test
    public void testAsyncStartCalledTwice() throws Exception
    {
        final AsyncModuleConfig conf = new AsyncModuleConfig();
        conf.moduleClass = AsyncModule.class.getCanonicalName();
        conf.id = "MOD_ASYNC2";
        conf.autoStart = false;
        conf.name = "ModuleAsync2";
        conf.startDelay = 50;
        conf.startExecTime = 100;
        long timeOut = 2000;
        
        IModule<?> module = loadModule(conf);
        module.init();
        
        // start module once
        module.start();
        module.waitForState(ModuleState.STARTED, timeOut);
        assertEquals("Module was not started", ModuleState.STARTED, module.getCurrentState());
        //assertTrue("No STARTED event received", conf.startEventReceived);
        
        // start again
        long t0 = System.currentTimeMillis();
        module.start();
        assertEquals("Module should remain started", ModuleState.STARTED, module.getCurrentState());
        long t1 = System.currentTimeMillis();
        
        long delay = t1 - t0;
        assertFalse("Start executed twice", delay >= conf.startExecTime);
    }
    
    
    @Test(expected=SensorHubException.class)
    public void testErrorStartCalledWhileInitializing() throws Exception
    {
        final AsyncModuleConfig conf = new AsyncModuleConfig();
        conf.moduleClass = AsyncModule.class.getCanonicalName();
        conf.id = "MOD_ASYNC2";
        conf.autoStart = false;
        conf.name = "ModuleAsync2";
        conf.initDelay = 120;
        conf.initExecTime = 100;
        conf.useThreadForInit = true;
        conf.startDelay = 150;
        conf.startExecTime = 70;
        conf.useThreadForStart = true;
        
        IModule<?> module = loadModule(conf);
        
        // request async start during init
        module.init();
        module.start();
    }
}
