/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.sensorhub.api.common.SensorHubException;


public class TestApiObsWs extends AbstractTestApiBase
{
    TestSystems systemTests = new TestSystems();
    TestDataStreams datastreamTests = new TestDataStreams();
    TestObservations obsTests = new TestObservations();
    
    
    @Before
    public void setup() throws IOException, SensorHubException
    {
        super.setup();
        systemTests.apiRootUrl = apiRootUrl;
        datastreamTests.apiRootUrl = apiRootUrl;
        obsTests.apiRootUrl = apiRootUrl;
    }
    
    
    /*--------------*/
    /* Observations */
    /*--------------*/
    
    protected String preloadObs(int numObs, Instant startTime, long timeStepMillis) throws Exception
    {
     // add system
        var procUrl = systemTests.addFeature(33);
        
        // add datastream
        var dsUrl = datastreamTests.addDatastreamOmJson(procUrl, 115);
        
        // add observations
        var ids = new ArrayList<String>();
        for (int i = 0; i < numObs; i++)
        {
            var url = obsTests.addObservation(dsUrl, startTime, timeStepMillis, i);
            var id = url.substring(url.lastIndexOf('/')+1);
            ids.add(id);
        }
        
        return dsUrl;
    }
    
    
    @Test
    public void testStreamHistoricalObsPhenomenonTimeRange() throws Exception
    {
        int numObs = 10;
        var startTime = Instant.now();
        long timeStep = 200L;
        var dsUrl = preloadObs(numObs, startTime, timeStep);
        
        int numStreamedObs = 3;
        startTime = startTime.plusMillis(100);
        var endTime = startTime.plusMillis(numStreamedObs*timeStep);
        var path = concat(dsUrl, AbstractTestApiBase.OBS_COLLECTION + "?phenomenonTime=" + startTime + "/" + endTime);
        sendWsRequest(path, numStreamedObs);
    }
    
    
    @Test
    public void testStreamHistoricalObsResultTimeRange() throws Exception
    {
        int numObs = 10;
        var startTime = Instant.now();
        long timeStep = 363L;
        var dsUrl = preloadObs(numObs, startTime, timeStep);
        
        int numStreamedObs = 4;
        var endTime = startTime.plusMillis(numStreamedObs*timeStep);
        var path = concat(dsUrl, AbstractTestApiBase.OBS_COLLECTION + "?resultTime=" + startTime + "/" + endTime);
        sendWsRequest(path, numStreamedObs);
    }
    
    
    @Test
    public void testStreamHistoricalObsUnboundedTimeRange() throws Exception
    {
        int numObs = 10;
        var startTime = Instant.now();
        long timeStep = 100L;
        var dsUrl = preloadObs(numObs, startTime, timeStep);
        
        var path = concat(dsUrl, AbstractTestApiBase.OBS_COLLECTION + "?phenomenonTime=../..");
        sendWsRequest(path, numObs);
    }
    
    
    protected void sendWsRequest(String path, int numExpectedObs) throws Exception
    {
        var obsCounter = new CountDownLatch(numExpectedObs);
        
        HttpClient client = HttpClient.newHttpClient();
        client.newWebSocketBuilder()
            .buildAsync(URI.create(concat(getWsEndpoint(), path)), new WebSocket.Listener() {
                @Override
                public void onOpen(WebSocket webSocket)
                {
                    System.out.println("Websocket connection open");
                }

                @Override
                public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason)
                {
                    System.out.println("Websocket connection closed: status=" + statusCode + ", reason=" + reason);
                    return CompletableFuture.completedStage(null);
                }

                @Override
                public void onError(WebSocket webSocket, Throwable error)
                {
                    System.err.println("Websocket error: " + error);
                }

                @Override
                public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last)
                {
                    var msg = StandardCharsets.UTF_8.decode(data);
                    System.out.println("Message received: " + msg);
                    obsCounter.countDown();
                    return CompletableFuture.completedStage(null);
                }
            })
            .thenAccept(ws -> {
                ws.request(Long.MAX_VALUE);
            });
        
        obsCounter.await(10, TimeUnit.SECONDS);
        assertEquals("Some obs are left", 0L, obsCounter.getCount());
    }
    
    
    protected String getWsEndpoint()
    {
        return apiRootUrl.replace("http", "ws");
    }

}
