/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.sensorhub.api.common.SensorHubException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;


public class TestObservations extends AbstractTestApiBase
{
    TestSystems systemTests = new TestSystems();
    TestDataStreams datastreamTests = new TestDataStreams();
    
    
    @Before
    public void setup() throws IOException, SensorHubException
    {
        super.setup();
        systemTests.apiRootUrl = apiRootUrl;
        datastreamTests.apiRootUrl = apiRootUrl;
    }
    
    
    @Test
    public void testAddDatastreamAndObservations() throws Exception
    {
        // add system
        var sysUrl = systemTests.addFeature(33);
        
        // add datastream
        var dsUrl = datastreamTests.addDatastreamOmJson(sysUrl, 115);
        
        // add observations
        var now = Instant.now();
        int numObs = 10;
        var ids = new ArrayList<String>();
        for (int i = 0; i < numObs; i++)
        {
            var url = addObservation(dsUrl, now, i);
            ids.add(getResourceId(url));
        }
        
        // get list of obs
        var jsonResp = sendGetRequestAndParseJson(concat(dsUrl, OBS_COLLECTION));
        checkCollectionItemIds(ids, jsonResp);
    }
    
    
    @Test
    public void testAddDatastreamAndObsBatch() throws Exception
    {
        // add system
        var sysUrl = systemTests.addFeature(6);
        
        // add datastream
        var dsUrl = datastreamTests.addDatastreamOmJson(sysUrl, 115);
        
        // add observations in batch
        var urls = addObservationBatch(dsUrl, Instant.now(), 100, 50, true);
        
        // get list of obs
        var jsonResp = sendGetRequestAndParseJson(concat(dsUrl, OBS_COLLECTION));
        checkCollectionItemIds(urls, jsonResp);
    }
    
    
    @Test
    public void testAddAndDeleteObs() throws Exception
    {
        // add system
        var sysUrl = systemTests.addFeature(6);
        
        // add datastream
        var dsUrl = datastreamTests.addDatastreamOmJson(sysUrl, 115);
        
        // add observations in batch
        var urls = addObservationBatch(dsUrl, Instant.now(), 100, 10, true);
        
        // delete one obs
        sendGetRequestAndCheckStatus(urls.get(0), 200);
        sendDeleteRequestAndCheckStatus(urls.get(0), 204);
        sendGetRequestAndCheckStatus(urls.get(0), 404);
        sendDeleteRequestAndCheckStatus(urls.get(0), 404);
        urls.remove(0);
        sendGetRequestAndGetItems(OBS_COLLECTION, urls.size());
        
        // delete all remaining obs
        for (var url: urls)
            sendDeleteRequestAndCheckStatus(url, 204);
        sendGetRequestAndGetItems(OBS_COLLECTION, 0);
    }
    
    
    @Test
    public void testAddObsAndDeleteDatastream() throws Exception
    {
        // add system
        var sysUrl = systemTests.addFeature(6);
        
        // add datastream
        var dsUrl1 = datastreamTests.addDatastreamOmJson(sysUrl, 2);
        var dsUrl2 = datastreamTests.addDatastreamOmJson(sysUrl, 3);
        
        // add observations in batch
        var urlsDs1 = addObservationBatch(dsUrl1, Instant.now(), 100, 10, true);
        var urlsDs2 = addObservationBatch(dsUrl2, Instant.now(), 1000, 5, true);
        sendGetRequestAndGetItems(OBS_COLLECTION, urlsDs1.size() + urlsDs2.size());
        
        // delete entire datastream 1
        sendDeleteRequestAndCheckStatus(dsUrl1, 204);
        sendGetRequestAndCheckStatus(dsUrl1, 404);
        sendGetRequestAndGetItems(OBS_COLLECTION, urlsDs2.size());
        
        // delete entire datastream 2
        sendDeleteRequestAndCheckStatus(dsUrl2, 204);
        sendGetRequestAndCheckStatus(dsUrl2, 404);
        sendGetRequestAndGetItems(OBS_COLLECTION, 0);
    }
    
    
    // Non-Test helper methods
    
    protected String addObservation(String dsUrl, Instant startTime, int num) throws Exception
    {
        return addObservation(dsUrl, startTime, 1000L, num);
    }
    
    
    protected String addObservation(String dsUrl, Instant startTime, long timeStepMillis, int num) throws Exception
    {
        // add datastream
        var json = createObservationNoFoi(startTime, timeStepMillis, num);
        var httpResp = sendPostRequest(concat(dsUrl, OBS_COLLECTION), json);
        var url = getLocation(httpResp);
        
        // get it back by id
        var jsonResp = sendGetRequestAndParseJson(url);
        checkId(url, jsonResp);
        assertObsEquals(json, (JsonObject)jsonResp);
        
        // return datastream ID
        return url;
    }
    
    
    protected List<String> addObservationBatch(String dsUrl, Instant startTime, long timeStepMillis, int numObs, boolean checkGet) throws Exception
    {
        var array = new JsonArray();
        for (int i = 0; i < numObs; i++)
        {
            var obj = createObservationNoFoi(startTime, timeStepMillis, i);
            array.add(obj);
        }
        
        var urlList = sendPostRequestAndParseUrlList(concat(dsUrl, OBS_COLLECTION), array);
        assertEquals("Wrong number of resources created", array.size(), urlList.size());
        
        if (checkGet)
        {
            for (int i = 0; i < array.size(); i++)
            {
                var url = urlList.get(i);
                var json = array.get(i);
                var jsonResp = sendGetRequestAndParseJson(url);
                checkId(url, jsonResp);
                assertObsEquals((JsonObject)json, (JsonObject)jsonResp);
            }
        }
        
        return urlList;
    }
    
    
    protected JsonObject createObservationNoFoi(Instant startTime, long timeStepMillis, int num) throws Exception
    {
        var buffer = new StringWriter();
        
        try (var writer = new JsonWriter(buffer))
        {
            writer.beginObject();
            
            var timeStamp = startTime.plusMillis(num*timeStepMillis).toString();
            writer.name("phenomenonTime").value(timeStamp.toString());
            writer.name("resultTime").value(timeStamp.toString());
            writer.name("result")
                .beginObject()
                .name("f1").value(num)
                .name("f2").value(true)
                .name("f3").value(String.format("text_%03d", num))
                .endObject();
            
            writer.endObject();
        }
        catch (Exception e)
        {
            throw new IOException("Error writing JSON observation", e);
        }
        
        return (JsonObject)JsonParser.parseString(buffer.getBuffer().toString());
    }
    
    
    protected void assertObsEquals(JsonObject expected, JsonObject actual)
    {
        // remove some fields not present in POST request before comparison
        actual.remove("id");
        actual.remove("datastream@id");
        assertEquals(expected, actual);
    }
}
