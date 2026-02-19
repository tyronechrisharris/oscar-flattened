/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.mem;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import org.junit.Test;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.impl.datastore.AbstractTestFeatureStore;


public class TestInMemFeatureStore extends AbstractTestFeatureStore<InMemoryFeatureStore>
{
    
    protected InMemoryFeatureStore initStore() throws Exception
    {
        return new InMemoryFeatureStore(DATABASE_NUM, new InMemoryIdProvider<>(1));
    }
    
    
    protected void forceReadBackFromStorage()
    {
    }
    
    
    protected void removeNonCurrentFeatures()
    {
        // remove all features that are not valid now
        var now = Instant.now();
        var keysToRemove = new ArrayList<FeatureKey>();
        FeatureKey prevKey = null;
        
        var it = allFeatures.keySet().iterator();
        while (it.hasNext())
        {
            var k = it.next();
            
            if (k.getValidStartTime().compareTo(now) > 0) {
                keysToRemove.add(k);
            }
            else if (prevKey != null && prevKey.getInternalID().equals(k.getInternalID())) {
                if (k.getValidStartTime().compareTo(now) <= 0)
                    keysToRemove.add(prevKey);
            }
            
            prevKey = k;
        }
        
        for (var k: keysToRemove)
            allFeatures.remove(k);
    }
    
    
    protected void addNonGeoFeatures(BigId parentID, int startIndex, int numFeatures) throws Exception
    {
        super.addNonGeoFeatures(parentID, startIndex, numFeatures);
        removeNonCurrentFeatures();
    }
    
    
    protected void addGeoFeaturesPoint2D(BigId parentID, int startIndex, int numFeatures) throws Exception
    {
        super.addGeoFeaturesPoint2D(parentID, startIndex, numFeatures);
        removeNonCurrentFeatures();
    }
    
    
    protected void addSamplingPoints2D(BigId parentID, int startIndex, int numFeatures) throws Exception
    {
        super.addSamplingPoints2D(parentID, startIndex, numFeatures);
        removeNonCurrentFeatures();
    }
    
    
    protected void addTemporalFeatures(BigId parentID, int startIndex, int numFeatures, OffsetDateTime startTime, boolean endNow) throws Exception
    {
        super.addTemporalFeatures(parentID, startIndex, numFeatures, startTime, endNow);
        removeNonCurrentFeatures();
    }
    
    
    protected void addTemporalGeoFeatures(BigId parentID, int startIndex, int numFeatures) throws Exception
    {
        super.addTemporalGeoFeatures(parentID, startIndex, numFeatures);
        removeNonCurrentFeatures();
    }
    
    
    @Test
    @Override
    public void testGetDatastoreName() throws Exception
    {
    }
}
