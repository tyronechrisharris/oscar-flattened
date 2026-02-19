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
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.vast.util.TimeExtent;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public abstract class AbstractTestAllFeatures extends AbstractTestApiBase
{
    protected final String collectionName;
    protected final String uidFormat;
    
    
    protected AbstractTestAllFeatures(String collectionName, String uidFormat)
    {
        this.collectionName = collectionName;
        this.uidFormat = uidFormat;
    }
    
    
    @Test
    public void testAddFeatureAndGetById() throws Exception
    {
        addFeature(1, true);
        addFeature(10, true);
    }
    
    
    @Test
    public void testAddFeatureBatchAndGetById() throws Exception
    {
        addFeatureBatch(1, 20, true);
    }
    
    
    @Test
    public void testAddFeatureBatchAndGetAllById() throws Exception
    {
        var urlList = addFeatureBatch(100, 110);
        var idList = Lists.transform(urlList, AbstractTestApiBase::getResourceId);
        
        // get request with full id list
        var jsonResp = sendGetRequestAndParseJson(collectionName + "?id=" + String.join(",", idList));
        checkCollectionItemIds(idList, jsonResp);
        
        // get request with subset of id list
        var subList = idList.subList(3, 9);
        jsonResp = sendGetRequestAndParseJson(collectionName + "?id=" + String.join(",", subList));
        checkCollectionItemIds(subList, jsonResp);
    }
    
    
    @Test
    public void testGetFeatureWrongId() throws Exception
    {
        var urlInvalidId = concat(collectionName, "toto");
        sendGetRequestAndCheckStatus(urlInvalidId, 404);
        
        addFeature(1, false);
        sendGetRequestAndCheckStatus(urlInvalidId, 404);
    }
    
    
    @Test
    public void testAddFeatureCustomPropsAndGetById() throws Exception
    {
        addFeature(5, true, ImmutableMap.<String, Object>builder()
            .put("description", "Human readable system description")
            .put("num_prop1", 53)
            .put("str_prop2", "string")
            .build());
    }
    
    
    /*removed test for now since we now return success in this case
    @Test
    public void testAddDuplicateFeature() throws Exception
    {
        var json = createFeatureGeoJson(1);
        
        var resp = sendPostRequest(collectionName, json);
        assertEquals(201, resp.statusCode());
        
        resp = sendPostRequest(collectionName, json);
        assertEquals(400, resp.statusCode());
    }*/
    
    
    @Test
    public void testAddFeaturesAndGetByUid() throws Exception
    {
        var uid1 = String.format(uidFormat, 1);
        var url1 = addFeature(1, false);
        
        var uid2 = String.format(uidFormat, 5);
        var url2 = addFeature(5, false);
        
        // get 1 at a time
        // test json resources fetched by ID and by UID are the same!
        var json = (JsonObject)sendGetRequestAndParseJson(url1);
        var items = sendGetRequestAndGetItems(collectionName + "?uid=" + uid1, 1);
        checkId(url1, items.get(0));
        checkFeatureUid(uid1, items.get(0));
        json.remove("links");
        assertEquals(json, items.get(0));
        
        json = (JsonObject)sendGetRequestAndParseJson(url2);
        items = sendGetRequestAndGetItems(collectionName + "?uid=" + uid2, 1);
        checkId(url2, items.get(0));
        checkFeatureUid(uid2, items.get(0));
        json.remove("links");
        assertEquals(json, items.get(0));
        
        // get both
        items = sendGetRequestAndGetItems(collectionName + "?uid=" + uid1 + "," + uid2, 2);
        checkId(url1, items.get(0));
        checkFeatureUid(uid1, items.get(0));
        checkId(url2, items.get(1));
        checkFeatureUid(uid2, items.get(1));
    }
    
    
    @Test
    public void testAddFeaturesAndFilterByProps() throws Exception
    {
        var uid1 = String.format(uidFormat, 1);
        var url1 = addFeature(1, false, ImmutableMap.<String, Object>builder()
            .put("prop1", 53)
            .put("prop2", "temp")
            .build());
        
        var uid2 = String.format(uidFormat, 2);
        var url2 = addFeature(2, false, ImmutableMap.<String, Object>builder()
            .put("prop1", 22)
            .put("prop2", "pressure")
            .build());
        
        var uid3 = String.format(uidFormat, 3);
        var url3 = addFeature(3, false, ImmutableMap.<String, Object>builder()
            .put("prop1", 53)
            .put("prop2", "pressure")
            .build());
        
        // match one
        var items = sendGetRequestAndGetItems(collectionName + "?p:prop1=22", 1);
        checkId(url2, items.get(0));
        checkFeatureUid(uid2, items.get(0));
        checkFeatureProp("prop1", Double.valueOf(22), items.get(0));
        
        items = sendGetRequestAndGetItems(collectionName + "?p:prop2=temp", 1);
        checkId(url1, items.get(0));
        checkFeatureUid(uid1, items.get(0));
        checkFeatureProp("prop2", "temp", items.get(0));
        
        // match one with AND
        items = sendGetRequestAndGetItems(collectionName + "?p:prop2=pressure&p:prop1=53", 1);
        checkId(url3, items.get(0));
        checkFeatureUid(uid3, items.get(0));
        checkFeatureProp("prop1", Double.valueOf(53), items.get(0));
        checkFeatureProp("prop2", "pressure", items.get(0));

        // match several
        items = sendGetRequestAndGetItems(collectionName + "?p:prop2=pressure", 2);
        checkCollectionItemIds(Set.of(url2, url3), items);
        
        // match several with OR
        items = sendGetRequestAndGetItems(collectionName + "?p:prop2=pressure,temp", 3);
        checkCollectionItemIds(Set.of(url1, url2, url3), items);
        
        // match several with wildcard
        items = sendGetRequestAndGetItems(collectionName + "?p:prop2=press*", 2);
        checkCollectionItemIds(Set.of(url2, url3), items);
        
        // match none
        sendGetRequestAndGetItems(collectionName + "?p:prop2=nothing", 0);
    }
    
    
    @Test
    public void testAddFeaturesAndFilterByKeywords() throws Exception
    {
        var url1 = addFeature(1, false, ImmutableMap.<String, Object>builder()
            .put("description", "this is a thermometer")
            .build());
        
        var url2 = addFeature(2, false, ImmutableMap.<String, Object>builder()
            .put("description", "this is a barometer")
            .build());
        
        var url3 = addFeature(3, false, ImmutableMap.<String, Object>builder()
            .put("description", "this is a camera")
            .build());
        
        // match one
        var items = sendGetRequestAndGetItems(collectionName + "?q=thermo", 1);
        checkId(url1, items.get(0));
        
        items = sendGetRequestAndGetItems(collectionName + "?q=camera", 1);
        checkId(url3, items.get(0));
        
        // match several with OR
        items = sendGetRequestAndGetItems(collectionName + "?q=thermo,baro", 2);
        checkCollectionItemIds(Set.of(url1, url2), items);
        
        // match none
        items = sendGetRequestAndGetItems(collectionName + "?q=borehole", 0);
    }
    
    
    @Test
    public void testAddFeaturesAndGetByTime() throws Exception
    {
        var url1 = addFeature(1, true, TimeExtent.parse("2000-03-16T23:45:12Z/2001-07-22T11:38:56Z"), Collections.emptyMap());
        var url2 = addFeature(2, true, TimeExtent.parse("2010-01-21T13:15:37Z/2020-07-01T08:24:46Z"), Collections.emptyMap());
        
        // get one
        var items = sendGetRequestAndGetItems(collectionName + "?validTime=2001-01-01Z", 1);
        checkCollectionItemIds(Set.of(url1), items);
        
        items = sendGetRequestAndGetItems(collectionName + "?validTime=2013-01-01Z", 1);
        checkCollectionItemIds(Set.of(url2), items);
        
        items = sendGetRequestAndGetItems(collectionName + "?validTime=2013-01-01Z/now", 1);
        checkCollectionItemIds(Set.of(url2), items);
        
        // get both
        items = sendGetRequestAndGetItems(collectionName + "?validTime=../..", 2);
        checkCollectionItemIds(Set.of(url1, url2), items);
        
        items = sendGetRequestAndGetItems(collectionName + "?validTime=2001-01-01Z/2012-01-01Z", 2);
        checkCollectionItemIds(Set.of(url1, url2), items);
        
        items = sendGetRequestAndGetItems(collectionName + "?validTime=2001-01-01Z/now", 2);
        checkCollectionItemIds(Set.of(url1, url2), items);
        
        items = sendGetRequestAndGetItems(collectionName + "?validTime=1900-01-01Z/2050-01-01Z", 2);
        checkCollectionItemIds(Set.of(url1, url2), items);
    }
    
    
    @Test
    public void testAddFeatureVersionsAndGetByTime() throws Exception
    {
        var url1 = addFeature(1, true, TimeExtent.parse("2000-03-16T23:45:12Z/2001-07-22T11:38:56Z"), Collections.emptyMap());
        
        // get current
        var items = sendGetRequestAndGetItems(collectionName, 1);
        checkFeatureValidTime("2000-03-16T23:45:12Z", "2001-07-22T11:38:56Z", items.get(0));
        
        // add more
        addFeature(1, false, TimeExtent.parse("1990-01-21T13:15:37Z/2000-01-01T08:24:46Z"), Collections.emptyMap());
        addFeature(1, true, TimeExtent.parse("2010-01-21T13:15:37Z/2014-07-01T08:24:46Z"), Collections.emptyMap());
        
        // get current
        items = sendGetRequestAndGetItems(collectionName, 1);
        checkFeatureValidTime("2010-01-21T13:15:37Z", "2014-07-01T08:24:46Z", items.get(0));
        
        // add more
        addFeature(1, false, TimeExtent.parse("2015-01-21T13:15:37Z/now"), Collections.emptyMap());
        addFeature(1, false, TimeExtent.parse("2017-09-30T13:15:37Z/now"), Collections.emptyMap());
        
        // get current
        items = sendGetRequestAndGetItems(collectionName, 1);
        checkFeatureValidTime("2017-09-30T13:15:37Z", "now", items.get(0));

        // add more
        addFeature(1, false, TimeExtent.parse("2020-04-18T00:00:00Z/now"), Collections.emptyMap());
        
        // get current
        items = sendGetRequestAndGetItems(collectionName, 1);
        checkFeatureValidTime("2020-04-18T00:00:00Z", "now", items.get(0));
        
        items = sendGetRequestAndGetItems(collectionName + "?validTime=2021-01-01Z/now", 1);
        checkFeatureValidTime("2020-04-18T00:00:00Z", "now", items.get(0));
        
        var json = sendGetRequestAndParseJson(url1);
        checkFeatureValidTime("2020-04-18T00:00:00Z", "now", json);
        
        // get all versions
        items = sendGetRequestAndGetItems(collectionName + "?validTime=../..", 6);
        
        // get specific version
        items = sendGetRequestAndGetItems(collectionName + "?validTime=1991-05-08Z", 1);
        checkFeatureValidTime("1990-01-21T13:15:37Z", "2000-01-01T08:24:46Z", items.get(0));
        
        items = sendGetRequestAndGetItems(collectionName + "?validTime=2010-01-21T13:15:37Z", 1);
        checkFeatureValidTime("2010-01-21T13:15:37Z", "2014-07-01T08:24:46Z", items.get(0));
        
        items = sendGetRequestAndGetItems(collectionName + "?validTime=2014-01-01Z", 1);
        checkFeatureValidTime("2010-01-21T13:15:37Z", "2014-07-01T08:24:46Z", items.get(0));
        
        items = sendGetRequestAndGetItems(collectionName + "?validTime=2020-04-17T23:59:59Z", 1);
        checkFeatureValidTime("2017-09-30T13:15:37Z", "2020-04-18T00:00:00Z", items.get(0));
        
        items = sendGetRequestAndGetItems(collectionName + "?validTime=2021-01-01Z", 1);
        checkFeatureValidTime("2020-04-18T00:00:00Z", "now", items.get(0));
        
        items = sendGetRequestAndGetItems(collectionName + "?validTime=now", 1);
        checkFeatureValidTime("2020-04-18T00:00:00Z", "now", items.get(0));
        
        // select none
        items = sendGetRequestAndGetItems(collectionName + "?validTime=2010-01-21T13:15:36Z", 0);
    }
    
    
    @Test
    public void testUpdateFeatureAndGetById() throws Exception
    {
        var url1 = addFeature(5, true, ImmutableMap.<String, Object>builder()
            .put("description", "This is my first system")
            .put("num_prop1", 53)
            .put("str_prop2", "string")
            .build());
        
        var url2 = addFeature(23, true, ImmutableMap.<String, Object>builder()
            .put("description", "This is system 2")
            .put("size", 21)
            .put("mode", "turbo")
            .build());
        
        updateFeature(url1, 5, true, ImmutableMap.<String, Object>builder()
            .put("description", "Updated version of first system")
            .put("num_prop1", 15)
            .put("str_prop2", "text")
            .build());
        
        updateFeature(url2, 23, true, ImmutableMap.<String, Object>builder()
            .put("description", "Updated version of system 2")
            .put("size", 22)
            .put("mode", "turbo")
            .put("str_prop2", "text")
            .build());
    }
    
    
    @Test
    public void testUpdateFeatureWrongId() throws Exception
    {
        addFeature(55, true, ImmutableMap.<String, Object>builder()
            .put("description", "This is my first system")
            .put("num_prop1", 53)
            .put("str_prop2", "string")
            .build());
        
        var urlInvalidId = concat(collectionName, "917pe5d33noso");
        sendPutRequestAndCheckStatus(urlInvalidId, new JsonObject(), 404);
        
        var resp = updateFeature(urlInvalidId, 55, false, ImmutableMap.<String, Object>builder()
            .put("description", "Updated version of first system")
            .put("num_prop1", 15)
            .put("str_prop2", "text")
            .build());
        assertEquals(404, resp.statusCode());
    }
    
    
    @Test
    public void testUpdateFeatureBadUid() throws Exception
    {
        var url1 = addFeature(55, true, ImmutableMap.<String, Object>builder()
            .put("description", "This is my first system")
            .put("num_prop1", 53)
            .put("str_prop2", "string")
            .build());
        
        var resp = updateFeature(url1, 22, false, ImmutableMap.<String, Object>builder()
            .put("description", "Updated version of first system")
            .put("num_prop1", 15)
            .put("str_prop2", "text")
            .build());
        assertEquals(400, resp.statusCode());
    }
    
    
    @Test
    public void testAddFeatureAndDeleteById() throws Exception
    {
        var url1 = addFeature(1, false);
        var url2 = addFeature(10, false);
        
        var items = sendGetRequestAndGetItems(collectionName, 2);
        checkCollectionItemIds(Set.of(url1, url2), items);
        
        sendDeleteRequestAndCheckStatus(url1, 204);
        items = sendGetRequestAndGetItems(collectionName, 1);
        checkId(url2, items.get(0));
        
        sendDeleteRequestAndCheckStatus(url2, 204);
        items = sendGetRequestAndGetItems(collectionName, 0);
    }
    
    
    @Test
    public void testDeleteFeatureWrongId() throws Exception
    {
        var urlInvalidId = concat(collectionName, "toto");
        sendDeleteRequestAndCheckStatus(urlInvalidId, 404);
        
        var url1 = addFeature(1, false);
        
        sendDeleteRequestAndCheckStatus(urlInvalidId, 404);
        sendDeleteRequestAndCheckStatus(url1, 204);
        
        // delete again and check it doesn't exist anymore
        sendDeleteRequestAndCheckStatus(url1, 404);
    }
    
    
    // Non-Test helper methods
    
    protected JsonObject createFeatureGeoJson(int procNum) throws Exception
    {
        return createFeatureGeoJson(procNum, Collections.emptyMap());
    }
    
    
    protected JsonObject createFeatureGeoJson(int procNum, Map<String, Object> props) throws Exception
    {
        return createFeatureGeoJson(procNum, null, props);
    }
    
    
    protected abstract JsonObject createFeatureGeoJson(int procNum, TimeExtent validTime, Map<String, Object> props) throws Exception;
    
    
    protected String addFeature(int num) throws Exception
    {
        return addFeature(num, false);
    }
    
    
    protected String addFeature(int num, boolean checkGet) throws Exception
    {
        return addFeature(num, checkGet, Collections.emptyMap());
    }
    
    
    protected String addFeature(int num, boolean checkGet, Map<String, Object> props) throws Exception
    {
        return addFeature(num, checkGet, null, props);
    }
    
    
    protected String addFeature(int num, boolean checkGet, TimeExtent validTime, Map<String, Object> props) throws Exception
    {
        var json = createFeatureGeoJson(num, validTime, props);
        
        var httpResp = sendPostRequest(collectionName, json);
        var url = getLocation(httpResp);
        
        if (checkGet)
        {
            var jsonResp = sendGetRequestAndParseJson(url);
            checkId(url, jsonResp);
            assertFeatureEquals(json, (JsonObject)jsonResp);
        }
        
        return url;
    }
    
    
    protected List<String> addFeatureBatch(int startNum, int endNum) throws Exception
    {
        return addFeatureBatch(startNum, endNum, false);
    }
    
    
    protected List<String> addFeatureBatch(int startNum, int endNum, boolean checkGet) throws Exception
    {
        var array = new JsonArray();
        for (int num = startNum; num <= endNum; num++)
        {
            var json = createFeatureGeoJson(num);
            array.add(json);
        }
        
        var urlList = sendPostRequestAndParseUrlList(collectionName, array);
        assertEquals("Wrong number of resources created", array.size(), urlList.size());
        
        if (checkGet)
        {
            for (int i = 0; i < array.size(); i++)
            {
                var url = urlList.get(i);
                var json = array.get(i);
                var jsonResp = sendGetRequestAndParseJson(url);
                checkId(url, jsonResp);
                assertFeatureEquals((JsonObject)json, (JsonObject)jsonResp);
            }
        }
        
        return urlList;
    }
    
    
    protected HttpResponse<String> updateFeature(String path, int num, boolean checkGet, Map<String, Object> props) throws Exception
    {
        var json = createFeatureGeoJson(num, props);
        
        var resp = sendPutRequest(path, json);
        
        if (checkGet)
        {
            var jsonResp = sendGetRequestAndParseJson(path);
            checkId(path, jsonResp);
            assertFeatureEquals(json, (JsonObject)jsonResp);
        }
        
        return resp;
    }
    
    
    protected void assertFeatureEquals(JsonObject expected, JsonObject actual)
    {
        actual.remove("id");
        actual.remove("links");
        assertEquals(expected, actual);
    }
    
    
    protected void checkFeatureUid(int sysNum, JsonElement actual)
    {
        checkFeatureUid(String.format(uidFormat, sysNum), actual);
    }
    
    
    protected void checkFeatureUid(String expectedUid, JsonElement actual)
    {
        assertTrue(actual instanceof JsonObject);
        var uid = ((JsonObject)actual).getAsJsonObject("properties").get("uid").getAsString();
        assertEquals(expectedUid, uid);
    }
    
}
