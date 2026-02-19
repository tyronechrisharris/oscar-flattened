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
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.concurrent.Future;
import net.opengis.sensorml.v20.PhysicalSystem;
import net.opengis.swe.v20.DataRecord;
import net.opengis.swe.v20.DataStream;
import net.opengis.swe.v20.Quantity;
import net.opengis.swe.v20.Time;
import net.opengis.swe.v20.Vector;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vast.data.DataRecordImpl;
import org.vast.data.DataStreamImpl;
import org.vast.data.QuantityImpl;
import org.vast.data.TextEncodingImpl;
import org.vast.data.TimeImpl;
import org.vast.data.VectorImpl;
import org.vast.ogc.om.IObservation;
import org.vast.ogc.om.ObservationImpl;
import org.vast.ows.GetCapabilitiesRequest;
import org.vast.ows.OWSException;
import org.vast.ows.OWSResponse;
import org.vast.ows.OWSUtils;
import org.vast.ows.sos.InsertResultRequest;
import org.vast.ows.sos.InsertResultTemplateRequest;
import org.vast.ows.sos.InsertResultTemplateResponse;
import org.vast.ows.sos.InsertSensorRequest;
import org.vast.ows.swe.InsertSensorResponse;
import org.vast.ows.swe.SWESUtils;
import org.vast.ows.sos.SOSOfferingCapabilities;
import org.vast.ows.sos.SOSServiceCapabilities;
import org.vast.ows.sos.SOSUtils;
import org.vast.sensorML.PhysicalSystemImpl;
import org.vast.swe.SWEConstants;


public class TestSOSTService
{
    private static String SENSOR_UID = "urn:test:newsensor:0001";  
    private static final String URI_OUTPUT1 = "urn:blabla:temp";
    private static final String URI_OUTPUT2 = "urn:blabla:gps";
    private static final int NUM_GEN_SAMPLES = 5;
    TestSOSService sosTest;
    Throwable error = null;
    
    
    @Before
    public void setup() throws Exception
    {
        sosTest = new TestSOSService();
        sosTest.setup();
    }
    
    
    protected SOSService deployService(SOSProviderConfig... providerConfigs) throws Exception
    {   
        return sosTest.deployService(true, providerConfigs);
    }
    
    
    protected SystemDataProviderConfig buildSensorProvider1() throws Exception
    {
        return sosTest.buildSensorProvider1();
    }
    
    
    protected InsertSensorRequest buildInsertSensor() throws Exception
    {
        // create procedure
        PhysicalSystem procedure = new PhysicalSystemImpl();
        procedure.setName("My weather station");
        procedure.setUniqueIdentifier(SENSOR_UID);
        
        // output 1
        DataStream tempOutput = new DataStreamImpl();
        procedure.addOutput("tempOut", tempOutput);
        tempOutput.setEncoding(new TextEncodingImpl());
        
        DataRecord tempRec = new DataRecordImpl(2);
        tempRec.setDefinition(URI_OUTPUT1);
        tempOutput.setElementType("tempOut", tempRec);
        Time timeTag = new TimeImpl();
        timeTag.setDefinition(SWEConstants.DEF_SAMPLING_TIME);
        timeTag.getUom().setHref(Time.ISO_TIME_UNIT);
        tempRec.addComponent("time", timeTag);
        Quantity tempVal = new QuantityImpl();
        tempVal.setDefinition("http://mmisw.org/ont/cf/parameter/air_temperature");
        tempVal.getUom().setCode("Cel");
        tempRec.addComponent("temp", tempVal);
        
        // output 2
        DataStream posOutput = new DataStreamImpl();
        procedure.addOutput("posOut", posOutput);
        posOutput.setEncoding(new TextEncodingImpl());
        
        DataRecord posRec = new DataRecordImpl(2);
        posRec.setDefinition(URI_OUTPUT2);
        posOutput.setElementType("posOut", posRec);
        posRec.addComponent("time", timeTag.copy());
        Vector posVector = new VectorImpl(3);
        posVector.setDefinition(SWEConstants.DEF_SAMPLING_LOC);
        posVector.setReferenceFrame("http://www.opengis.net/def/crs/EPSG/0/4979");
        posVector.addComponent("lat", new QuantityImpl());
        posVector.addComponent("lon", new QuantityImpl());
        posVector.addComponent("alt", new QuantityImpl());
        posRec.addComponent("pos", posVector);
        
        // build insert sensor request
        InsertSensorRequest req = new InsertSensorRequest();
        req.setPostServer(TestSOSService.HTTP_ENDPOINT);
        req.setVersion("2.0");        
        req.setProcedureDescription(procedure);
        req.setProcedureDescriptionFormat(SWESUtils.DEFAULT_PROCEDURE_FORMAT);
        req.getObservationTypes().add(IObservation.OBS_TYPE_GENERIC);
        req.getObservationTypes().add(IObservation.OBS_TYPE_RECORD);
        req.getObservableProperties().add(SWEConstants.DEF_SAMPLING_LOC);
        req.getObservableProperties().add("http://mmisw.org/ont/cf/parameter/air_temperature");
        req.getFoiTypes().add("urn:blabla:myfoi1");
        req.getFoiTypes().add("urn:blabla:myfoi2");
        
        return req;
    }
    
    
    protected InsertResultTemplateRequest buildInsertResultTemplate(DataStream output, InsertSensorResponse resp) throws Exception
    {
        InsertResultTemplateRequest req = new InsertResultTemplateRequest();
        req.setPostServer(TestSOSService.HTTP_ENDPOINT);
        req.setVersion("2.0");
        req.setOffering(resp.getAssignedOffering());
        req.setResultStructure(output.getElementType());
        req.setResultEncoding(output.getEncoding());
        req.setObservationTemplate(new ObservationImpl());
        return req;
    }
    
    
    protected InsertResultRequest buildInsertResult(InsertResultTemplateResponse resp) throws Exception
    {
        InsertResultRequest req = new InsertResultRequest();
        req.setGetServer(TestSOSService.HTTP_ENDPOINT);
        req.setVersion("2.0");
        req.setTemplateId(resp.getAcceptedTemplateId());
        return req;
    }
    
    
    protected SOSOfferingCapabilities getCapabilities(int offeringIndex) throws Exception
    {
        OWSUtils utils = new OWSUtils();
        
        // check capabilities has one more offering
        GetCapabilitiesRequest getCap = new GetCapabilitiesRequest();
        getCap.setService(SOSUtils.SOS);
        getCap.setVersion("2.0");
        getCap.setGetServer(TestSOSService.HTTP_ENDPOINT);
        SOSServiceCapabilities caps = (SOSServiceCapabilities)utils.sendRequest(getCap, false);
        //utils.writeXMLResponse(System.out, caps);
        assertEquals("No offering added", offeringIndex+1, caps.getLayers().size());
        
        return (SOSOfferingCapabilities)caps.getLayers().get(offeringIndex);
    }    
    
    
    @Test
    public void testSetupService() throws Exception
    {
        deployService(buildSensorProvider1());
    }
    
    
    @Test
    public void testInsertSensor() throws Exception
    {
        deployService(buildSensorProvider1());
        OWSUtils utils = new OWSUtils();
        InsertSensorRequest req = buildInsertSensor();
        
        try
        {
            utils.writeXMLQuery(System.out, req);
            OWSResponse resp = utils.sendRequest(req, false);
            utils.writeXMLResponse(System.out, resp);
        }
        catch (OWSException e)
        {
            utils.writeXMLException(System.out, "SOS", "2.0", e);
            throw e;
        }
        
        // check new offering has correct properties
        SOSOfferingCapabilities newOffering = getCapabilities(1);
        String sysUID = req.getProcedureDescription().getUniqueIdentifier();
        assertEquals(sysUID, newOffering.getProcedures().iterator().next());
    }
    
    
    @Test
    public void testInsertSensorWithAutoStorage() throws Exception
    {
        deployService(buildSensorProvider1());
        
        OWSUtils utils = new OWSUtils();
        InsertSensorRequest req = buildInsertSensor();
        
        try
        {
            utils.writeXMLQuery(System.out, req);
            OWSResponse resp = utils.sendRequest(req, false);
            utils.writeXMLResponse(System.out, resp);
        }
        catch (OWSException e)
        {
            utils.writeXMLException(System.out, "SOS", "2.0", e);
            throw e;
        }
        
        // wait for capabilities update
        Thread.sleep(TestSOSService.CAPS_REFRESH_PERIOD);
        
        // check new offering has correct properties
        SOSOfferingCapabilities newOffering = getCapabilities(1);
        String sysUID = req.getProcedureDescription().getUniqueIdentifier();
        assertEquals(sysUID, newOffering.getProcedures().iterator().next());
    }

    
    /*@Test
    public void testInsertObservation() throws Exception
    {
        deployService(buildSensorProvider1());
        OWSUtils utils = new OWSUtils();
        
        // first register sensor
        OWSResponse resp = utils.sendRequest(buildInsertSensor(), false);
        utils.writeXMLResponse(System.out, resp);
        
        // connect to SOS to listen for new obs
        Thread t = new Thread() {
            public void run()
            {
                try
                {
                    InputStream is = new URL(TestSOSService.HTTP_ENDPOINT + "?service=SOS&version=2.0&request=GetResult&offering=" + SENSOR_UID + "-sos&observedProperty=urn:blabla:temperature").openStream();
                    IOUtils.copy(is, System.out);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        };
        t.start();
        
        // create new observation
        var obs = new ObservationImpl();
        obs.setPhenomenonTime(TimeExtent.currentTime());
        obs.setResultTime(obs.getPhenomenonTime().begin());
        obs.setProcedure(new ProcedureRef(SENSOR_UID));
        
        // build and send insert observation
        InsertObservationRequest insObs = new InsertObservationRequest();
        insObs.setPostServer(TestSOSService.HTTP_ENDPOINT);
        insObs.setVersion("2.0");
        insObs.setOffering(SENSOR_UID + "-sos");
        insObs.getObservations().add(obs);
        
        utils.writeXMLQuery(System.out, insObs);
        resp = utils.sendRequest(insObs, false);
        utils.writeXMLResponse(System.out, resp);
    }*/
    
    
    @Test
    public void testInsertResultTemplate() throws Exception
    {
        deployService(buildSensorProvider1());
        OWSUtils utils = new OWSUtils();
        
        // first register sensor
        InsertSensorRequest req = buildInsertSensor();
        InsertSensorResponse resp = (InsertSensorResponse)utils.sendRequest(req, false);
        
        // send insert template
        DataStream output = (DataStream)req.getProcedureDescription().getOutputList().get(0);
        utils.sendRequest(buildInsertResultTemplate(output, resp), false);
        output = (DataStream)req.getProcedureDescription().getOutputList().get(1);
        utils.sendRequest(buildInsertResultTemplate(output, resp), false);
        
        // check new offering has correct properties
        SOSOfferingCapabilities newOffering = getCapabilities(1);
        String sysUID = req.getProcedureDescription().getUniqueIdentifier();
        assertEquals(sysUID, newOffering.getMainProcedure());
        assertTrue("Observation types missing", newOffering.getObservationTypes().containsAll(req.getObservationTypes()));
        assertTrue("Observed properties missing", newOffering.getObservableProperties().containsAll(req.getObservableProperties()));
        assertTrue("Procedure format missing", newOffering.getProcedureFormats().contains(req.getProcedureDescriptionFormat()));
    }

    
    @Test
    public void testInsertResultWebsocket() throws Exception
    {
        SOSService sos = deployService(new SOSProviderConfig[0]);
        sos.getConfiguration().defaultLiveTimeout = 2.0;
        OWSUtils utils = new OWSUtils();
        
        // first register sensor
        InsertSensorRequest insertSensorReq = buildInsertSensor();
        InsertSensorResponse insertSensorResp = (InsertSensorResponse)utils.sendRequest(insertSensorReq, false);
        
        // send insert template
        DataStream output = (DataStream)insertSensorReq.getProcedureDescription().getOutputList().get(1);
        InsertResultTemplateResponse insertTemplateResp = (InsertResultTemplateResponse)utils.sendRequest(buildInsertResultTemplate(output, insertSensorResp), false);
        
        // build insert result request
        InsertResultRequest insertResultReq = buildInsertResult(insertTemplateResp);
        String url = utils.buildURLQuery(insertResultReq);
        url = url.replace("http://", "ws://");
        
        // init websocket client
        final Object lock = new Object();
        WebSocketClient wsClient = new WebSocketClient();
        wsClient.start();
        URI wsUri = new URI(url);
        WebSocketAdapter socket = new WebSocketAdapter() {
            public void onWebSocketClose(int statusCode, String reason)
            {
                super.onWebSocketClose(statusCode, reason);
                synchronized (lock) { lock.notifyAll(); }
            }

            public void onWebSocketError(Throwable e)
            {
                error = e;
            }            
        };
        Future<Session> future = wsClient.connect(socket, wsUri);
        Session session = future.get();
        assertTrue("Websocket client could not connect", socket.isConnected());
        
        // initiate GetResult request to check retrieved data
        Future<String[]> f = sosTest.sendGetResultAsync(SENSOR_UID, 
                URI_OUTPUT2, TestSOSService.TIMERANGE_FUTURE, false);
        
        // send data using websocket
        for (int i=0; i<NUM_GEN_SAMPLES; i++)
        {
            Thread.sleep(300);
            String isoTime = Instant.now().toString();
            var data = new String(isoTime + "," + (40.0+i/10.) + "," + (-90.0-i/10.) + ",0.0\n");
            ByteBuffer buf = ByteBuffer.wrap(data.getBytes());
            System.out.println("Sending data: " + data);
            session.getRemote().sendBytes(buf);            
        }
        
        // check no errors occured during transfer
        if (error != null)
        {
            error.printStackTrace();
            fail("Error while sending records using websocket");
        }
        
        // check that we received all records
        sosTest.checkGetResultResponse(f.get(), NUM_GEN_SAMPLES, 4);
        
        // request close and check proper disconnection
        session.close();
        synchronized (lock)
        {
            if (socket.isConnected())
                lock.wait(1000);
        }
        
        assertTrue("Websocket client was not properly disconnected", socket.isNotConnected());
    }
    
    
    @After
    public void cleanup()
    {
        sosTest.cleanup();
    }
}
