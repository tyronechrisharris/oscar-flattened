/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2023 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.client;

import static org.junit.Assert.assertEquals;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import org.eclipse.jetty.http.HttpMethod;
import org.sensorhub.impl.service.consys.AbstractTestApiBase;


public class TestClientBase extends AbstractTestApiBase
{


    
    
    protected void checkUriExists(String resourceUri) throws Exception
    {
        System.out.println("Checking resource at " + resourceUri);
        
        var req = HttpRequest.newBuilder()
            .uri(new URI(resourceUri))
            .method(HttpMethod.HEAD.asString(), BodyPublishers.noBody())
            .build();
        
        assertEquals(200, HttpClient.newHttpClient()
            .send(req, BodyHandlers.discarding())
            .statusCode()); 
    }
    
}
