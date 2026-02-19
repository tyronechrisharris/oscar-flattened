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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sensorhub.api.module.ModuleEvent.ModuleState;
import org.sensorhub.api.sensor.SensorConfig;
import org.sensorhub.impl.SensorHub;
import org.sensorhub.impl.database.system.SystemDriverDatabaseConfig;
import org.sensorhub.impl.datastore.h2.MVObsSystemDatabaseConfig;
import org.sensorhub.impl.module.ModuleRegistry;
import org.sensorhub.impl.sensor.FakeSensor;
import org.sensorhub.impl.sensor.FakeSensorData;
import org.sensorhub.impl.sensor.FakeSensorData2;
import org.sensorhub.impl.sensor.FakeSensorNetOnlyFois;
import org.sensorhub.impl.service.HttpServerConfig;
import org.sensorhub.impl.service.ogc.OGCServiceConfig.CapabilitiesInfo;
import org.sensorhub.impl.service.swe.SWEServlet;
import org.sensorhub.test.AsyncTests;
import org.vast.data.DataBlockDouble;
import org.vast.data.QuantityImpl;
import org.vast.data.TextEncodingImpl;
import org.vast.ogc.OGCException;
import org.vast.ogc.OGCExceptionReader;
import org.vast.ows.GetCapabilitiesRequest;
import org.vast.ows.OWSException;
import org.vast.ows.OWSExceptionReader;
import org.vast.ows.OWSRequest;
import org.vast.ows.OWSUtils;
import org.vast.ows.sos.GetFeatureOfInterestRequest;
import org.vast.ows.sos.GetObservationRequest;
import org.vast.ows.sos.InsertResultRequest;
import org.vast.ows.sos.SOSServiceCapabilities;
import org.vast.ows.swe.DescribeSensorRequest;
import org.vast.swe.SWEData;
import org.vast.util.Bbox;
import org.vast.util.DateTimeFormat;
import org.vast.util.TimeExtent;
import org.vast.xml.DOMHelper;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.google.common.collect.Sets;


public class TestSOSService
{
    static final long TIMEOUT = 5000L;
    static final long CAPS_REFRESH_PERIOD = SWEServlet.GET_CAPS_MIN_REFRESH_PERIOD; // time to wait until capabilities are refreshed
    static final String ID_SENSOR_MODULE1 = "dfc5249b-2e7d-4fcb-8dc2-33186668fbs1";
    static final String ID_SENSOR_MODULE2 = "26f00c5d-c18f-46dc-ac71-c0c10efc08s2";
    static final String UID_SENSOR1 = "urn:sensors:mysensor:001";
    static final String UID_SENSOR2 = "urn:sensors:mysensornet:002";
    static final String URI_OFFERING1 = UID_SENSOR1;//"urn:mysos:sensor1";
    static final String URI_OFFERING2 = UID_SENSOR2;//"urn:mysos:sensor2";
    static final String NAME_OUTPUT1 = "weatherOut";
    static final String NAME_OUTPUT2 = "imageOut";
    static final String URI_PROP1 = FakeSensorData.URI_OUTPUT1;
    static final String URI_PROP1_FIELD1 = FakeSensorData.URI_OUTPUT1_FIELD1;
    static final String URI_PROP1_FIELD2 = FakeSensorData.URI_OUTPUT1_FIELD3;
    static final String URI_PROP2 = FakeSensorData2.URI_OUTPUT1;
    static final String NAME_OFFERING1 = "SOS Sensor Provider #1";
    static final String NAME_OFFERING2 = "SOS Sensor Provider #2";
    static final double SAMPLING_PERIOD = 0.1;
    static final int NUM_GEN_SAMPLES = 5;
    static final int NUM_GEN_FEATURES = 3;
    static final int SERVER_PORT = 8888;
    static final String SERVICE_PATH = "/sos";
    static final String HTTP_ENDPOINT = "http://localhost:" + SERVER_PORT + "/sensorhub" + SERVICE_PATH;
    static final String WS_ENDPOINT = HTTP_ENDPOINT.replace("http://", "ws://"); 
    static final String GETCAPS_REQUEST = "?service=SOS&version=2.0&request=GetCapabilities";
    static final String OFFERING_NODES = "contents/Contents/offering/*";
    static final String TIMERANGE_FUTURE = "now/2080-01-01Z";
    static final String TIMERANGE_NOW = "now";
    
    
    SensorHub hub;
    ModuleRegistry moduleRegistry;
    File dbFile1, dbFile2;
    NavigableMap<Integer, Integer> obsFoiMap = new TreeMap<>();
        
    
    @Before
    public void setup() throws Exception
    {
        // use temp DB files
        dbFile1 = File.createTempFile("osh-db1-", ".dat");//new File(DB_PATH);
        dbFile1.deleteOnExit();
        dbFile2 = File.createTempFile("osh-db2-", ".dat");//new File(DB_PATH);
        dbFile2.deleteOnExit();
        
        // get instance with in-memory DB
        hub = new SensorHub();
        hub.start();
        moduleRegistry = hub.getModuleRegistry();
        
        // start HTTP server
        HttpServerConfig httpConfig = new HttpServerConfig();
        httpConfig.httpPort = SERVER_PORT;
        moduleRegistry.loadModule(httpConfig, TIMEOUT);
    }
    
    
    protected SOSService deployService(SOSProviderConfig... providerConfigs) throws Exception
    {
        return deployService(false, providerConfigs);
    }
    
    
    protected SOSService deployService(boolean enableSOST, SOSProviderConfig... providerConfigs) throws Exception
    {
        // create service config
        SOSServiceConfig serviceCfg = new SOSServiceConfig();
        serviceCfg.moduleClass = SOSService.class.getCanonicalName();
        serviceCfg.endPoint = SERVICE_PATH;
        serviceCfg.autoStart = true;
        serviceCfg.name = "SOS";
        serviceCfg.customFormats.clear();
        serviceCfg.defaultLiveTimeout = 2.0;
        CapabilitiesInfo srvcMetadata = serviceCfg.ogcCapabilitiesInfo;
        srvcMetadata.title = "My SOS Service";
        srvcMetadata.description = "An SOS service automatically deployed by SensorHub";
        srvcMetadata.serviceProvider.setOrganizationName("Test Provider, Inc.");
        srvcMetadata.serviceProvider.setDeliveryPoint("15 MyStreet");
        srvcMetadata.serviceProvider.setCity("MyCity");
        srvcMetadata.serviceProvider.setCountry("MyCountry");
        serviceCfg.customDataProviders.addAll(Arrays.asList(providerConfigs));
        srvcMetadata.fees = "NONE";
        srvcMetadata.accessConstraints = "NONE";
        
        // if testing SOS-T, we need a database to write to
        if (enableSOST)
        {
            MVObsSystemDatabaseConfig dbConfig = new MVObsSystemDatabaseConfig();
            dbConfig.name = "H2 Storage";
            dbConfig.autoStart = true;
            dbConfig.databaseNum = 10;
            dbConfig.storagePath = dbFile1.getAbsolutePath();
            dbConfig.readOnly = false;
            
            // start storage module
            var storage = moduleRegistry.loadModuleAsync(dbConfig, null);
            storage.waitForState(ModuleState.STARTED, TIMEOUT);
            
            serviceCfg.enableTransactional = true;
            serviceCfg.databaseID = storage.getLocalID();
        }
        
        // start module
        SOSService sos = (SOSService)moduleRegistry.loadModule(serviceCfg, TIMEOUT);
        
        // save config
        moduleRegistry.saveModulesConfiguration();
        
        return sos;
    }
    
    
    protected SystemDataProviderConfig buildSensorProvider1() throws Exception
    {
        return buildSensorProvider1(true, true);
    }
    
    
    protected SystemDataProviderConfig buildSensorProvider1(boolean start, boolean startSending) throws Exception
    {
        // create test sensor
        SensorConfig sensorCfg = new SensorConfig();
        sensorCfg.id = ID_SENSOR_MODULE1;
        sensorCfg.autoStart = false;
        sensorCfg.moduleClass = FakeSensor.class.getCanonicalName();
        sensorCfg.name = "Sensor1";
        FakeSensor sensor = (FakeSensor)moduleRegistry.loadModule(sensorCfg);
        sensor.init();
        sensor.setSensorUID(UID_SENSOR1);
        sensor.setDataInterfaces(new FakeSensorData(sensor, NAME_OUTPUT1, SAMPLING_PERIOD, NUM_GEN_SAMPLES));
        if (start)
        {
            moduleRegistry.startModule(sensorCfg.id, TIMEOUT);
            if (startSending)
                sensor.startSendingData();
        }
        
        // create SOS data provider config
        SystemDataProviderConfig provCfg = new SystemDataProviderConfig();
        provCfg.enabled = true;
        provCfg.name = NAME_OFFERING1;
        provCfg.systemUID = sensor.getUniqueIdentifier();
        provCfg.liveDataTimeout = 1.0;
        
        return provCfg;
    }
    
    
    protected SystemDataProviderConfig buildSensorProvider2() throws Exception
    {
        return buildSensorProvider2(true, true);
    }
    
    
    protected SystemDataProviderConfig buildSensorProvider2(boolean start, boolean startSending) throws Exception
    {
        // create test sensor
        SensorConfig sensorCfg = new SensorConfig();
        sensorCfg.id = ID_SENSOR_MODULE2;
        sensorCfg.autoStart = false;
        sensorCfg.moduleClass = FakeSensorNetOnlyFois.class.getCanonicalName();
        sensorCfg.name = "Sensor2";
        var sensorNet = (FakeSensorNetOnlyFois)moduleRegistry.loadModule(sensorCfg);
        sensorNet.init();
        sensorNet.setSensorUID(UID_SENSOR2);
        sensorNet.addFois(obsFoiMap.size());
        sensorNet.setDataInterfaces(new FakeSensorData2(sensorNet, NAME_OUTPUT2, SAMPLING_PERIOD, NUM_GEN_SAMPLES, obsFoiMap));
        if (start)
        {
            moduleRegistry.startModule(sensorCfg.id, TIMEOUT);
            if (startSending)
                sensorNet.startSendingData();
        }
        
        // create SOS data provider config
        SystemDataProviderConfig provCfg = new SystemDataProviderConfig();
        provCfg.enabled = true;
        provCfg.name = NAME_OFFERING2;
        provCfg.systemUID = sensorNet.getUniqueIdentifier();
        provCfg.liveDataTimeout = 1.0;
        
        return provCfg;
    }
    
    
    protected SystemDataProviderConfig buildSensorProvider1WithStorage() throws Exception
    {
        return buildSensorProvider1WithStorage(true, true);
    }
    
    
    protected SystemDataProviderConfig buildSensorProvider1WithStorage(boolean start, boolean startSending) throws Exception
    {
        // configure H2 database
        SystemDriverDatabaseConfig streamStorageConfig = new SystemDriverDatabaseConfig();
        streamStorageConfig.name = "H2 Storage";
        streamStorageConfig.autoStart = true;
        streamStorageConfig.databaseNum = 1;
        streamStorageConfig.systemUIDs.add(UID_SENSOR1);
        streamStorageConfig.dbConfig = new MVObsSystemDatabaseConfig();
        ((MVObsSystemDatabaseConfig)streamStorageConfig.dbConfig).storagePath = dbFile1.getAbsolutePath();
        ((MVObsSystemDatabaseConfig)streamStorageConfig.dbConfig).readOnly = false;
        
        // start storage module
        var storage = moduleRegistry.loadModuleAsync(streamStorageConfig, null);
        storage.waitForState(ModuleState.STARTED, TIMEOUT);
        
        // create sensor & provider
        SystemDataProviderConfig sosProviderConfig = buildSensorProvider1(start, startSending);
        
        return sosProviderConfig;
    }
    
    
    protected SystemDataProviderConfig buildSensorProvider2WithObsStorage() throws Exception
    {
        return buildSensorProvider2WithObsStorage(true, true);
    }
    
    
    protected SystemDataProviderConfig buildSensorProvider2WithObsStorage(boolean start, boolean startSending) throws Exception
    {
        // configure H2 database
        SystemDriverDatabaseConfig streamStorageConfig = new SystemDriverDatabaseConfig();
        streamStorageConfig.name = "H2 Storage";
        streamStorageConfig.autoStart = true;
        streamStorageConfig.databaseNum = 2;
        streamStorageConfig.systemUIDs.add(UID_SENSOR2);
        streamStorageConfig.dbConfig = new MVObsSystemDatabaseConfig();
        ((MVObsSystemDatabaseConfig)streamStorageConfig.dbConfig).storagePath = dbFile2.getAbsolutePath();
        ((MVObsSystemDatabaseConfig)streamStorageConfig.dbConfig).readOnly = false;
        
        // start storage module
        var storage = moduleRegistry.loadModuleAsync(streamStorageConfig, null);
        storage.waitForState(ModuleState.STARTED, TIMEOUT);
        
        // create sensor & provider
        SystemDataProviderConfig sosProviderConfig = buildSensorProvider2(start, startSending);
        
        return sosProviderConfig;
    }
    
    
    protected FakeSensor startSensor(String sensorID) throws Exception
    {
        return (FakeSensor)moduleRegistry.startModule(sensorID);        
    }
    
    
    protected FakeSensor startSendingData(String sensorID) throws Exception
    {
        return startSendingData(sensorID, 0L);
    }
    
    
    protected FakeSensor startSendingData(String sensorID, long delay) throws Exception
    {
        FakeSensor sensor = (FakeSensor)moduleRegistry.getModuleById(sensorID);
        sensor.startSendingData(delay);
        return sensor;
    }
        
        
    protected FakeSensor startSendingAndWaitForAllRecords(String sensorID) throws Exception
    {
        FakeSensor sensor = startSendingData(sensorID);
        AsyncTests.waitForCondition(() -> !sensor.hasMoreData(), TIMEOUT);
        return sensor;
    }
    
    
    protected DOMHelper sendRequest(OWSRequest request, boolean usePost) throws Exception
    {
        OWSUtils utils = new OWSUtils();
        InputStream is;
        
        if (usePost)
        {
            utils.writeXMLQuery(System.out, request);
            is = utils.sendPostRequest(request).getInputStream();            
        }
        else
        {
            System.out.println(utils.buildURLQuery(request));
            is = utils.sendGetRequest(request).getInputStream();
        }
        
        DOMHelper dom = new DOMHelper(is, false);
        dom.serialize(dom.getBaseElement(), System.out, true);
        OWSExceptionReader.checkException(dom, dom.getBaseElement());
        return dom;
    }
    
    
    protected Future<DOMHelper> sendRequestAsync(OWSRequest request, boolean usePost) throws Exception
    {
        return CompletableFuture.supplyAsync(() -> {
            try { return sendRequest(request, usePost); }
            catch (Exception e) { throw new CompletionException(e); }
        });
    }
    
    
    protected DOMHelper sendSoapRequest(OWSRequest request) throws Exception
    {
        OWSUtils utils = new OWSUtils();
        InputStream is = utils.sendSoapRequest(request).getInputStream();
        DOMHelper dom = new DOMHelper(is, false);
        dom.serialize(dom.getBaseElement(), System.out, true);
        OWSExceptionReader.checkException(dom, dom.getBaseElement());
        return dom;
    }
    
    
    protected void checkServiceException(InputStream is, String locator) throws Exception
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        IOUtils.copy(is, os);
        
        try
        {
            ByteArrayInputStream bis = new ByteArrayInputStream(os.toByteArray());
            OGCExceptionReader.parseException(bis);
            fail("Expected service exception"); // we should never be here
        }
        catch (OGCException e)
        {
            String exceptionXml = os.toString(StandardCharsets.UTF_8.name());
            assertTrue("Wrong exception:\n" + exceptionXml, exceptionXml.contains("locator=\"" + locator + "\""));
        }
    }
    
    
    @Test
    public void testSetupService() throws Exception
    {
        deployService(buildSensorProvider1());
        
        var sosList = moduleRegistry.getLoadedModules(SOSService.class);
        assertEquals("No SOS service deployed", 1, sosList.size());
        
        assertTrue(sosList.iterator().next().isStarted());
    }
    
    
    @Test(expected=OWSException.class)
    public void testNoTransactional() throws Exception
    {
        deployService(buildSensorProvider1());
        
        InsertResultRequest req = new InsertResultRequest();
        req.setPostServer(HTTP_ENDPOINT);
        req.setVersion("2.0");
        req.setTemplateId("template01");
        SWEData sweData = new SWEData();
        sweData.setElementType(new QuantityImpl());
        sweData.setEncoding(new TextEncodingImpl(",", " "));
        sweData.addData(new DataBlockDouble(1));
        req.setResultData(sweData);
        new OWSUtils().sendRequest(req, false);
    }

    
    protected DOMHelper checkOfferings(InputStream is, String... sensorUIDs) throws Exception
    {
        DOMHelper dom = new DOMHelper(is, false);
        checkOfferings(dom, dom.getBaseElement(), sensorUIDs);
        return dom;
    }
    
    
    protected void checkOfferings(DOMHelper dom, Element baseElt, String... sensorUIDs) throws Exception
    {
        dom.serialize(baseElt, System.out, true);        
        NodeList offeringElts = dom.getElements(baseElt, OFFERING_NODES);
        assertEquals("Wrong number of offerings", sensorUIDs.length, offeringElts.getLength());
        
        var sensorUIDSet = Sets.newHashSet(sensorUIDs);
        for (int i = 0; i < offeringElts.getLength(); i++)
        {
            var offeringElt = (Element)offeringElts.item(i);            
            var offeringID = dom.getElementValue(offeringElt, "identifier");
            var sysUID = dom.getElementValue(offeringElt, "procedure");
            assertTrue("Wrong offering: " + offeringID, sensorUIDSet.contains(offeringID));            
            assertTrue("Wrong procedure: " + sysUID, sensorUIDSet.contains(sysUID));
        }
    }
    
    
    @Test
    public void testGetCapabilitiesOneOffering1() throws Exception
    {
        deployService(buildSensorProvider1());
        InputStream is = new URL(HTTP_ENDPOINT + GETCAPS_REQUEST).openStream();
        checkOfferings(is, new String[] {UID_SENSOR1});
    }
    
    
    @Test
    public void testGetCapabilitiesTwoOfferings() throws Exception
    {
        deployService(buildSensorProvider1(), buildSensorProvider2());
        InputStream is = new URL(HTTP_ENDPOINT + GETCAPS_REQUEST).openStream();
        checkOfferings(is, new String[] {UID_SENSOR1, UID_SENSOR2});
    }
    
    
    @Test
    public void testGetCapabilitiesSoap12() throws Exception
    {
        deployService(buildSensorProvider1());
        
        GetCapabilitiesRequest getCaps = new GetCapabilitiesRequest();
        getCaps.setPostServer(HTTP_ENDPOINT);
        getCaps.setSoapVersion(OWSUtils.SOAP12_URI);
        DOMHelper dom = sendSoapRequest(getCaps);
        
        assertEquals(OWSUtils.SOAP12_URI, dom.getBaseElement().getNamespaceURI());
                
        Element capsElt = dom.getElement("Body/Capabilities");
        checkOfferings(dom, capsElt, new String[] {UID_SENSOR1});
    }
    
    
    @Test
    public void testGetCapabilitiesSoap11() throws Exception
    {
        deployService(buildSensorProvider1(), buildSensorProvider2());
        
        GetCapabilitiesRequest getCaps = new GetCapabilitiesRequest();
        getCaps.setPostServer(HTTP_ENDPOINT);
        getCaps.setSoapVersion(OWSUtils.SOAP11_URI);
        DOMHelper dom = sendSoapRequest(getCaps);
        
        assertEquals(OWSUtils.SOAP11_URI, dom.getBaseElement().getNamespaceURI());
        
        Element capsElt = dom.getElement("Body/Capabilities");
        checkOfferings(dom, capsElt, new String[] {UID_SENSOR1, UID_SENSOR2});
    }
    
    
    protected void checkOfferingTimeRange(DOMHelper dom, String sysUID, String expectedBeginValue, String expectedEndValue) throws ParseException
    {
        NodeList offeringElts = dom.getElements(OFFERING_NODES);
        Element offeringElt = null;
        for (int i = 0; i < offeringElts.getLength(); i++)
        {
            offeringElt = (Element)offeringElts.item(i);
            if (sysUID.equals(dom.getElementValue(offeringElt, "procedure")))
                break;
        }
        
        boolean isBeginIso = Character.isDigit(expectedBeginValue.charAt(0));
        if (isBeginIso)
        {
            String isoText = dom.getElementValue(offeringElt, "phenomenonTime/TimePeriod/beginPosition");
            double time = new DateTimeFormat().parseIso(isoText);
            double expectedTime = new DateTimeFormat().parseIso(expectedBeginValue);
            assertEquals("Wrong begin time " + isoText, expectedTime, time, 10.0);
        }
        else
            assertEquals("Wrong begin time", expectedBeginValue, dom.getAttributeValue(offeringElt, "phenomenonTime/TimePeriod/beginPosition/indeterminatePosition"));
            
        boolean isEndIso = Character.isDigit(expectedEndValue.charAt(0));
        if (isEndIso)
        {
            String isoText = dom.getElementValue(offeringElt, "phenomenonTime/TimePeriod/endPosition");
            double time = new DateTimeFormat().parseIso(isoText);
            double expectedTime = new DateTimeFormat().parseIso(expectedEndValue);
            assertEquals("Wrong end time " + isoText, expectedTime, time, 10.0);
        }
        else
            assertEquals("Wrong end time", expectedEndValue, dom.getAttributeValue(offeringElt, "phenomenonTime/TimePeriod/endPosition/indeterminatePosition"));
    }
    
    
    @Test
    public void testGetCapabilitiesLiveTimeRange() throws Exception
    {
        var provider1 = buildSensorProvider1(false, false);
        var provider2 = buildSensorProvider2(true, false);
        provider1.liveDataTimeout = 2.0;
        provider2.liveDataTimeout = 1.0;
        final SOSService sos = deployService(provider2, provider1);
        
        // wait for timeout
        Thread.sleep(((long)(provider2.liveDataTimeout*1000)));
        
        // sensor1 is not started, sensor2 is started but not sending data
        InputStream is = new URL(HTTP_ENDPOINT + GETCAPS_REQUEST).openStream();
        DOMHelper dom = checkOfferings(is, UID_SENSOR2);
        checkOfferingTimeRange(dom, UID_SENSOR2, "unknown", "unknown");
        
        // start sensor1
        moduleRegistry.startModule(ID_SENSOR_MODULE1);
        AsyncTests.waitForCondition(() -> sos.getCapabilities().getLayers().size() == 2, TIMEOUT);
        
        is = new URL(HTTP_ENDPOINT + GETCAPS_REQUEST).openStream();
        dom = checkOfferings(is, UID_SENSOR2, UID_SENSOR1);
        checkOfferingTimeRange(dom, UID_SENSOR1, "unknown", "unknown");
        checkOfferingTimeRange(dom, UID_SENSOR2, "unknown", "unknown");
        
        // trigger measurements from sensor1, wait for measurements and check capabilities again
        var sensor1 = startSendingData(ID_SENSOR_MODULE1);
        var begin1 = Instant.now();
        AsyncTests.waitForCondition(() -> sensor1.getOutputs().get(NAME_OUTPUT1).getLatestRecord() != null, 1000L);
        Thread.sleep(CAPS_REFRESH_PERIOD);
        is = new URL(HTTP_ENDPOINT + GETCAPS_REQUEST).openStream();
        dom = checkOfferings(is, UID_SENSOR1, UID_SENSOR2);
        checkOfferingTimeRange(dom, UID_SENSOR1, begin1.toString(), "now");
        checkOfferingTimeRange(dom, UID_SENSOR2, "unknown", "unknown");
        
        // trigger measurements from sensor2, wait for measurements and check capabilities again
        FakeSensor sensor2 = (FakeSensor)startSendingData(ID_SENSOR_MODULE2);
        var begin2 = Instant.now();
        AsyncTests.waitForCondition(() -> sensor2.getOutputs().get(NAME_OUTPUT2).getLatestRecord() != null, 1000L);
        Thread.sleep(CAPS_REFRESH_PERIOD);
        is = new URL(HTTP_ENDPOINT + GETCAPS_REQUEST).openStream();
        dom = checkOfferings(is, UID_SENSOR1, UID_SENSOR2);
        checkOfferingTimeRange(dom, UID_SENSOR2, begin2.toString(), "now");
        
        // wait until timeout
        AsyncTests.waitForCondition(() -> !sensor1.hasMoreData() && !sensor2.hasMoreData(), TIMEOUT);
        var end = Instant.now();
        Thread.sleep((long)(provider1.liveDataTimeout*1000));
        Thread.sleep(CAPS_REFRESH_PERIOD);
        is = new URL(HTTP_ENDPOINT + GETCAPS_REQUEST).openStream();
        dom = checkOfferings(is, UID_SENSOR1, UID_SENSOR2);
        checkOfferingTimeRange(dom, UID_SENSOR1, begin1.toString(), end.toString());
        checkOfferingTimeRange(dom, UID_SENSOR2, begin2.toString(), end.toString());
    }
    
    
    @Test
    public void testGetCapabilitiesLiveAndHistorical() throws Exception
    {
        var provider1 = buildSensorProvider1WithStorage(false, false);
        var provider2 = buildSensorProvider2WithObsStorage(true, true);
        provider1.liveDataTimeout = 100.0;
        provider2.liveDataTimeout = 100.0;
        deployService(provider2, provider1);
        
        // wait for at least one record to be in storage
        Thread.sleep(((long)(SAMPLING_PERIOD*1000)));
        
        // sensor1 not sending, sensor2 sending data
        InputStream is = new URL(HTTP_ENDPOINT + GETCAPS_REQUEST).openStream();
        DOMHelper dom = checkOfferings(is, UID_SENSOR2);
        String currentIsoTime = new DateTimeFormat().formatIso(System.currentTimeMillis()/1000., 0);
        checkOfferingTimeRange(dom, UID_SENSOR2, currentIsoTime, "now");
        
        // start sensor1 and wait for at least one record to be in storage
        startSensor(ID_SENSOR_MODULE1);
        startSendingData(ID_SENSOR_MODULE1);
        Thread.sleep(CAPS_REFRESH_PERIOD);
        
        is = new URL(HTTP_ENDPOINT + GETCAPS_REQUEST).openStream();
        dom = checkOfferings(is, UID_SENSOR2, UID_SENSOR1);
        currentIsoTime = new DateTimeFormat().formatIso(System.currentTimeMillis()/1000., 0);
        checkOfferingTimeRange(dom, UID_SENSOR1, currentIsoTime, "now");
        checkOfferingTimeRange(dom, UID_SENSOR2, currentIsoTime, "now");
    }
    
    
    @Test
    public void testGetCapabilitiesLiveAndHistoricalAfterTimeOut() throws Exception
    {
        var provider1 = buildSensorProvider1(true, true);
        var provider2 = buildSensorProvider2WithObsStorage(true, true);
        provider1.liveDataTimeout = 1.0;
        provider2.liveDataTimeout = 100.0;
        deployService(provider2, provider1);
        
        // wait for time out from sensor1
        FakeSensor sensor1 = getSensorModule(ID_SENSOR_MODULE1);
        AsyncTests.waitForCondition(() -> !sensor1.hasMoreData(), TIMEOUT);
        Thread.sleep((long)(provider1.liveDataTimeout*1000));
        Thread.sleep(CAPS_REFRESH_PERIOD);
        InputStream is = new URL(HTTP_ENDPOINT + GETCAPS_REQUEST).openStream();
        DOMHelper dom = checkOfferings(is, UID_SENSOR2, UID_SENSOR1);
        String currentIsoTime = new DateTimeFormat().formatIso(System.currentTimeMillis()/1000., 0);
        checkOfferingTimeRange(dom, UID_SENSOR1, currentIsoTime, currentIsoTime);
        checkOfferingTimeRange(dom, UID_SENSOR2, currentIsoTime, "now");
    }
    
    
    @Test
    public void testDescribeSensor() throws Exception
    {
        deployService(buildSensorProvider1(), buildSensorProvider2());
        OWSRequest dsReq;
        DOMHelper dom;
        
        dsReq = generateDescribeSensor(UID_SENSOR1);
        dom = sendRequest(dsReq, false);        
        assertEquals(UID_SENSOR1, dom.getElementValue("description/SensorDescription/data/PhysicalSystem/identifier"));
        
        dsReq = generateDescribeSensor(UID_SENSOR2);
        dom = sendRequest(dsReq, false);        
        assertEquals(UID_SENSOR2, dom.getElementValue("description/SensorDescription/data/PhysicalSystem/identifier"));
    }
    
    
    @Test
    public void testDescribeSensorSoap11() throws Exception
    {
        deployService(buildSensorProvider1(), buildSensorProvider2());
        
        OWSRequest request = generateDescribeSensor(UID_SENSOR1);
        request.setSoapVersion(OWSUtils.SOAP11_URI);
        DOMHelper dom = sendSoapRequest(request);
        
        assertEquals(OWSUtils.SOAP11_URI, dom.getBaseElement().getNamespaceURI());        
        assertEquals(UID_SENSOR1, dom.getElementValue("Body/DescribeSensorResponse/description/SensorDescription/data/PhysicalSystem/identifier"));
    }
    
    
    protected String[] sendGetResult(String offering, String observables, String timeRange) throws Exception
    {
        return sendGetResult(offering, observables, timeRange, false);
    }
    
    
    protected String[] sendGetResult(String offering, String observables, String timeRange, boolean useWebsocket) throws Exception
    {
        String url = (useWebsocket ? WS_ENDPOINT : HTTP_ENDPOINT) + 
                "?service=SOS&version=2.0&request=GetResult" + 
                "&offering=" + offering +
                "&observedProperty=" + observables + 
                "&temporalfilter=time," + timeRange;
        
        String currentTime = new DateTimeFormat().formatIso(System.currentTimeMillis()/1000., 0);
        
        if (useWebsocket)
        {
            WebSocketClient client = new WebSocketClient();
            final ReentrantLock lock = new ReentrantLock();
            final Condition endData = lock.newCondition();
            
            class MyWsHandler extends WebSocketAdapter
            {
                ArrayList<String> records = new ArrayList<String>();
                
                public void onWebSocketBinary(byte payload[], int offset, int len)
                {
                    String rec = new String(payload, offset, len);
                    System.out.print("Received from WS: " + rec);
                    records.add(rec);
                }

                public void onWebSocketClose(int arg0, String arg1)
                {
                    lock.lock();
                    try { endData.signalAll(); }
                    finally { lock.unlock(); }
                }            
            };
            
            System.out.println("Sending WebSocket request @ " + currentTime);
            MyWsHandler wsHandler = new MyWsHandler();
            client.start();
            client.connect(wsHandler, new URI(url));
            
            lock.lock();
            try { assertTrue("No data received before timeout", endData.await(5, TimeUnit.SECONDS)); }
            finally { lock.unlock(); }
            
            return wsHandler.records.toArray(new String[0]);
        }
        else
        {
            System.out.println("Sending HTTP GET request @ " + currentTime);
            InputStream is = new URL(url).openStream();
            
            var os = new ByteArrayOutputStream();
            IOUtils.copy(is, os);
            var respString = new String(os.toByteArray());
                        
            assertFalse("Unexpected XML response received:\n" + respString, respString.startsWith("<?xml"));        
            assertFalse("Response is empty", os.size() == 0);
            
            if (URI_PROP2.equals(observables))
            {
                var numRecords = os.size() / FakeSensorData2.ARRAY_SIZE / 3;
                return new String[numRecords];
            }
            else
            {
                
                System.out.println("Received from HTTP:\n" + respString);
                return respString.split("\n");
            }
        }
    }
    
    
    protected Future<String[]> sendGetResultAsync(final String offering, final String observables, final String timeRange, final boolean useWebsocket) throws Exception
    {
        return CompletableFuture.supplyAsync(() -> {
            try { return sendGetResult(offering, observables, timeRange, useWebsocket); }
            catch (Exception e) { throw new CompletionException(e); }
        });
    }
    
    
    protected void checkGetResultResponse(String[] records, int expectedNumRecords, int expectedNumFields)
    {
        assertEquals("Wrong number of records", expectedNumRecords, records.length);
        
        for (String rec: records)
        {
            if (rec != null) // null in case of binary record
            {
                String[] fields = rec.split(",");
                assertEquals("Wrong number of record fields", expectedNumFields, fields.length);
            }
        }
    }
    
    
    @Test
    public void testGetResultNow() throws Exception
    {
        deployService(buildSensorProvider1());
        var sensor1 = getSensorModule(ID_SENSOR_MODULE1);
        AsyncTests.waitForCondition(() -> !sensor1.hasMoreData(), TIMEOUT);
        
        String[] records = sendGetResult(URI_OFFERING1, URI_PROP1_FIELD2, TIMERANGE_NOW);
        checkGetResultResponse(records, 1, 2);
    }
    
    
    @Test
    public void testGetResultNowOneFoi() throws Exception
    {
        deployService(buildSensorProvider2());
        var sensor1 = getSensorModule(ID_SENSOR_MODULE2);
        AsyncTests.waitForCondition(() -> !sensor1.hasMoreData(), TIMEOUT);
        
        String[] records = sendGetResult(URI_OFFERING2, URI_PROP2, TIMERANGE_NOW);
        checkGetResultResponse(records, 1, 1);
    }
    
    
    @Test
    public void testGetResultNowMultiFoi() throws Exception
    {
        obsFoiMap.put(1, 1);
        obsFoiMap.put(3, 2);
        obsFoiMap.put(4, 3);
        
        deployService(buildSensorProvider2());
        var sensor1 = getSensorModule(ID_SENSOR_MODULE2);
        AsyncTests.waitForCondition(() -> !sensor1.hasMoreData(), TIMEOUT);
        
        String[] records = sendGetResult(URI_OFFERING2, URI_PROP2, TIMERANGE_NOW);
        checkGetResultResponse(records, 3, 2);
    }
    
    
    @Test
    public void testGetResultRealTimeAllObservables() throws Exception
    {
        deployService(buildSensorProvider1(true, false));      
        
        var future = sendGetResultAsync(URI_OFFERING1, URI_PROP1, TIMERANGE_FUTURE, false);
        startSendingData(ID_SENSOR_MODULE1, 100);
        
        String[] records = future.get();
        checkGetResultResponse(records, NUM_GEN_SAMPLES, 4);
    }
    
    
    @Test
    public void testGetResultRealTimeOneObservable() throws Exception
    {
        deployService(buildSensorProvider1(true, false));
                
        var future = sendGetResultAsync(URI_OFFERING1, URI_PROP1_FIELD1, TIMERANGE_FUTURE, false);
        startSendingData(ID_SENSOR_MODULE1, 100);
        
        String[] records = future.get();
        checkGetResultResponse(records, NUM_GEN_SAMPLES, 2);
    }
    
    
    @Test
    public void testGetResultRealTimeTwoObservables() throws Exception
    {
        deployService(buildSensorProvider1(true, false));
        
        var future = sendGetResultAsync(URI_OFFERING1, URI_PROP1_FIELD1 + "," + URI_PROP1_FIELD2, TIMERANGE_FUTURE, false);
        startSendingData(ID_SENSOR_MODULE1, 100);
        
        String[] records = future.get();
        checkGetResultResponse(records, NUM_GEN_SAMPLES, 3);
    }
    
    
    @Test
    public void testGetResultRealTimeTwoOfferings() throws Exception
    {
        deployService(buildSensorProvider1(true, false), buildSensorProvider2(true, false));
        
        var future = sendGetResultAsync(URI_OFFERING1, URI_PROP1, TIMERANGE_FUTURE, false);
        startSendingData(ID_SENSOR_MODULE1, 100);
        
        String[] records = future.get();
        checkGetResultResponse(records, NUM_GEN_SAMPLES, 4);
        
        // now get data for 2nd offering
        future = sendGetResultAsync(URI_OFFERING2, URI_PROP2, TIMERANGE_FUTURE, false);
        startSendingData(ID_SENSOR_MODULE2, 100);
        
        records = future.get();
        checkGetResultResponse(records, NUM_GEN_SAMPLES, 4);
    }
    
    
    @Test
    public void testGetResultBeforeDataIsAvailable() throws Exception
    {
        deployService(buildSensorProvider1(true, false));
        
        Future<String[]> future = sendGetResultAsync(URI_OFFERING1, URI_PROP1_FIELD1, TIMERANGE_FUTURE, false);
        
        // start sending data after a small pause
        startSendingData(ID_SENSOR_MODULE1, 500);

        try
        {
            String[] records = future.get(5, TimeUnit.SECONDS);
            checkGetResultResponse(records, NUM_GEN_SAMPLES, 2);
        }
        catch (Exception e)
        {
            assertTrue("No data received before timeout", false);
        }        
    }
    
    
    @Test
    public void testGetResultWrongOffering() throws Exception
    {
        deployService(buildSensorProvider1(), buildSensorProvider2());
        
        var conn = (HttpURLConnection)new URL(HTTP_ENDPOINT + 
                "?service=SOS&version=2.0&request=GetResult"
                + "&offering=urn:mysos:wrong"
                + "&observedProperty=urn:blabla:temperature").openConnection();
        
        conn.getResponseCode(); // force connection and read
        checkServiceException(conn.getErrorStream(), "offering");
    }
    
    
    @Test
    public void testGetResultWrongObservable() throws Exception
    {
        deployService(buildSensorProvider1(), buildSensorProvider2());
        
        var conn = (HttpURLConnection)new URL(HTTP_ENDPOINT + 
                "?service=SOS&version=2.0&request=GetResult"
                + "&offering=" + URI_OFFERING1
                + "&observedProperty=urn:blabla:wrong").openConnection();

        conn.getResponseCode(); // force connection and read
        checkServiceException(conn.getErrorStream(), "observedProperty");
    }
    
    
    @Test
    public void testGetResultWebSocketAllObservables() throws Exception
    {
        deployService(buildSensorProvider1(true, false));
        
        startSendingData(ID_SENSOR_MODULE1, 100);
        String[] records = sendGetResult(URI_OFFERING1, URI_PROP1, TIMERANGE_FUTURE, true);
        checkGetResultResponse(records, NUM_GEN_SAMPLES, 4);
    }
    
    
    @Test
    public void testGetResultWebSocketOneObservable() throws Exception
    {
        deployService(buildSensorProvider1(true, false));
                
        startSendingData(ID_SENSOR_MODULE1, 100);
        String[] records = sendGetResult(URI_OFFERING1, URI_PROP1_FIELD1, TIMERANGE_FUTURE, true);
        checkGetResultResponse(records, NUM_GEN_SAMPLES, 2);
    }
    
    
    @Test
    public void testGetResultWebSocketTwoObservables() throws Exception
    {
        deployService(buildSensorProvider1(true, false));
                
        startSendingData(ID_SENSOR_MODULE1, 100);
        String[] records = sendGetResult(URI_OFFERING1, URI_PROP1_FIELD1 + "," + URI_PROP1_FIELD2, TIMERANGE_FUTURE, true);
        checkGetResultResponse(records, NUM_GEN_SAMPLES, 3);
    }
    
    
    @Test
    public void testGetResultWebSocketBeforeDataIsAvailable() throws Exception
    {
        deployService(buildSensorProvider1(true, false));
        
        Future<String[]> future = sendGetResultAsync(URI_OFFERING1, URI_PROP1_FIELD1, TIMERANGE_FUTURE, true);
        
        // start sending data after a small pause
        startSendingData(ID_SENSOR_MODULE1, 500);

        try
        {
            String[] records = future.get(5, TimeUnit.SECONDS);
            checkGetResultResponse(records, NUM_GEN_SAMPLES, 2);
        }
        catch (Exception e)
        {
            fail("No data received before timeout");
        }
    }
    
    
    @Test
    public void testGetObsOneOfferingStartNow() throws Exception
    {
        deployService(buildSensorProvider1(true, false));
        
        var future = sendRequestAsync(generateGetObsStartNow(URI_OFFERING1, URI_PROP1), false);
        startSendingData(ID_SENSOR_MODULE1, 100);
        
        DOMHelper dom = future.get();
        assertEquals("Wrong number of observations returned", NUM_GEN_SAMPLES, dom.getElements("*/OM_Observation").getLength());
    }
    
    
    private FakeSensor getSensorModule(String moduleID)
    {
        return (FakeSensor)moduleRegistry.getLoadedModuleById(moduleID);
    }
    
    
    @Test
    public void testGetObsOneOfferingEndNow() throws Exception
    {
        deployService(buildSensorProvider1WithStorage());
        
        // wait until data has been produced and archived
        FakeSensor sensor = getSensorModule(ID_SENSOR_MODULE1);
        AsyncTests.waitForCondition(() -> !sensor.hasMoreData(), TIMEOUT);
        
        DOMHelper dom = sendRequest(generateGetObsEndNow(URI_OFFERING1, URI_PROP1), false);
        assertEquals("Wrong number of observations returned", NUM_GEN_SAMPLES, dom.getElements("*/OM_Observation").getLength());
    }
    
    
    @Test
    public void testGetObsOneOfferingWithTimeRange() throws Exception
    {
        deployService(buildSensorProvider1WithStorage());
        
        // wait until data has been produced and archived
        FakeSensor sensor = getSensorModule(ID_SENSOR_MODULE1);
        AsyncTests.waitForCondition(() -> !sensor.hasMoreData(), TIMEOUT);
                
        // first get capabilities to know available time range
        SOSServiceCapabilities caps = (SOSServiceCapabilities)new OWSUtils().getCapabilities(HTTP_ENDPOINT, "SOS", "2.0");
        TimeExtent timePeriod = caps.getLayer(URI_OFFERING1).getPhenomenonTime();
        System.out.println("Available time period is " + timePeriod);
        
        // then get obs
        Instant stopTime = Instant.now();
        DOMHelper dom = sendRequest(generateGetObsTimeRange(URI_OFFERING1, URI_PROP1, timePeriod.begin(), stopTime), false);
        assertEquals("Wrong number of observations returned", NUM_GEN_SAMPLES, dom.getElements("*/OM_Observation").getLength());
    }
    
    
    @Test
    public void testGetObsTwoOfferingsWithPost() throws Exception
    {
        deployService(buildSensorProvider1WithStorage(), buildSensorProvider2());
        
        // wait until data has been produced and archived
        FakeSensor sensor1 = getSensorModule(ID_SENSOR_MODULE1);
        AsyncTests.waitForCondition(() -> !sensor1.hasMoreData(), TIMEOUT);
                
        // then get obs
        DOMHelper dom = sendRequest(generateGetObs(URI_OFFERING1, URI_PROP1), true);        
        assertEquals("Wrong number of observations returned", NUM_GEN_SAMPLES, dom.getElements("*/OM_Observation").getLength());
    }
    
    
    @Test
    public void testGetObsTwoOfferingsByFoi() throws Exception
    {
        obsFoiMap.put(1, 1);
        obsFoiMap.put(3, 2);
        obsFoiMap.put(4, 3);
        
        deployService(buildSensorProvider1(), buildSensorProvider2WithObsStorage());
        
        // wait until data has been produced and archived
        FakeSensor sensor = getSensorModule(ID_SENSOR_MODULE2);
        AsyncTests.waitForCondition(() -> !sensor.hasMoreData(), TIMEOUT);
        DOMHelper dom;
        
        dom = sendRequest(generateGetObsByFoi(URI_OFFERING2, URI_PROP2, 1), true);        
        assertEquals("Wrong number of observations returned", 2, dom.getElements("*/OM_Observation").getLength());
        
        dom = sendRequest(generateGetObsByFoi(URI_OFFERING2, URI_PROP2, 2), true);        
        assertEquals("Wrong number of observations returned", 1, dom.getElements("*/OM_Observation").getLength());
        
        dom = sendRequest(generateGetObsByFoi(URI_OFFERING2, URI_PROP2, 3), true);        
        assertEquals("Wrong number of observations returned", 2, dom.getElements("*/OM_Observation").getLength());
        
        dom = sendRequest(generateGetObsByFoi(URI_OFFERING2, URI_PROP2, 1, 2), true);        
        assertEquals("Wrong number of observations returned", 3, dom.getElements("*/OM_Observation").getLength());
        
        dom = sendRequest(generateGetObsByFoi(URI_OFFERING2, URI_PROP2, 1, 2, 3), true);        
        assertEquals("Wrong number of observations returned", NUM_GEN_SAMPLES, dom.getElements("*/OM_Observation").getLength());
    }
    
    
    @Test
    public void testGetObsByBbox() throws Exception
    {
        obsFoiMap.put(1, 1);
        obsFoiMap.put(3, 2);
        obsFoiMap.put(4, 3);
        
        deployService(buildSensorProvider2WithObsStorage());
        
        // wait until data has been produced and archived
        FakeSensor sensor2 = getSensorModule(ID_SENSOR_MODULE2);
        AsyncTests.waitForCondition(() -> !sensor2.hasMoreData(), TIMEOUT);
        DOMHelper dom;
        
        dom = sendRequest(generateGetObsByBbox(URI_OFFERING2, URI_PROP2, new Bbox(0,0,0,0)), true);        
        assertEquals("Wrong number of observations returned", 0, dom.getElements("*/OM_Observation").getLength());
        
        dom = sendRequest(generateGetObsByBbox(URI_OFFERING2, URI_PROP2, new Bbox(0,0,1.5,1.5)), true);        
        assertEquals("Wrong number of observations returned", 2, dom.getElements("*/OM_Observation").getLength());
        
        dom = sendRequest(generateGetObsByBbox(URI_OFFERING2, URI_PROP2, new Bbox(1.5,1.5,2.0,2.0)), true);
        assertEquals("Wrong number of observations returned", 1, dom.getElements("*/OM_Observation").getLength());
    }
    
    
    @Test
    public void testGetObsWrongFormat() throws Exception
    {
        deployService(buildSensorProvider1());
        
        var conn = (HttpURLConnection)new URL(HTTP_ENDPOINT + "?service=SOS&version=2.0&request=GetObservation&offering=urn:mysos:sensor1&observedProperty=urn:blabla:temperature&responseFormat=badformat").openConnection();
        conn.getResponseCode(); // force connection and read
        
        checkServiceException(conn.getErrorStream(), "responseFormat");
    }
    
    
    protected DescribeSensorRequest generateDescribeSensor(String procId)
    {
        DescribeSensorRequest ds = new DescribeSensorRequest();
        ds.setGetServer(HTTP_ENDPOINT);
        ds.setVersion("2.0");
        ds.setProcedureID(procId);
        return ds;
    }
    
    
    protected GetObservationRequest generateGetObs(String offeringId, String obsProp)
    {
        GetObservationRequest getObs = new GetObservationRequest();
        getObs.setGetServer(HTTP_ENDPOINT);
        getObs.setVersion("2.0");
        getObs.setOffering(offeringId);
        getObs.getObservables().add(obsProp);
        return getObs;
    }
    
    
    protected GetObservationRequest generateGetObsStartNow(String offeringId, String obsProp)
    {
        GetObservationRequest getObs = generateGetObs(offeringId, obsProp);
        Instant futureTime = Instant.now().plus(1, ChronoUnit.HOURS);
        getObs.setTime(TimeExtent.beginNow(futureTime));
        return getObs;
    }
    
    
    protected GetObservationRequest generateGetObsEndNow(String offeringId, String obsProp)
    {
        GetObservationRequest getObs = generateGetObs(offeringId, obsProp);
        Instant pastTime = Instant.now().minus(1, ChronoUnit.HOURS);
        getObs.setTime(TimeExtent.endNow(pastTime));        
        return getObs;
    }
    
    
    protected GetObservationRequest generateGetObsTimeRange(String offeringId, String obsProp, Instant beginTime, Instant endTime)
    {
        GetObservationRequest getObs = generateGetObs(offeringId, obsProp);
        getObs.setTime(TimeExtent.period(beginTime, endTime));
        return getObs;
    }
    
    
    protected String getFoiUID(int foiNum)
    {
        var sensorNet = (FakeSensorNetOnlyFois)moduleRegistry.getLoadedModuleById(ID_SENSOR_MODULE2);
        return sensorNet.getFoiUID(foiNum);
    }
    
    
    protected GetObservationRequest generateGetObsByFoi(String offeringId, String obsProp, int... foiNums)
    {
        GetObservationRequest getObs = generateGetObs(offeringId, obsProp);
        for (int foiNum: foiNums)
            getObs.getFoiIDs().add(getFoiUID(foiNum));
        return getObs;
    }
    
    
    protected GetObservationRequest generateGetObsByBbox(String offeringId, String obsProp, Bbox bbox)
    {
        GetObservationRequest getObs = generateGetObs(offeringId, obsProp);
        getObs.setBbox(bbox);
        return getObs;
    }
    
    
    // TODO test getresult replay
    
    
    @Test
    public void testGetFoisByID() throws Exception
    {
        obsFoiMap.put(1, 1);
        obsFoiMap.put(3, 2);
        obsFoiMap.put(4, 3);
        
        var sensor1 = buildSensorProvider1();
        var sensor2 = buildSensorProvider2WithObsStorage();
        deployService(sensor1, sensor2);
        
        // wait until data has been produced and archived
        startSendingAndWaitForAllRecords(ID_SENSOR_MODULE2);      
        
        testGetFoisByID(1);
        testGetFoisByID(2);
        testGetFoisByID(3);
        testGetFoisByID(1, 2);
        testGetFoisByID(1, 3);
        testGetFoisByID(3, 2);
        testGetFoisByID(1, 2, 3);
        testGetFoisByID(2, 3, 1);
    }
    
    
    @Test
    public void testGetFoisByIDSoap() throws Exception
    {
        deployService(buildSensorProvider2WithObsStorage());
                
        GetFeatureOfInterestRequest req = new GetFeatureOfInterestRequest();
        req.setGetServer(HTTP_ENDPOINT);
        req.setVersion("2.0");
        req.setSoapVersion(OWSUtils.SOAP12_URI);
        req.getFoiIDs().add(getFoiUID(1));
        DOMHelper dom = sendSoapRequest(req);
        
        assertEquals(OWSUtils.SOAP12_URI, dom.getBaseElement().getNamespaceURI());
        assertEquals("Wrong number of features returned", 1, dom.getElements("*/*").getLength());
    }
    
    
    protected void testGetFoisByID(int... foiNums) throws Exception
    {
        GetFeatureOfInterestRequest req = new GetFeatureOfInterestRequest();
        req.setGetServer(HTTP_ENDPOINT);
        req.setVersion("2.0");
        for (int foiNum: foiNums)
            req.getFoiIDs().add(getFoiUID(foiNum));
        
        DOMHelper dom = sendRequest(req, false);
        assertEquals("Wrong number of features returned", foiNums.length, dom.getElements("*/*").getLength());
        
        Arrays.sort(foiNums);
        NodeList nodes = dom.getElements("*/*");
        for (int i=0; i<nodes.getLength(); i++)
        {
            var elt = (Element)nodes.item(i);
            String uid = dom.getElementValue(elt, "identifier");
            assertTrue(req.getFoiIDs().contains(uid));
        }
    }
    
    
    @Test
    public void testGetFoisByBbox() throws Exception
    {
        obsFoiMap.put(2, 1);
        obsFoiMap.put(4, 2);
        obsFoiMap.put(5, 3);
        
        var sensor1 = buildSensorProvider1();
        var sensor2 = buildSensorProvider2WithObsStorage();
        deployService(sensor1, sensor2);
        
        // wait until data has been produced and archived
        startSendingAndWaitForAllRecords(ID_SENSOR_MODULE2);
        
        testGetFoisByBbox(new Bbox(0.5, 0.5, 0.0, 1.5, 1.5, 0.0), 1);
        testGetFoisByBbox(new Bbox(1.5, 1.5, 0.0, 2.5, 2.5, 0.0), 2);
        testGetFoisByBbox(new Bbox(0.5, 0.5, 0.0, 2.5, 2.5, 0.0), 1, 2);
        testGetFoisByBbox(new Bbox(0.5, 0.5, 0.0, 3.5, 3.5, 0.0), 1, 2, 3);
    }
    
    
    protected void testGetFoisByBbox(Bbox bbox, int... expectedFoiNums) throws Exception
    {
        GetFeatureOfInterestRequest req = new GetFeatureOfInterestRequest();
        req.setGetServer(HTTP_ENDPOINT);
        req.setVersion("2.0");
        req.setBbox(bbox);
        
        DOMHelper dom = sendRequest(req, false);
        checkReturnedFois(dom, expectedFoiNums);
    }
    
    
    @Test
    public void testGetFoisByProcedure() throws Exception
    {
        obsFoiMap.put(2, 1);
        obsFoiMap.put(4, 2);
        obsFoiMap.put(5, 3);
        
        var sensor1 = buildSensorProvider1();
        var sensor2 = buildSensorProvider2WithObsStorage();
        deployService(sensor1, sensor2);
        
        // wait until data has been produced and archived
        startSendingAndWaitForAllRecords(ID_SENSOR_MODULE2);
        
        testGetFoisByProcedure(Arrays.asList(UID_SENSOR1), new int[0]);
        testGetFoisByProcedure(Arrays.asList(UID_SENSOR2), 1, 2, 3);
        testGetFoisByProcedure(Arrays.asList(UID_SENSOR1, UID_SENSOR2), 1, 2, 3);
    }
    
    
    protected void testGetFoisByProcedure(List<String> sysIDs, int... expectedFoiNums) throws Exception
    {
        GetFeatureOfInterestRequest req = new GetFeatureOfInterestRequest();
        req.setGetServer(HTTP_ENDPOINT);
        req.setVersion("2.0");
        req.getProcedures().addAll(sysIDs);
        
        DOMHelper dom = sendRequest(req, false);
        checkReturnedFois(dom, expectedFoiNums);
    }
    
    
    @Test
    public void testGetFoisByObservables() throws Exception
    {
        obsFoiMap.put(2, 1);
        obsFoiMap.put(4, 2);
        obsFoiMap.put(5, 3);
        
        var sensor1 = buildSensorProvider1();
        var sensor2 = buildSensorProvider2WithObsStorage();
        deployService(sensor1, sensor2);
        
        // wait until data has been produced and archived
        startSendingAndWaitForAllRecords(ID_SENSOR_MODULE2);
        
        testGetFoisByObservables(Arrays.asList("urn:blabla:image"), 1, 2, 3);
        testGetFoisByObservables(Arrays.asList("urn:blabla:RedChannel"), 1, 2, 3);
        testGetFoisByObservables(Arrays.asList("urn:blabla:GreenChannel"), 1, 2, 3);
        testGetFoisByObservables(Arrays.asList("urn:blabla:BlueChannel"), 1, 2, 3);
        testGetFoisByObservables(Arrays.asList("urn:blabla:weatherData"), 1, 2, 3);
    }
    
    
    protected void testGetFoisByObservables(List<String> obsIDs, int... expectedFoiNums) throws Exception
    {
        GetFeatureOfInterestRequest req = new GetFeatureOfInterestRequest();
        req.setGetServer(HTTP_ENDPOINT);
        req.setVersion("2.0");
        req.getObservables().addAll(obsIDs);
        
        DOMHelper dom = sendRequest(req, false); 
        checkReturnedFois(dom, expectedFoiNums);
    }
    
    
    protected void checkReturnedFois(DOMHelper dom, int... expectedFoiNums)
    {
        assertEquals("Wrong number of features returned", expectedFoiNums.length, dom.getElements("*/*").getLength());
        
        NodeList nodes = dom.getElements("*/*");
        for (int i=0; i<nodes.getLength(); i++)
        {
            var uid = dom.getElementValue((Element)nodes.item(i), "identifier");
            var expectedUid = getFoiUID(expectedFoiNums[i]);
            assertEquals(expectedUid, uid);
        }
    }
    
   
    @After
    public void cleanup()
    {
        try
        {
            if (hub != null)
                hub.stop();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (dbFile1 != null)
                dbFile1.delete();
            if (dbFile2 != null)
                dbFile2.delete();
        }
    }
}
