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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.jglue.fluentjson.JsonBuilderFactory;
import org.vast.sensorML.SMLHelper;
import org.vast.sensorML.SMLJsonBindings;
import org.vast.swe.SWEHelper;
import org.vast.util.TimeExtent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;


public class TestDeployments extends AbstractTestAllSmlFeatures
{
    public static final String UID_FORMAT = "urn:osh:deplyt:test%03d";
    static SMLHelper sml = new SMLHelper();
    
    
    public static class DeploymentInfo
    {
        public String url;
        public String id;
        public Collection<DeploymentInfo> systems = new ArrayList<>();
        public Collection<String> fois = new ArrayList<>();
        
        public DeploymentInfo(String url)
        {
            this.url = url;
            this.id = getResourceId(url);
        }
    }
    
    
    public TestDeployments()
    {
        super(DEPLOYMENT_COLLECTION, UID_FORMAT);
    }
    
    
    /*@Test
    public void testAddDeployedSystemsAndGetById() throws Exception
    {
        // add system group
        var groupUrl = addFeature(1);
        
        // add members
        int numMembers = 10;
        var ids = new ArrayList<String>();
        for (int i = 0; i < numMembers; i++)
        {
            var url = addMember(groupUrl, i+2);
            var id = getResourceId(url);
            ids.add(id);
        }
        
        // get list of members
        var jsonResp = sendGetRequestAndParseJson(concat(groupUrl, MEMBER_COLLECTION));
        checkCollectionItemIds(ids, jsonResp);
    }*/
    
    
    // Non-Test helper methods
    
    @Override
    protected JsonObject createFeatureGeoJson(int procNum, TimeExtent validTime, Map<String, Object> props) throws Exception
    {
        var json = JsonBuilderFactory.buildObject()
            .add("type", "Feature")
            .addNull("geometry")
            .addObject("properties")
              .add("uid", String.format(UID_FORMAT, procNum))
              .add("name", "Test Deployment #" + procNum);
        
        if (validTime != null)
        {
            json.addArray("validTime")
                .add(validTime.begin().toString())
                .add(validTime.endsNow() ? "now" : validTime.end().toString())
            .end();
        }
        
        // add all other properties
        for (var prop: props.entrySet())
        {
            var val = prop.getValue();
            if (val instanceof String)
                json.add(prop.getKey(), (String)val);
            else if (val instanceof Number)
                json.add(prop.getKey(), (Number)val);
            else
                throw new IllegalArgumentException();
        }
        
        return json.end().getJson();
    }
    
    
    @Override
    protected JsonObject createFeatureSmlJson(int procNum) throws Exception
    {
        var numId = String.format("%03d", procNum);
        var builder = sml.createDeployment()
            .uniqueID(String.format(UID_FORMAT, procNum))
            .description("Deployment registered using CONSYS API")
            .name("Test Deployment")
            .addIdentifier(sml.identifiers.shortName("Artic Mission #" + numId))
            .addIdentifier("Mission ID", SWEHelper.getPropertyUri("MissionID"), "SD-1405");
        
        var strWriter = new StringWriter();
        try (var writer = new JsonWriter(strWriter))
        {
            new SMLJsonBindings().writeDescribedObject(writer, builder.build());
        }
        
        return (JsonObject)JsonParser.parseString(strWriter.toString());
    }
    
    
    @Override
    protected void assertFeatureEquals(JsonObject expected, JsonObject actual)
    {
        actual.remove("id");
        actual.remove("links");
        assertEquals(expected, actual);
    }
    
    
    protected String addMember(String parentUrl, int num) throws Exception
    {
        // add group member
        var json = createFeatureGeoJson(num);
        var httpResp = sendPostRequest(concat(parentUrl, MEMBER_COLLECTION), json);
        var url = getLocation(httpResp);
        
        // get it back by id
        var jsonResp = sendGetRequestAndParseJson(url);
        //System.out.println(gson.toJson(jsonResp));
        checkId(url, jsonResp);
        assertFeatureEquals(json, (JsonObject)jsonResp);
        
        return url;
    }
    
}
