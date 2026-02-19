/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sos;

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sensorhub.api.data.ObsData;
import org.sensorhub.api.data.ObsEvent;
import org.sensorhub.api.module.ModuleEvent.ModuleState;
import org.sensorhub.impl.SensorHub;
import org.sensorhub.impl.service.HttpServerConfig;
import org.sensorhub.impl.service.swe.RecordTemplate;
import org.vast.data.JSONEncodingImpl;
import org.vast.data.TextEncodingImpl;
import org.vast.data.XMLEncodingImpl;
import org.vast.ows.sos.GetResultRequest;
import org.vast.swe.SWEHelper;
import org.vast.swe.helper.GeoPosHelper;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataEncoding;


public class TestResultSerializers
{
    static final long TIMEOUT = 2000;
    static final int SERVER_PORT = 8888;
    static final int NUM_GENERATED_RECORDS = 10;
    
    static SensorHub hub;
    static SOSServlet servlet;
    
    
    @BeforeClass
    public static void init() throws Exception
    {
        // get instance with in-memory DB
        hub = new SensorHub();
        hub.start();
        var moduleRegistry = hub.getModuleRegistry();
        
        // start HTTP server
        var httpConfig = new HttpServerConfig();
        httpConfig.httpPort = SERVER_PORT;
        moduleRegistry.loadModule(httpConfig, TIMEOUT);
        
        // load and start SOSService
        var sosCfg = new SOSServiceConfig();
        var sos = (SOSService)moduleRegistry.loadModule(sosCfg, TIMEOUT);
        sos.init();
        sos.start();
        sos.waitForState(ModuleState.STARTED, TIMEOUT);
        servlet = sos.getServlet();
    }
    
    
    protected void testSerializer(AbstractResultSerializerSwe serializer, DataEncoding encoding) throws Exception
    {
        var swe = new GeoPosHelper();
        var recordStruct = swe.createRecord()
            .name("weather_data")
            .addSamplingTimeIsoUTC("time")
            .addField("loc", swe.newLocationVectorLLA("uri"))
            .build();
        
        // init test async context and request
        TestAsyncContext asyncCtx = new TestAsyncContext();
        var req = new GetResultRequest();
        req.setVersion("2.0");
        req.setHttpResponse((HttpServletResponse)asyncCtx.getResponse());
        
        // init async serializer
        serializer.init(
            servlet, asyncCtx, req,
            new RecordTemplate(recordStruct, encoding));
        var sub = new TestSubscription<ObsEvent>(serializer);
        serializer.onSubscribe(sub);
        
        // also init a simple writer to compare results
        var syncWriterOs = new ByteArrayOutputStream();
        var syncWriter = SWEHelper.createDataWriter(encoding);
        syncWriter.setDataComponents(recordStruct);
        syncWriter.setOutput(syncWriterOs);
        syncWriter.startStream(true);
        
        int numRecords = 10;
        AtomicInteger counter = new AtomicInteger();
        Instant t0 = Instant.parse("2020-03-01T10:00:00Z");
        
        var timer = Executors.newScheduledThreadPool(1);
        timer.scheduleAtFixedRate(() -> {
            boolean done = false;
            
            try
            {
                var time = t0.plusSeconds(counter.get()*10).getEpochSecond();
        
                DataBlock data = recordStruct.createDataBlock();
                data.setDoubleValue(0, time);
                data.setDoubleValue(1, -20+counter.get()*5);
                data.setDoubleValue(2, counter.get()*10);
                done = counter.incrementAndGet() >= numRecords;
                
                // push data to subscription feeding the async serializer
                var obs = new ObsData.Builder()
                    .withPhenomenonTime(Instant.ofEpochSecond(time))
                    .withResult(data)
                    .build();
                sub.push(new ObsEvent(System.currentTimeMillis(), "myproc-001", "myoutput", obs), done);
                
                // also write synchronously so we can compare outputs later
                syncWriter.write(data);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                done = true;
            }
            
            if (done)
            {
                timer.shutdown();
                throw new CancellationException();
            }
            
        }, 0, 103, TimeUnit.MILLISECONDS);
        
        // wait for timer to terminate
        timer.awaitTermination(10, TimeUnit.SECONDS);
        
        // wait for serializer to be done
        while (!serializer.done || asyncCtx.counter.get() > 0)
            Thread.sleep(100);
        
        assertEquals(numRecords, counter.get());
        
        syncWriter.endStream();
        syncWriter.flush();
        
        //System.out.println();        
        //System.out.println("Async Output:\n" + asyncCtx.os.toString());
        System.out.println();
        System.out.println("Sync Output:\n" + syncWriterOs.toString());
        System.out.println();
        
        // compare outputs
        assertEquals(syncWriterOs.toString(), asyncCtx.os.toString());
    }
    
    
    @Test
    public void testJsonSerializer() throws Exception
    {
        testSerializer(new ResultSerializerJson(), new JSONEncodingImpl());
    }
    
    
    @Test
    public void testCsvSerializer() throws Exception
    {
        testSerializer(new ResultSerializerText(), new TextEncodingImpl());
    }
    
    
    @Test
    public void testXmlSerializer() throws Exception
    {
        testSerializer(new ResultSerializerXml(), new XMLEncodingImpl());
    }
    
   
    @AfterClass
    public static void cleanup()
    {
        if (hub != null)
        {
            hub.stop();
            hub = null;
            servlet = null;
        }
    }

}
