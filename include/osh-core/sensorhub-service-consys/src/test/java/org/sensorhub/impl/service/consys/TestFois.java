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
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jglue.fluentjson.JsonBuilderFactory;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.sensorhub.api.common.SensorHubException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class TestFois extends AbstractTestApiBase
{
    public static final String UID_FORMAT = "urn:osh:foi:test%03d";
    
    TestSystems systemTests = new TestSystems();
    
    
    @Before
    public void setup() throws IOException, SensorHubException
    {
        super.setup();
        systemTests.apiRootUrl = apiRootUrl;
    }
    
    
    @Test
    public void testAddRootFoisAndGetById() throws Exception
    {
        // add foi
        int numFois = 10;
        var ids = new ArrayList<String>();
        for (int i = 0; i < numFois; i++)
        {
            var url = addFoi(null, i);
            var id = url.substring(url.lastIndexOf('/')+1);
            ids.add(id);
        }
        
        // get list of fois
        var jsonResp = sendGetRequestAndParseJson(FOI_COLLECTION);
        checkCollectionItemIds(ids, jsonResp);
    }
    
    
    @Test
    public void testAddSystemFoisAndGetById() throws Exception
    {
        var sysUrl = systemTests.addFeature(10);
        
        // add foi
        int numFois = 10;
        var ids = new ArrayList<String>();
        for (int i = 0; i < numFois; i++)
        {
            var url = addFoi(sysUrl, i);
            var id = url.substring(url.lastIndexOf('/')+1);
            ids.add(id);
        }
        
        // get list of system fois
        var jsonResp = sendGetRequestAndParseJson(concat(sysUrl, FOI_COLLECTION));
        checkCollectionItemIds(ids, jsonResp);
        
        // get list of all fois
        jsonResp = sendGetRequestAndParseJson(FOI_COLLECTION);
        checkCollectionItemIds(ids, jsonResp);
    }
    
    
    @Test
    public void testAddSystemFoiBatchAndGetById() throws Exception
    {
        var sysUrl = systemTests.addFeature(10);
        addFoiBatch(sysUrl, 1, 20, true);
    }
    
    
    @Test
    public void testAddRootFoiBatchAndGetById() throws Exception
    {
        addFoiBatch(null, 1, 20, true);
    }
    
    
    @Test
    public void testAddSystemFoiBatchAndGetFromSystem() throws Exception
    {
        var sysUrl1 = systemTests.addFeature(3);
        var sysUrl2 = systemTests.addFeature(4);
        
        var urlList = addFoiBatch(sysUrl1, 100, 120);
        var idList = Lists.transform(urlList, AbstractTestApiBase::getResourceId);
        
        // get all fois
        var jsonResp = sendGetRequestAndParseJson(FOI_COLLECTION);
        checkCollectionItemIds(idList, jsonResp);
        
        // get correct system fois
        jsonResp = sendGetRequestAndParseJson(concat(sysUrl1, FOI_COLLECTION));
        checkCollectionItemIds(idList, jsonResp);
        
        // get from wrong system
        sendGetRequestAndGetItems(concat(sysUrl2, FOI_COLLECTION), 0);
    }
    
    
    @Test
    public void testAddRootFoiBatchAndGetAllById() throws Exception
    {
        var urlList = addFoiBatch(null, 100, 120);
        var idList = Lists.transform(urlList, AbstractTestApiBase::getResourceId);
        
        // get all fois
        var jsonResp = sendGetRequestAndParseJson(FOI_COLLECTION);
        checkCollectionItemIds(idList, jsonResp);
        
        // get with full id list
        jsonResp = sendGetRequestAndParseJson(FOI_COLLECTION + "?id=" + String.join(",", idList));
        checkCollectionItemIds(idList, jsonResp);
        
        // get with subset of id list
        var subList = idList.subList(3, 9);
        jsonResp = sendGetRequestAndParseJson(FOI_COLLECTION + "?id=" + String.join(",", subList));
        checkCollectionItemIds(subList, jsonResp);
    }
    
    
    @Test
    public void testGetFoiWrongId() throws Exception
    {
        var urlInvalidId = concat(FOI_COLLECTION, "toto");
        sendGetRequestAndCheckStatus(urlInvalidId, 404);
        
        addFoi(null, 1);
        sendGetRequestAndCheckStatus(urlInvalidId, 404);
    }
    
    
    @Test
    public void testAddDuplicateFoi() throws Exception
    {
        var json = createFoiGeoJson(1);
        
        var resp = sendPostRequest(FOI_COLLECTION, json);
        assertEquals(201, resp.statusCode());
        
        resp = sendPostRequest(FOI_COLLECTION, json);
        assertEquals(400, resp.statusCode());
    }
    
    
    @Test
    public void testAddRootFoisAndGetByUid() throws Exception
    {
        var uid1 = String.format(UID_FORMAT, 1);
        var url1 = addFoi(null, 1, false);
        
        var uid2 = String.format(UID_FORMAT, 5);
        var url2 = addFoi(null, 5);
        
        // get 1 at a time
        // test json resources fetched by ID and by UID are the same!
        var json = (JsonObject)sendGetRequestAndParseJson(url1);
        var items = sendGetRequestAndGetItems(FOI_COLLECTION + "?uid=" + uid1, 1);
        checkId(url1, items.get(0));
        checkFoiUid(uid1, items.get(0));
        json.remove("links");
        assertEquals(json, items.get(0));
        
        json = (JsonObject)sendGetRequestAndParseJson(url2);
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?uid=" + uid2, 1);
        checkId(url2, items.get(0));
        checkFoiUid(uid2, items.get(0));
        json.remove("links");
        assertEquals(json, items.get(0));
        
        // get both
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?uid=" + uid1 + "," + uid2, 2);
        checkId(url1, items.get(0));
        checkFoiUid(uid1, items.get(0));
        checkId(url2, items.get(1));
        checkFoiUid(uid2, items.get(1));
    }
    
    
    @Test
    public void testAddSystemFoisAndFilterByProps() throws Exception
    {
        var uid1 = String.format(UID_FORMAT, 1);
        var url1 = addFoi(null, 1, true, ImmutableMap.<String, Object>builder()
            .put("featureType", "building")
            .put("height", 53)
            .put("width", 10)
            .build());
        
        var uid2 = String.format(UID_FORMAT, 2);
        var url2 = addFoi(null, 2, true, ImmutableMap.<String, Object>builder()
            .put("featureType", "vehicle")
            .put("make", "ford")
            .put("length", 4.2)
            .put("width", 1.5)
            .build());
        
        var uid3 = String.format(UID_FORMAT, 3);
        var url3 = addFoi(null, 3, true, ImmutableMap.<String, Object>builder()
            .put("featureType", "river")
            .put("usgs_id", "R1256")
            .build());
        
        var uid4 = String.format(UID_FORMAT, 4);
        var url4 = addFoi(null, 4, true, ImmutableMap.<String, Object>builder()
            .put("featureType", "river")
            .put("usgs_id", "R2212")
            .build());
        
        // match one
        var items = sendGetRequestAndGetItems(FOI_COLLECTION + "?p:length=*", 1);
        checkId(url2, items.get(0));
        checkFoiUid(uid2, items.get(0));
        
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?featureType=building", 1);
        checkId(url1, items.get(0));
        checkFoiUid(uid1, items.get(0));
        
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?p:usgs_id=R2212", 1);
        checkId(url4, items.get(0));
        checkFoiUid(uid4, items.get(0));
        
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?p:length=4.2", 1);
        checkId(url2, items.get(0));
        checkFoiUid(uid2, items.get(0));
        
        // match one with AND
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?featureType=building&p:height=*", 1);
        checkId(url1, items.get(0));
        checkFoiUid(uid1, items.get(0));
        
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?featureType=river&p:usgs_id=R1256", 1);
        checkId(url3, items.get(0));
        checkFoiUid(uid3, items.get(0));

        // match several
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?featureType=river", 2);
        checkCollectionItemIds(Set.of(url3, url4), items);
        
        // match several with OR
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?featureType=river,building", 3);
        checkCollectionItemIds(Set.of(url1, url3, url4), items);
        
        // match several with wildcard
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?p:width=*", 2);
        checkCollectionItemIds(Set.of(url1, url2), items);
        
        // match none
        sendGetRequestAndGetItems(FOI_COLLECTION + "?p:prop2=nothing", 0);
        sendGetRequestAndGetItems(FOI_COLLECTION + "?p:prop=*", 0);
        sendGetRequestAndGetItems(FOI_COLLECTION + "?p:make=nothing", 0);
        sendGetRequestAndGetItems(FOI_COLLECTION + "?featureType=building&p:height=8", 0);
    }
    
    
    @Test
    public void testAddSystemFoisAndFilterByKeywords() throws Exception
    {
        var sysUrl = systemTests.addFeature(1);
        
        var url1 = addFoi(sysUrl, 1, false, ImmutableMap.<String, Object>builder()
            .put("description", "this is a sampling point")
            .build());
        
        var url2 = addFoi(sysUrl, 2, false, ImmutableMap.<String, Object>builder()
            .put("description", "this is a sampling surface")
            .build());
        
        var url3 = addFoi(sysUrl, 3, false, ImmutableMap.<String, Object>builder()
            .put("description", "this is a feature part")
            .build());
        
        // match one
        var items = sendGetRequestAndGetItems(FOI_COLLECTION + "?q=sampling%20point", 1);
        checkId(url1, items.get(0));
        
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?q=surface", 1);
        checkId(url2, items.get(0));
        
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?q=part", 1);
        checkId(url3, items.get(0));
        
        // match several
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?q=sampling", 3); // sampling is also in the default name!
        checkCollectionItemIds(Set.of(url1, url2, url3), items);
        
        // match several with OR
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?q=point,part", 2);
        checkCollectionItemIds(Set.of(url1, url3), items);
        
        // match none
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?q=toto", 0);
    }
    
    
    @Test
    public void testAddSystemFoisAndFilterByBbox() throws Exception
    {
        var sysUrl = systemTests.addFeature(1);
        var fac = new GeometryFactory();
        
        setDebug(true);
        var url1 = addFoi(sysUrl, 1, false,
            fac.createPolygon(new Coordinate[] {
                new Coordinate(45, 0),
                new Coordinate(46, 0),
                new Coordinate(46, 1),
                new Coordinate(45, 1),
                new Coordinate(45, 0)
            }),
            ImmutableMap.<String, Object>builder()
                .put("description", "this is a sampling surface")
                .build());
        
        var url2 = addFoi(sysUrl, 20, false, ImmutableMap.<String, Object>builder()
            .put("description", "this is a sampling surface")
            .build());
        
        var url3 = addFoi(sysUrl, 30, false, ImmutableMap.<String, Object>builder()
            .put("description", "this is a feature part")
            .build());
        
        // feature w/ no geom
        var url4 = addFoi(sysUrl, 3, false, null, ImmutableMap.<String, Object>builder()
            .put("description", "this is a feature w/o geom")
            .build());
        
        // match all
        var items = sendGetRequestAndGetItems(FOI_COLLECTION, 4);
        checkCollectionItemIds(Set.of(url1, url2, url3, url4), items);
        
        // match all w/ geom
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?bbox=-90,-180,90,180", 3);
        checkCollectionItemIds(Set.of(url1, url2, url3), items);
        
        // match one
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?bbox=40,0,50,1", 1);
        checkId(url1, items.get(0));
        
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?bbox=45.5,0.5,45.6,0.6", 1);
        checkId(url1, items.get(0));
        
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?bbox=19.9,39.9,20.1,40.1", 1);
        checkId(url2, items.get(0));
        
        // match several
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?bbox=20,40,30,60", 2);
        checkCollectionItemIds(Set.of(url2, url3), items);
        
        // match none
        items = sendGetRequestAndGetItems(FOI_COLLECTION + "?bbox=-1,-1,1,1", 0);
    }
    
    
    @Test
    public void testUpdateFoiCustomPropsAndGetById() throws Exception
    {
        var url1 = addFoi(null, 5, true, ImmutableMap.<String, Object>builder()
            .put("description", "This is FOI 1")
            .put("num_prop1", 53)
            .put("str_prop2", "string")
            .build());
        
        var url2 = addFoi(null, 23, true, ImmutableMap.<String, Object>builder()
            .put("description", "This is FOI 2")
            .put("size", 21)
            .put("mode", "turbo")
            .build());
        
        updateFoi(url1, 5, true, ImmutableMap.<String, Object>builder()
            .put("description", "Updated version of first system")
            .put("num_prop1", 15)
            .put("str_prop2", "text")
            .build());
        
        updateFoi(url2, 23, true, ImmutableMap.<String, Object>builder()
            .put("description", "Updated version of system 2")
            .put("size", 22)
            .put("mode", "turbo")
            .put("str_prop2", "text")
            .build());
    }
    
    
    @Test
    public void testUpdateFoiWrongId() throws Exception
    {
        addFoi(null, 46, true, ImmutableMap.<String, Object>builder()
            .put("description", "This is my first system")
            .put("num_prop1", 53)
            .put("str_prop2", "string")
            .build());
        
        var urlInvalidId = concat(FOI_COLLECTION, "917pe5d33noso");
        sendPutRequestAndCheckStatus(urlInvalidId, new JsonObject(), 404);
        
        urlInvalidId = concat(FOI_COLLECTION, "toto");
        sendPutRequestAndCheckStatus(urlInvalidId, new JsonObject(), 404);
    }
    
    
    @Test
    public void testUpdateFoiBadUid() throws Exception
    {
        var url1 = addFoi(null, 1, true, ImmutableMap.<String, Object>builder()
            .put("description", "This is an FOI")
            .put("num_prop1", 53)
            .put("str_prop2", "string")
            .build());
        
        var resp = updateFoi(url1, 22, false, ImmutableMap.<String, Object>builder()
            .put("description", "Updated version of FOI")
            .put("num_prop1", 15)
            .put("str_prop2", "text")
            .build());
        assertEquals(400, resp.statusCode());
    }
    
    
    @Test
    public void testAddFoiAndDeleteById() throws Exception
    {
        var url1 = addFoi(null, 1, false);
        var url2 = addFoi(null, 10, false);
        
        var items = sendGetRequestAndGetItems(FOI_COLLECTION, 2);
        checkCollectionItemIds(Set.of(url1, url2), items);
        
        sendDeleteRequestAndCheckStatus(url1, 204);
        items = sendGetRequestAndGetItems(FOI_COLLECTION, 1);
        checkId(url2, items.get(0));
        
        sendDeleteRequestAndCheckStatus(url2, 204);
        items = sendGetRequestAndGetItems(FOI_COLLECTION, 0);
    }
    
    
    @Test
    public void testDeleteFoiWrongId() throws Exception
    {
        var urlInvalidId = concat(FOI_COLLECTION, "toto");
        sendDeleteRequestAndCheckStatus(urlInvalidId, 404);
        
        var url1 = addFoi(null, 11, false);
        
        sendDeleteRequestAndCheckStatus(urlInvalidId, 404);
        sendDeleteRequestAndCheckStatus(url1, 204);
        
        // delete again and check it doesn't exist anymore
        sendDeleteRequestAndCheckStatus(url1, 404);
    }
    
    
    protected String addFoi(String sysUrl, int num) throws Exception
    {
        return addFoi(sysUrl, num, true);
    }
    
    
    protected String addFoi(String sysUrl, int num, boolean checkGet) throws Exception
    {
        return addFoi(sysUrl, num, checkGet,
            new GeometryFactory().createPoint(new Coordinate(30.56871, 58.479315)),
            Collections.emptyMap());
    }
    
    
    protected String addFoi(String sysUrl, int num, boolean checkGet, Map<String, Object> props) throws Exception
    {
        return addFoi(sysUrl, num, true,
            new GeometryFactory().createPoint(new Coordinate(num, num*2)),
            props);
    }
    
    
    protected String addFoi(String sysUrl, int num, boolean checkGet, Geometry geom, Map<String, Object> props) throws Exception
    {
        var postUrl = sysUrl != null ? concat(sysUrl, FOI_COLLECTION) : FOI_COLLECTION;
        
        // add foi
        var json = createFoiGeoJson(num, geom, props);
        var httpResp = sendPostRequest(postUrl, json);
        var url = getLocation(httpResp);
        
        // get it back by id
        if (checkGet)
        {
            var jsonResp = sendGetRequestAndParseJson(url);
            checkId(url, jsonResp);
            assertFoiEquals(json, (JsonObject)jsonResp);
        }
        
        return url;
    }
    
    
    protected List<String> addFoiBatch(String sysUrl, int startNum, int endNum) throws Exception
    {
        return addFoiBatch(sysUrl, startNum, endNum, false);
    }
    
    
    protected List<String> addFoiBatch(String sysUrl, int startNum, int endNum, boolean checkGet) throws Exception
    {
        var array = new JsonArray();
        for (int num = startNum; num <= endNum; num++)
        {
            var json = createFoiGeoJson(num);
            array.add(json);
        }
        
        var postUrl = sysUrl != null ? concat(sysUrl, FOI_COLLECTION) : FOI_COLLECTION;
        var urlList = sendPostRequestAndParseUrlList(postUrl, array);
        assertEquals("Wrong number of resources created", array.size(), urlList.size());
        
        if (checkGet)
        {
            for (int i = 0; i < array.size(); i++)
            {
                var url = urlList.get(i);
                var json = array.get(i);
                var jsonResp = sendGetRequestAndParseJson(url);
                checkId(url, jsonResp);
                assertFoiEquals((JsonObject)json, (JsonObject)jsonResp);
            }
        }
        
        return urlList;
    }
    
    
    protected HttpResponse<String> updateFoi(String path, int num, boolean checkGet, Map<String, Object> props) throws Exception
    {
        return updateFoi(path, num, checkGet, null, props);
    }
    
    
    protected HttpResponse<String> updateFoi(String path, int num, boolean checkGet, Geometry geom, Map<String, Object> props) throws Exception
    {
        var json = createFoiGeoJson(num, geom, props);
        
        var resp = sendPutRequest(path, json);
        
        if (checkGet)
        {
            var jsonResp = sendGetRequestAndParseJson(path);
            checkId(path, jsonResp);
            assertFoiEquals(json, (JsonObject)jsonResp);
        }
        
        return resp;
    }
    
    
    protected JsonObject createFoiGeoJson(int fNum) throws Exception
    {
        return createFoiGeoJson(fNum,
            new GeometryFactory().createPoint(new Coordinate(30.56871, 58.479315)),
            ImmutableMap.<String, Object>builder()
                .put("description", "Sensor Station #" + fNum)
                .put("featureType", "http://www.opengis.net/def/featureType/MyFeature")
                .build()
            );
    }
    
    
    protected JsonObject createFoiGeoJson(int fNum, Geometry geom, Map<String, Object> props) throws Exception
    {
        var json = JsonBuilderFactory.buildObject()
            .add("type", "Feature");
            
        if (geom instanceof Point)
        {
            json.addObject("geometry")
                .add("type", "Point")
                .addArray("coordinates")
                    .add(((Point) geom).getX())
                    .add(((Point) geom).getY())
                .end()
            .end();
        }
        else if (geom instanceof LineString)
        {
            var coordArray = json.addObject("geometry")
                .add("type", "LineString")
                .addArray("coordinates");
            
            for (var c: geom.getCoordinates())
            {
                coordArray.addArray()
                    .add(c.getX())
                    .add(c.getY())
                .end();
            }
            
            coordArray.end();
            json.end(); // end geometry obj
        }
        else if (geom instanceof Polygon)
        {
            var coordArray = json.addObject("geometry")
                .add("type", "Polygon")
                .addArray("coordinates")
                .addArray();
            
            for (var c: ((Polygon) geom).getExteriorRing().getCoordinates())
            {
                coordArray.addArray()
                    .add(c.getX())
                    .add(c.getY())
                .end();
            }
            
            coordArray.end().end();
            json.end(); // end geometry obj
        }
        else
            json.addNull("geometry");
        
        var jsonProps = json.addObject("properties")
            .add("uid", String.format(UID_FORMAT, fNum))
            .add("name", "Sampling Feature #" + fNum);
        
        // add all other properties
        for (var prop: props.entrySet())
        {
            var val = prop.getValue();
            if (val instanceof String)
                jsonProps.add(prop.getKey(), (String)val);
            else if (val instanceof Number)
                jsonProps.add(prop.getKey(), (Number)val);
            else
                throw new IllegalArgumentException();
        }
        
        return jsonProps.end().getJson();
    }
    
    
    protected void assertFoiEquals(JsonObject expected, JsonObject actual)
    {
        // remove some fields not present in POST request before comparison
        actual.remove("id");
        actual.remove("links");
        assertEquals(expected, actual);
    }
    
    
    protected void checkFoiUid(int sysNum, JsonElement actual)
    {
        checkFoiUid(String.format(UID_FORMAT, sysNum), actual);
    }
    
    
    protected void checkFoiUid(String expectedUid, JsonElement actual)
    {
        assertTrue(actual instanceof JsonObject);
        var uid = ((JsonObject)actual).getAsJsonObject("properties").get("uid").getAsString();
        assertEquals(expectedUid, uid);
    }
    
}
