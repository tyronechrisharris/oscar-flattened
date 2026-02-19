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
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamWriter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sensorhub.impl.SensorHub;
import org.sensorhub.impl.service.HttpServerConfig;
import org.vast.ogc.om.IProcedure;
import org.vast.ows.swe.DescribeSensorRequest;
import org.vast.sensorML.SMLHelper;
import org.vast.sensorML.SMLStaxBindings;
import org.vast.sensorML.json.SMLJsonStreamWriter;


public class TestProcedureSerializers
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
        moduleRegistry.startModule(sosCfg.id);
        servlet = sos.getServlet();
        
        TestAsyncContext.WRITE_BUFFER_CONSUME_DELAY_MS = 100;
        TestAsyncContext.WRITE_BUFFER_READY_LIMIT = 1024;
    }
    
    
    protected void testSerializer(AbstractAsyncSerializerStax<DescribeSensorRequest,IProcedure> serializer) throws Exception
    {
        // init test async context and request
        TestAsyncContext asyncCtx = new TestAsyncContext();
        var req = new DescribeSensorRequest();
        req.setHttpResponse((HttpServletResponse)asyncCtx.getResponse());
        
        // init async serializer
        serializer.init(servlet, asyncCtx, req);
        var sub = new TestSubscription<IProcedure>(serializer);
        serializer.onSubscribe(sub);
        
        // also init a simple writer to compare results
        var syncWriterOs = new ByteArrayOutputStream();
        var smlBindings = new SMLStaxBindings();
        XMLStreamWriter syncWriter;
        if (serializer instanceof ProcedureSerializerJson)
        {
            syncWriter = new SMLJsonStreamWriter(syncWriterOs, StandardCharsets.UTF_8);
            ((SMLJsonStreamWriter)syncWriter).beginArray();
        }
        else
        {
            syncWriter = null;
            /*XMLOutputFactory factory = XMLImplFinder.getStaxOutputFactory();
            XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(syncWriterOs, StandardCharsets.UTF_8.name());
            syncWriter = new IndentingXMLStreamWriter(xmlWriter);
            smlBindings.setNamespacePrefixes(syncWriter);
            smlBindings.declareNamespacesOnRootElement();*/
        }
                        
        int numRecords = 10;
        AtomicInteger counter = new AtomicInteger();
        //Instant t0 = Instant.parse("2020-03-01T10:00:00Z");
        
        var timer = Executors.newScheduledThreadPool(1);
        timer.scheduleAtFixedRate(() -> {
            boolean done = false;
            
            try
            {
                var sml = new SMLHelper();
                var proc = sml.createPhysicalComponent()
                    .uniqueID(String.format("urn:uid:016548948:%03d", counter.get()+1))
                    .addIdentifier(sml.identifiers.longName("My Sensor of type XXX and model 001"))
                    .addIdentifier(sml.identifiers.shortName("Sensor 001"))
                    .addIdentifier(sml.identifiers.serialNumber("045GGG101"))
                    .build();
                
                done = counter.incrementAndGet() >= numRecords;
                sub.push(proc, done);
                
                // also write synchronously so we can compare outputs later
                if (syncWriter instanceof SMLJsonStreamWriter)
                {
                    smlBindings.writeAbstractProcess(syncWriter, proc);
                    ((SMLJsonStreamWriter)syncWriter).resetContext();
                }
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
        }, 0, 250, TimeUnit.MILLISECONDS);
        
        // wait for timer to terminate
        timer.awaitTermination(1000, TimeUnit.SECONDS);
        
        // wait for serializer to be done
        while (!serializer.done || asyncCtx.counter.get() > 0)
            Thread.sleep(100);
        
        assertEquals(numRecords, counter.get());
        
        if (syncWriter instanceof SMLJsonStreamWriter)
        {
            ((SMLJsonStreamWriter)syncWriter).endArray();
            syncWriter.flush();
        }
        
        //System.out.println();        
        //System.out.println("Async Output:\n" + asyncCtx.os.toString());
        System.out.println();
        System.out.println("Sync Output:\n" + syncWriterOs.toString());
        System.out.println();
        
        // compare outputs
        if (syncWriter != null)
            assertEquals(syncWriterOs.toString(), asyncCtx.os.toString());
    }
    
    
    @Test
    public void testJsonSerializer() throws Exception
    {
        testSerializer(new ProcedureSerializerJson());
    }
    
    
    @Test
    public void testXmlSerializer() throws Exception
    {
        testSerializer(new ProcedureSerializerXml());
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
