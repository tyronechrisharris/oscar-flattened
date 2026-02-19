/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sos;

import static org.junit.Assert.*;
import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sensorhub.api.client.ClientException;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.api.module.ModuleEvent;
import org.sensorhub.api.module.ModuleEvent.ModuleState;
import org.sensorhub.api.sensor.ISensorModule;
import org.sensorhub.api.sensor.SensorConfig;
import org.sensorhub.api.service.IHttpServer;
import org.sensorhub.impl.SensorHub;
import org.sensorhub.impl.client.sost.SOSTClient;
import org.sensorhub.impl.client.sost.SOSTClientConfig;
import org.sensorhub.impl.datastore.view.ObsSystemDatabaseViewConfig;
import org.sensorhub.impl.module.ModuleRegistry;
import org.sensorhub.impl.security.ClientAuth;
import org.sensorhub.impl.sensor.FakeSensor;
import org.sensorhub.impl.sensor.FakeSensorData;
import org.sensorhub.test.AsyncTests;
import org.vast.ows.GetCapabilitiesRequest;
import org.vast.ows.OWSUtils;
import org.vast.ows.sos.SOSOfferingCapabilities;
import org.vast.ows.sos.SOSServiceCapabilities;
import org.vast.ows.sos.SOSUtils;


public class TestSOSTClient
{
    static final long TIMEOUT = 5000L;
    static final String SENSOR_UID = "urn:test:newsensor:0002";
    static final double SAMPLING_PERIOD = 0.2;
    static final int NUM_GEN_SAMPLES = 4;
    
    TestSOSService sosTest;
    SensorHub hub;
    ModuleRegistry moduleRegistry;
    Exception asyncError;
    int recordCounter = 0;
    
    
    @Before
    public void setup() throws Exception
    {
        // start SOS service
        sosTest = new TestSOSService();
        sosTest.setup();
        
        // create separate hub for client
        hub = new SensorHub();
        hub.start();
        moduleRegistry = hub.getModuleRegistry();
        ClientAuth.createInstance(null);
    }
    
    
    protected FakeSensor buildSensor1(int numSamples) throws Exception
    {
        // create test sensor
        SensorConfig sensorCfg = new SensorConfig();
        sensorCfg.autoStart = false;
        sensorCfg.moduleClass = FakeSensor.class.getCanonicalName();
        sensorCfg.name = "Sensor1";
        FakeSensor sensor = (FakeSensor)moduleRegistry.loadModule(sensorCfg);
        sensor.init();
        sensor.setSensorUID(SENSOR_UID);
        sensor.setDataInterfaces(new FakeSensorData(sensor, TestSOSService.NAME_OUTPUT1, SAMPLING_PERIOD, numSamples));
        sensor.start();
        return sensor;
    }
    
    
    protected SOSTClient startClient(String sensorID, boolean async) throws Exception
    {
        return startClient(sensorID, async, false, 4);
    }
    
    
    protected SOSTClient startClient(String sensorUID, boolean async, boolean persistent, int maxAttempts) throws Exception
    {
        URL sosUrl = new URL(TestSOSService.HTTP_ENDPOINT);
        
        SOSTClientConfig config = new SOSTClientConfig();
        config.id = "SOST";
        config.name = "SOS-T Client";
        config.dataSourceSelector = new ObsSystemDatabaseViewConfig();
        config.dataSourceSelector.includeFilter = new SystemFilter.Builder()
            .withUniqueIDs(sensorUID)
            .build();
        config.sos.remoteHost = sosUrl.getHost();
        config.sos.remotePort = (sosUrl.getPort() > 0) ? sosUrl.getPort() : 80;
        config.sos.resourcePath = sosUrl.getPath();
        config.connection.connectTimeout = 1000;
        config.connection.reconnectPeriod = 500;
        config.connection.reconnectAttempts = maxAttempts;
        config.connection.checkReachability = false;
        config.connection.usePersistentConnection = persistent;
        config.connection.maxConnectErrors = 2;
        
        final SOSTClient client = (SOSTClient)moduleRegistry.loadModule(config);
        client.init();        
        client.start();
        
        if (!async)
            client.waitForState(ModuleState.STARTED, TIMEOUT);
        
        return client;
    }
    
    
    protected SOSOfferingCapabilities getCapabilities(int offeringIndex, long waitTimeOut) throws Exception
    {
        OWSUtils utils = new OWSUtils();
        
        // check capabilities has one more offering
        GetCapabilitiesRequest getCap = new GetCapabilitiesRequest();
        getCap.setService(SOSUtils.SOS);
        getCap.setVersion("2.0");
        getCap.setGetServer(TestSOSService.HTTP_ENDPOINT);
        
        long maxWait = System.currentTimeMillis() + waitTimeOut;
        SOSServiceCapabilities caps = null;
        int numOffering = 0;
        do
        {
            caps = (SOSServiceCapabilities)utils.sendRequest(getCap, false);
            numOffering = caps.getLayers().size();
            if (numOffering >= offeringIndex+1)
                break;
            if (waitTimeOut > 0)
                Thread.sleep(1000);
        }
        while (System.currentTimeMillis() < maxWait);
        
        //utils.writeXMLResponse(System.out, caps);
        assertTrue("No offering added", numOffering >= offeringIndex+1);
        return (SOSOfferingCapabilities)caps.getLayers().get(offeringIndex);
    }
    
    
    @Test
    public void testRegisterSync() throws Exception
    {
        // start service with SOS-T support
        sosTest.deployService(true, new SOSProviderConfig[0]);
     
        // start client
        ISensorModule<?> sensor = buildSensor1(NUM_GEN_SAMPLES);
        startClient(sensor.getUniqueIdentifier(), false);
        Thread.sleep(TestSOSService.CAPS_REFRESH_PERIOD);
        
        // check capabilities content
        SOSOfferingCapabilities newOffering = getCapabilities(0, 0);
        assertEquals(SENSOR_UID, newOffering.getMainProcedure());
    }
    
    
    @Test
    public void testRegisterErrorNoTransactionalServer() throws Exception
    {
        // start service w/o SOS-T support
        sosTest.deployService(false, new SOSProviderConfig[0]);
        
        // start client
        ISensorModule<?> sensor = buildSensor1(NUM_GEN_SAMPLES);
        var client = startClient(sensor.getUniqueIdentifier(), false);
        
        assertFalse(client.isStarted());
        assertTrue(client.getCurrentError() instanceof ClientException);
    }
    
    
    @Test
    public void testRegisterAsyncReconnect() throws Exception
    {
        // start client
        ISensorModule<?> sensor = buildSensor1(NUM_GEN_SAMPLES);
        startClient(sensor.getUniqueIdentifier(), true);
        Thread.sleep(100);
        
        // start service
        sosTest.deployService(true, new SOSProviderConfig[0]);
        
        // check capabilities content
        SOSOfferingCapabilities newOffering = getCapabilities(0, TIMEOUT);
        assertEquals(SENSOR_UID, newOffering.getMainProcedure());
    }
    
    
    @Test
    public void testRegisterAsyncReconnectNoServer() throws Exception
    {
        // start client
        ISensorModule<?> sensor = buildSensor1(NUM_GEN_SAMPLES);
        SOSTClient client = startClient(sensor.getUniqueIdentifier(), true, false, 3);
        
        // wait for exception
        long maxWait = System.currentTimeMillis() + TIMEOUT;
        while (client.getCurrentError() == null)
        {
            Thread.sleep(500);
            if (System.currentTimeMillis() > maxWait)
                fail("No connection error reported");
        }
    }
    
    
    @Test
    public void testInsertResultPost() throws Exception
    {
        // start service with SOS-T support
        sosTest.deployService(true, new SOSProviderConfig[0]);
        
        // start client
        FakeSensor sensor = buildSensor1(NUM_GEN_SAMPLES);
        startClient(sensor.getUniqueIdentifier(), false, false, 1);
        
        // send getResult request
        Future<String[]> f = sosTest.sendGetResultAsync(SENSOR_UID, 
                TestSOSService.URI_PROP1, TestSOSService.TIMERANGE_FUTURE, false);
        
        // start sensor
        sensor.startSendingData(200);        
        sosTest.checkGetResultResponse(f.get(), NUM_GEN_SAMPLES, 4);
    }
    
    
    @Test
    public void testInsertResultPersistentHttp() throws Exception
    {
        // start service with SOS-T support
        sosTest.deployService(true, new SOSProviderConfig[0]);
        
        // start client
        FakeSensor sensor = buildSensor1(NUM_GEN_SAMPLES);
        var client = startClient(sensor.getUniqueIdentifier(), false, true, 1);
        
        // send getResult request
        Future<String[]> f = sosTest.sendGetResultAsync(SENSOR_UID, 
                TestSOSService.URI_PROP1, TestSOSService.TIMERANGE_FUTURE, false);
        
        // start sensor
        sensor.startSendingData(200);        
        sosTest.checkGetResultResponse(f.get(), NUM_GEN_SAMPLES, 4);
        
        client.stop();
    }
    
    
    @Test
    public void testInsertResultReconnect() throws Exception
    {
        // start service with SOS-T support
        sosTest.deployService(true, new SOSProviderConfig[0]);
        
        // start client
        FakeSensor sensor = buildSensor1(25);
        SOSTClient client = startClient(sensor.getUniqueIdentifier(), false, true, 2);
        
        // start sensor
        sensor.startSendingData();
        Thread.sleep(500);
        
        AtomicInteger clientStopCount = new AtomicInteger();
        AtomicInteger clientRestartCount = new AtomicInteger();
        client.registerListener(event -> {
            if (event instanceof ModuleEvent) {
                if (((ModuleEvent) event).getNewState() == ModuleState.STOPPED)
                    clientStopCount.incrementAndGet();
                else if (((ModuleEvent) event).getNewState() == ModuleState.STARTING)
                    clientRestartCount.incrementAndGet();
            }
        });
        
        // stop server
        var httpServer = sosTest.moduleRegistry.getModuleByType(IHttpServer.class);
        httpServer.stop();
        
        AsyncTests.waitForCondition(() -> clientRestartCount.get() >= 1, TIMEOUT);
        
        assertEquals("SOS-T client should have stopped once", 1, clientStopCount.get());
        assertEquals("SOS-T client should restarted once", 1, clientRestartCount.get());
        
        AsyncTests.waitForCondition(() -> clientStopCount.get() >= 2, TIMEOUT);
        
        assertTrue("Client should have an error", client.getCurrentError() != null);
    }
    
   
    @After
    public void cleanup()
    {
        if (hub != null)
            hub.stop();        
        sosTest.cleanup();
    }
}
