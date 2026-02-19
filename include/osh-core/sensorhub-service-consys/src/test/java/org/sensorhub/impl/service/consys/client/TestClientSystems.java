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
import static org.junit.Assert.assertFalse;
import java.util.ArrayList;
import java.util.Set;
import org.junit.Test;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.impl.system.wrapper.SystemWrapper;
import org.vast.sensorML.SMLHelper;
import com.google.common.base.Strings;


public class TestClientSystems extends TestClientBase
{
    
    public TestClientSystems() {}
    
    
    TestClientSystems(String apiRootUrl)
    {
        this.apiRootUrl = apiRootUrl;
    }
    
    
    @Test
    public void testAddSystem() throws Exception
    {
        addSystem(1, true);
        addSystem(10, true);
    }
    
    
    @Test
    public void testAddSystemBatch() throws Exception
    {
        addSystemBatch(5, true);
        addSystemBatch(100, true);
    }
    
    
    @Test
    public void testAddSystemAndGetByUid() throws Exception
    {
        var sysNum = 15;
        addSystem(sysNum, true);
        
        var sysUid = getSystemUid(sysNum);
        var sys = ConSysApiClient
            .newBuilder(apiRootUrl)
            .build()
            .getSystemByUid(sysUid, ResourceFormat.JSON).get();
        
        assertEquals(sysUid, sys.getUniqueIdentifier());
    }
    
    
    protected String addSystem(int num, boolean checkHead) throws Exception
    {
        // insert one system
        var sys = new SMLHelper().createPhysicalSystem()
            .uniqueID(getSystemUid(num))
            .name("Test System #" + num)
            .description("Test system #"+ num + " created using Java API client")
            .build();
        
        var f = ConSysApiClient
            .newBuilder(apiRootUrl)
            .build()
            .addSystem(new SystemWrapper(sys));
        
        var id = f.get();
        assertFalse(Strings.isNullOrEmpty(id));
        System.out.println("Ingested system " + num);
        
        if (checkHead)
            checkSystemIdExists(id);
        
        return id;
    }
    
    
    protected Set<String> addSystemBatch(int batchSize, boolean checkHead) throws Exception
    {
        // insert multiple systems
        var sysList = new ArrayList<ISystemWithDesc>();
        for (int i = 0; i < batchSize; i++)
        {
            var num = i+1000;
            var sys = new SMLHelper().createPhysicalSystem()
                .uniqueID(getSystemUid(num))
                .name("Test System #" + num)
                .description("Test system #" + num + " created using Java API client")
                .build();
            sysList.add(new SystemWrapper(sys));
        }
        
        var f = ConSysApiClient
            .newBuilder(apiRootUrl)
            .build()
            .addSystems(sysList);
        
        var idList = f.get();
        System.out.println("Ingested " + batchSize + " systems");
        
        if (checkHead)
        {
            for (var id: idList)
                checkSystemIdExists(id);
        }
        
        return idList;
    }
    
    
    protected String getSystemUid(int num)
    {
        return "urn:osh:test:system:" + num;
    }
    
    
    protected void checkSystemIdExists(String id) throws Exception
    {
        checkUriExists(apiRootUrl + "/" + SYSTEM_COLLECTION + "/" + id);
    }
}
