/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2023 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import com.google.gson.JsonObject;


public abstract class AbstractTestAllSmlFeatures extends AbstractTestAllFeatures
{

    protected AbstractTestAllSmlFeatures(String collectionName, String uidFormat)
    {
        super(collectionName, uidFormat);
    }
    
    
    @Test
    public void testAddFeatureSmlFormatAndGet() throws Exception
    {
        var json = createFeatureSmlJson(1);
        var httpResp = sendPostRequest(collectionName, json, ResourceFormat.SML_JSON.getMimeType());
        var url = getLocation(httpResp);
        
        // get summary
        var jsonResp = (JsonObject)sendGetRequestAndParseJson(url);
        checkId(url, jsonResp);
        assertEquals(json.get("label").getAsString(), jsonResp.getAsJsonObject("properties").get("name").getAsString());
        assertEquals(json.get("description").getAsString(), jsonResp.getAsJsonObject("properties").get("description").getAsString());
        
        // get details
        jsonResp = (JsonObject)sendGetRequestAndParseJson(url + "?f=application/sml%2Bjson");
        checkId(url, jsonResp);
        jsonResp.remove("id"); // remove auto-assigned id before compare
        jsonResp.remove("validTime");
        assertEquals(json, jsonResp);
    }
    
    
    protected abstract JsonObject createFeatureSmlJson(int procNum) throws Exception;
    
}
