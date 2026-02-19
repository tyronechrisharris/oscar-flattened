/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore;

import static org.junit.Assert.assertEquals;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.sensorhub.api.datastore.IQueryFilter;
import org.sensorhub.api.datastore.feature.FeatureFilter;
import org.sensorhub.api.datastore.obs.DataStreamFilter;
import org.sensorhub.api.datastore.obs.ObsFilter;
import org.sensorhub.api.datastore.system.SystemFilter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;


public class TestGsonFilterTypeAdapters
{
    Gson gson;
    
    
    @Before
    public void initGson()
    {
        this.gson = new GsonBuilder()
            .registerTypeAdapterFactory(new DataStoreFiltersTypeAdapterFactory())
            .setFieldNamingStrategy(new DataStoreFiltersTypeAdapterFactory.FieldNamingStrategy())
            .serializeSpecialFloatingPointValues()
            .setPrettyPrinting()
            .create();
    }
    
    
    protected <T extends IQueryFilter> void testReadWrite(URL resource, Class<T> filterType) throws Exception
    {
        JsonElement refJson = gson.fromJson(new InputStreamReader(resource.openStream()), JsonElement.class);
        
        var sampleFileRootType = TypeToken.getParameterized(List.class, filterType);
        List<SystemFilter> filters = gson.fromJson(
            new InputStreamReader(resource.openStream()),
            sampleFileRootType.getType());
        
        // write to json tree and check equality
        var json1 = gson.toJsonTree(filters);
        System.out.println(json1);
        assertEquals(json1, refJson);
        
        // read back, write to json tree and check again
        filters = gson.fromJson(json1, sampleFileRootType.getType());
        var json2 = gson.toJsonTree(filters);
        assertEquals(json1, json2);
    }
    
    
    @Test
    public void testReadWriteReadSystemFilters() throws Exception
    {
        var resource = TestGsonFilterTypeAdapters.class.getResource("system_filters.json");
        testReadWrite(resource, SystemFilter.class);
    }
    
    
    @Test
    public void testReadWriteReadDatastreamFilters() throws Exception
    {
        var resource = TestGsonFilterTypeAdapters.class.getResource("datastream_filters.json");
        testReadWrite(resource, DataStreamFilter.class);
    }
    
    
    @Test
    public void testReadWriteReadObsFilters() throws Exception
    {
        var resource = TestGsonFilterTypeAdapters.class.getResource("obs_filters.json");
        testReadWrite(resource, ObsFilter.class);
    }
    
    
    @Test
    public void testReadWriteReadFeatureFilters() throws Exception
    {
        var resource = TestGsonFilterTypeAdapters.class.getResource("feature_filters.json");
        testReadWrite(resource, FeatureFilter.class);
    }

}
