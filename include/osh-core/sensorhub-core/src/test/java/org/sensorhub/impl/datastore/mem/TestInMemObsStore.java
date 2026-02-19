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

import static org.junit.Assert.assertEquals;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.junit.Test;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.data.IObsData;
import org.sensorhub.api.datastore.obs.ObsFilter;
import org.sensorhub.impl.datastore.AbstractTestObsStore;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;


public class TestInMemObsStore extends AbstractTestObsStore<InMemoryObsStore>
{
    
    protected InMemoryObsStore initStore() throws Exception
    {
        return new InMemoryObsStore(DATABASE_NUM);
    }


    @Test
    @Override
    public void testGetNumRecordsOneDataStream() throws Exception
    {
        // add one datastream
        var dsID = addSimpleDataStream(bigId(10), "out1");
        
        // add multiple obs
        addSimpleObsWithoutResultTime(dsID, BigId.NONE, Instant.parse("2000-01-01T00:00:00Z"), 100);
        
        // check that we have only one record in store
        assertEquals(1, obsStore.getNumRecords());
    }


    @Test
    @Override
    public void testGetNumRecordsTwoDataStreams() throws Exception
    {
        // add 2 datastreams
        var ds1 = addSimpleDataStream(bigId(1), "out1");
        var ds2 = addSimpleDataStream(bigId(2), "out1");

        // add multiple obs to both streams
        addSimpleObsWithoutResultTime(ds1, BigId.NONE, Instant.parse("2000-06-21T14:36:12Z"), 100);
        addSimpleObsWithoutResultTime(ds2, BigId.NONE, Instant.parse("1970-01-01T00:00:00Z"), 50);
        
        // check that we have only 2 records, one in each stream
        assertEquals(2, obsStore.getNumRecords());
        
        assertEquals(1, obsStore.countMatchingEntries(new ObsFilter.Builder()
            .withDataStreams(ds1)
            .build()));
        
        assertEquals(1, obsStore.countMatchingEntries(new ObsFilter.Builder()
            .withDataStreams(ds2)
            .build()));
    }
    
    
    private Map<BigId, IObsData> keepOnlyLatestObs(Map<BigId, IObsData> expectedResults)
    {
        Map<Integer, Entry<BigId, IObsData>> latestPerStream = new LinkedHashMap<>();
        for (var entry: allObs.entrySet())
        {
            var obs = entry.getValue();
            var dsFoiKey = Objects.hash(obs.getDataStreamID(), obs.getFoiID());
            var savedEntry = latestPerStream.get(dsFoiKey);
            
            if (savedEntry == null || savedEntry.getValue().getResultTime().isBefore(obs.getResultTime()))
                latestPerStream.put(dsFoiKey, entry);
        }
        
        var onlyLatests = ImmutableMap.copyOf(latestPerStream.values());
        return Maps.filterKeys(expectedResults, k -> onlyLatests.containsKey(k));
    }
    
    
    protected void checkSelectedEntries(Stream<Entry<BigId, IObsData>> resultStream, Map<BigId, IObsData> expectedResults, ObsFilter filter)
    {
        // keep only latest command in expected results
        expectedResults = keepOnlyLatestObs(expectedResults);
        super.checkSelectedEntries(resultStream, expectedResults, filter);
    }
    
    
    @Override
    protected void checkMapKeySet(Set<BigId> keySet)
    {
        var saveAllCommands = allObs;
        allObs = keepOnlyLatestObs(allObs);
        super.checkMapKeySet(keySet);
        allObs = saveAllCommands; // revert to original map
    }
    
    
    @Override
    protected void checkGetObs(int expectedNumObs) throws Exception
    {
        var saveAllCommands = allObs;
        var latestCommands = allObs = keepOnlyLatestObs(allObs);
        expectedNumObs = allObs.size();
        super.checkGetObs(expectedNumObs);
        if (allObs == latestCommands)
        allObs = saveAllCommands; // revert to original map
    }
    
    
    @Override
    protected void checkRemoveAllKeys() throws Exception
    {
        var saveAllCommands = allObs;
        allObs = keepOnlyLatestObs(allObs);
        super.checkRemoveAllKeys();
        allObs = saveAllCommands; // revert to original map
        allObs.clear();
    }


    @Test
    @Override
    public void testGetDatastoreName() throws Exception
    {
        assertEquals(InMemoryObsStore.class.getSimpleName(), obsStore.getDatastoreName());
    }
    
    
    protected void forceReadBackFromStorage()
    {
    }

}
