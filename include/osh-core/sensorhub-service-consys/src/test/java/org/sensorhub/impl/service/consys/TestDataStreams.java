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
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Set;
import org.jglue.fluentjson.JsonBuilderFactory;
import org.junit.Before;
import org.junit.Test;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.impl.service.consys.TestSystems.SystemInfo;
import org.vast.swe.SWEHelper;
import org.vast.swe.SWEJsonBindings;
import org.vast.util.TimeExtent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.stream.JsonWriter;
import net.opengis.swe.v20.BinaryEncoding;
import net.opengis.swe.v20.DataComponent;


public class TestDataStreams extends AbstractTestApiBase
{
    static final String URI_DECIMAL_PROP = SWEHelper.getCfUri("Temperature");
    static final String URI_BOOLEAN_PROP = SWEHelper.getDBpediaUri("Motion_detection");
    static final String URI_TEXT_PROP = SWEHelper.getDBpediaUri("Vehicle_registration_plate");
    
    TestSystems systemTests = new TestSystems();
    TestObservations obsTests;
    
    
    @Before
    public void setup() throws IOException, SensorHubException
    {
        super.setup();
        systemTests.apiRootUrl = apiRootUrl;
        
        obsTests = new TestObservations();
        obsTests.apiRootUrl = apiRootUrl;
    }
    
    
    @Test
    public void testAddSystemAndDatastreams() throws Exception
    {
        // add system
        var sysUrl = systemTests.addFeature(1, false);
        
        // add datastreams
        int numDatastreams = 10;
        var ids = new ArrayList<String>();
        for (int i = 0; i < numDatastreams; i++)
        {
            var url = addDatastreamOmJson(sysUrl, i);
            ids.add(getResourceId(url));
        }
        
        // get list of datastreams
        var jsonResp = sendGetRequestAndParseJson(concat(sysUrl, DATASTREAM_COLLECTION));
        checkCollectionItemIds(ids, jsonResp);
    }
    
    
    @Test
    public void testAddDatastreamAndGetByKeywords() throws Exception
    {
        // add system
        var sysUrl = systemTests.addFeature(1, false);
        
        var url1 = addDatastreamOmJson(sysUrl, 1, "Atmospheric Data");
        var url2 = addDatastreamOmJson(sysUrl, 2, "Water Quality Data");
        var url3 = addDatastreamOmJson(sysUrl, 3, "Traffic Info");
        
        var items = sendGetRequestAndGetItems(DATASTREAM_COLLECTION + "?q=atmos", 1);
        checkId(url1, items.get(0));
        var expectedObj = (JsonObject)sendGetRequestAndParseJson(url1);
        assertDatastreamEquals(expectedObj, items.get(0));
        
        items = sendGetRequestAndGetItems(DATASTREAM_COLLECTION + "?q=water", 1);
        checkId(url2, items.get(0));
        expectedObj = (JsonObject)sendGetRequestAndParseJson(url2);
        assertDatastreamEquals(expectedObj, items.get(0));
        
        items = sendGetRequestAndGetItems(DATASTREAM_COLLECTION + "?q=traffic", 1);
        checkId(url3, items.get(0));
        expectedObj = (JsonObject)sendGetRequestAndParseJson(url3);
        assertDatastreamEquals(expectedObj, items.get(0));
        
        items = sendGetRequestAndGetItems(DATASTREAM_COLLECTION + "?q=data", 2);
        checkCollectionItemIds(Set.of(url1, url2), items);
        
        items = sendGetRequestAndGetItems(DATASTREAM_COLLECTION + "?q=water,traffic", 2);
        checkCollectionItemIds(Set.of(url2, url3), items);
        
        items = sendGetRequestAndGetItems(DATASTREAM_COLLECTION + "?q=nothing", 0);
    }
    
    
    @Test
    public void testAddDatastreamAndGetByObsProperty() throws Exception
    {
        // add system
        var sysUrl = systemTests.addFeature(1, false);
        
        // add datastreams
        var url1 = addDatastreamOmJson(sysUrl, 1, "Temp Data", 1);
        var url2 = addDatastreamOmJson(sysUrl, 2, "Motion Data", 2);
        var url3 = addDatastreamOmJson(sysUrl, 3, "Traffic Data", 4);
        
        // get back with filter
        var items = sendGetRequestAndGetItems(DATASTREAM_COLLECTION + "?observedProperty=" + URI_DECIMAL_PROP, 1);
        checkId(url1, items.get(0));
        checkJsonProp("name", "Temp Data", items.get(0));
        
        items = sendGetRequestAndGetItems(DATASTREAM_COLLECTION + "?observedProperty=" + URI_BOOLEAN_PROP, 1);
        checkId(url2, items.get(0));
        checkJsonProp("name", "Motion Data", items.get(0));
        
        items = sendGetRequestAndGetItems(DATASTREAM_COLLECTION + "?observedProperty=" + URI_TEXT_PROP, 1);
        checkId(url3, items.get(0));
        checkJsonProp("name", "Traffic Data", items.get(0));
        
        // check we get nothing with bad prop
        items = sendGetRequestAndGetItems(DATASTREAM_COLLECTION + "?observedProperty=http://blabla/nothing", 0);
    }
    
    
    @Test
    public void testAddDatastreamWithValidTimes() throws Exception
    {
        // add system
        var sysUrl = systemTests.addFeature(1, false);
        
        // add datastreams
        var url1 = addDatastreamOmJson(sysUrl, 1, "Temp Data - Campaign 2021", TimeExtent.parse("2021-01-01Z/2021-12-31Z"), 1);
        var url2 = addDatastreamOmJson(sysUrl, 1, "Temp Data - Campaign 2022", TimeExtent.parse("2022-01-01Z/2022-12-31Z"), 1);
        var url3 = addDatastreamOmJson(sysUrl, 1, "Temp Data - Campaign 2023", TimeExtent.parse("2023-01-01Z/2023-12-31Z"), 1);
        
        // check we now have 3 different datastreams
        sendGetRequestAndGetItems(concat(sysUrl, DATASTREAM_COLLECTION), 3);
        sendGetRequestAndGetItems(DATASTREAM_COLLECTION, 3);
        
        // get each one by its valid time
        var items = sendGetRequestAndGetItems(DATASTREAM_COLLECTION + "?validTime=2021-12-30T23:59:59.999Z", 1);
        checkId(url1, items.get(0));
        items = sendGetRequestAndGetItems(DATASTREAM_COLLECTION + "?validTime=2022-01-01Z", 1);
        checkId(url2, items.get(0));
        items = sendGetRequestAndGetItems(DATASTREAM_COLLECTION + "?validTime=2023-07-01Z", 1);
        checkId(url3, items.get(0));
        
        // get some by time period
        items = sendGetRequestAndGetItems(DATASTREAM_COLLECTION + "?validTime=2022-07-01Z/2023-03-15Z", 2);
        checkCollectionItemIds(Set.of(url2, url3), items);
    }
    
    
    @Test
    public void testUpdateDatastreamAndGetById() throws Exception
    {
        // add system
        var sysUrl = systemTests.addFeature(1, false);
        
        // add datastreams
        var url1 = addDatastreamOmJson(sysUrl, 1, "Temp Data", 1);
        var url2 = addDatastreamOmJson(sysUrl, 2, "Motion Data", 2);
        var url3 = addDatastreamOmJson(sysUrl, 3, "Traffic Data", 4);
        
        // change name only
        updateDatastreamOmJson(url1, 1, "Room Temperature", 1, true);
        updateDatastreamOmJson(url2, 2, "Motion Detections", 2, true);
        
        // change name & structure
        updateDatastreamOmJson(url1, 1, "Room Temperature", 2, true);
        
        // check we still have 3 items
        var items = sendGetRequestAndGetItems(DATASTREAM_COLLECTION, 3);
        checkCollectionItemIds(Set.of(url1, url2, url3), items);
    }
    
    
    @Test
    public void testUpdateDatastreamBadOutputName() throws Exception
    {
        // add system
        var sysUrl = systemTests.addFeature(1, false);
        
        // add datastream
        var url1 = addDatastreamOmJson(sysUrl, 1, "Temp Data", 1);
        
        // check we cannot change the output name
        var resp = updateDatastreamOmJson(url1, 2, "Room Temperature", 1, false);
        assertEquals(400, resp.statusCode());
    }
    
    
    @Test
    public void testUpdateDatastreamChangeStructWithObs() throws Exception
    {
        // add system
        var sysUrl = systemTests.addFeature(1, false);
        
        // add datastream with 1 obs
        var url1 = addDatastreamOmJson(sysUrl, 1);
        obsTests.addObservation(url1, Instant.now(), 1);
        
        // check we cannot update structure anymore
        var resp = updateDatastreamOmJson(url1, 1, "Room Temperature", 2, false);
        assertEquals(400, resp.statusCode());
        
        // but we should still be able to update the name
        updateDatastreamOmJson(url1, 1, "All Variables", 1|2|4, true);
    }
    
    
    @Test
    public void testAddDatastreamAndDeleteById() throws Exception
    {
        var sysUrl = systemTests.addFeature(1, false);
        
        var url1 = addDatastreamOmJson(sysUrl, 1, "Temp Data", 1);
        var url2 = addDatastreamOmJson(sysUrl, 2, "Motion Data", 2);
        
        var items = sendGetRequestAndGetItems(DATASTREAM_COLLECTION, 2);
        checkCollectionItemIds(Set.of(url1, url2), items);
        
        sendDeleteRequestAndCheckStatus(url1, 204);
        items = sendGetRequestAndGetItems(DATASTREAM_COLLECTION, 1);
        checkCollectionItemIds(Set.of(url2), items);
        
        sendDeleteRequestAndCheckStatus(url2, 204);
        items = sendGetRequestAndGetItems(DATASTREAM_COLLECTION, 0);
    }
    
    
    @Test
    public void testAddSystemAndSubsystemsAndDatastreams() throws Exception
    {
        var rootSysInfo = systemTests.addSystemAndSubsystems(4, new int[] {2, 2}, 10);
        addDataStreamToSubSystems(rootSysInfo, 0, new int[] {0, 1, 2});
        
        // all datastreams
        sendGetRequestAndGetItems(DATASTREAM_COLLECTION, 2+2*2*2);
        
        // datastreams in parent system
        // TODO Currently the service returns datastreams one level down only
        // should it return all nested datastreams recursively?
        sendGetRequestAndGetItems(concat(rootSysInfo.url, DATASTREAM_COLLECTION), 2);
        
        // datastreams in subsystems
        for (var childSys: rootSysInfo.subsystems)
            sendGetRequestAndGetItems(concat(childSys.url, DATASTREAM_COLLECTION), 1+2*2);
    }
    
    
    // Non-Test helper methods
    
    protected void addDataStreamToSubSystems(SystemInfo sys, int level, int[] numDsPerLevel) throws Exception
    {
        for (int i = 0; i < numDsPerLevel[level]; i++)
        {
            var url = addDatastreamOmJson(sys.url, i+1);
            System.err.println("Added " + url);
            sys.datastreams.add(url);
        }
        
        if (level < numDsPerLevel.length-1)
        {
            for (var childSys: sys.subsystems)
                addDataStreamToSubSystems(childSys, level+1, numDsPerLevel);
        }
    }
    
    
    protected String addDatastreamOmJson(String sysUrl, int outputNum) throws Exception
    {
        return addDatastreamOmJson(sysUrl, outputNum, null, 1|2|4);
    }
    
    
    protected String addDatastreamOmJson(String sysUrl, int outputNum, String name) throws Exception
    {
        return addDatastreamOmJson(sysUrl, outputNum, name, 1|2|4);
    }
    
    
    protected String addDatastreamOmJson(String sysUrl, int outputNum, String name, int propSet) throws Exception
    {
        return addDatastreamOmJson(sysUrl, outputNum, name, null, propSet);
    }
    
    
    protected String addDatastreamOmJson(String sysUrl, int outputNum, String name, TimeExtent validTime, int propSet) throws Exception
    {
        // add datastream
        var json = createDatastreamOmJson(name, outputNum, validTime, propSet);
        var httpResp = sendPostRequest(concat(sysUrl, DATASTREAM_COLLECTION), json);
        var url = getLocation(httpResp);
        
        // get it back by id
        var jsonResp = sendGetRequestAndParseJson(url);
        checkId(url, jsonResp);
        assertDatastreamEquals(json, (JsonObject)jsonResp);
        
        return url;
    }
    
    
    protected HttpResponse<String> updateDatastreamOmJson(String path, int outputNum, String name, int propSet, boolean checkSuccess) throws Exception
    {
        var json = createDatastreamOmJson(name, outputNum, propSet);
        
        var resp = sendPutRequest(path, json);
        
        if (checkSuccess)
        {
            assertEquals(204, resp.statusCode());
            var jsonResp = sendGetRequestAndParseJson(path);
            checkId(path, jsonResp);
            assertDatastreamEquals(json, (JsonObject)jsonResp);
        }
        
        return resp;
    }
    
    
    protected JsonObject createDatastreamOmJson(String name, int outputNum, int propSet) throws Exception
    {
        return createDatastreamOmJson(name, outputNum, null, propSet);
    }
    
    
    protected JsonObject createDatastreamOmJson(String name, int outputNum, TimeExtent validTime, int propSet) throws Exception
    {
        var swe = new SWEHelper();
        var rec = swe.createRecord()
            .name(String.format("output%03d", outputNum))
            .addSamplingTimeIsoUTC("time");
        
        if ((propSet & 1) != 0)
        {    
            rec.addField("f1", swe.createQuantity()
                .definition(URI_DECIMAL_PROP)
                .label("Air Temp")
                .uomCode("Cel"));
        }
        
        if ((propSet & 2) != 0)
        {
            rec.addField("f2", swe.createBoolean()
                .definition(URI_BOOLEAN_PROP)
                .label("Motion Detected"));
        }
        
        if ((propSet & 4) != 0)
        {
            rec.addField("f3", swe.createText()
                .definition(URI_TEXT_PROP)
                .label("Plate Number")
                .pattern("[0-9A-Z]{6-10}"));
        }
        
        return createDatastreamOmJson(name, validTime, rec.build());
    }
    
    
    protected JsonObject createDatastreamOmJson(String name, TimeExtent validTime, DataComponent resultStruct) throws Exception
    {
        var json = JsonBuilderFactory.buildObject()
            .add("name", name != null ? name : "Datastream " + resultStruct.getName())
            .add("outputName", resultStruct.getName());
        
        if (validTime != null)
        {
            json.addArray("validTime")
                .add(validTime.begin().toString())
                .add(validTime.endsNow() ? "now" : validTime.end().toString())
            .end();
        }
        
        // result schema & encoding
        try
        {
            var schema = JsonBuilderFactory.buildObject()
                .add("obsFormat", "application/om+json")
                .getJson();
            
            // use existing streaming writer
            var jsonWriter = new JsonTreeWriter();
            var sweBindings = new SWEJsonBindings();
            sweBindings.writeDataComponent(jsonWriter, resultStruct, false);
            var resultSchema = jsonWriter.get();
            schema.add("resultSchema", resultSchema);
            
            var jsonObj = json.getJson();
            jsonObj.add("schema", schema);
            return jsonObj;
        }
        catch (Exception e)
        {
            throw new IOException("Error writing SWE Common result structure", e);
        }
    }
    
    
    protected JsonObject createDatastreamSweBinary(DataComponent resultStruct, BinaryEncoding resultEncoding) throws Exception
    {
        var buffer = new StringWriter();
        var writer = new JsonWriter(buffer);
        
        writer.beginObject();
        writer.name("name").value("Name of datastream " + resultStruct.getName());
        writer.name("outputName").value(resultStruct.getName());
        
        // result schema & encoding
        try
        {
            var sweBindings = new SWEJsonBindings();
            
            writer.name("schema").beginObject();
            
            writer.name("resultSchema");
            sweBindings.writeDataComponent(writer, resultStruct, false);
            
            writer.name("resultEncoding");
            sweBindings.writeAbstractEncoding(writer, resultEncoding);
            
            writer.endObject();
        }
        catch (Exception e)
        {
            throw new IOException("Error writing SWE Common result structure", e);
        }
        
        writer.endObject();
        
        return (JsonObject)JsonParser.parseString(buffer.getBuffer().toString());
    }
    
    
    protected void assertDatastreamEquals(JsonObject expected, JsonObject actual)
    {
        assertEquals(expected.get("name"), actual.get("name"));
        assertEquals(expected.get("description"), actual.get("description"));
        //assertEquals(expected.get("outputName"), actual.get("outputName"));
    }
}
