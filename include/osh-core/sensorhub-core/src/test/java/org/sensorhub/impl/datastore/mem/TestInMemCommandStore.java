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
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.junit.Test;
import org.sensorhub.api.command.ICommandData;
import org.sensorhub.api.command.ICommandStatus;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.command.CommandFilter;
import org.sensorhub.api.datastore.command.CommandStatusFilter;
import org.sensorhub.impl.datastore.AbstractTestCommandStore;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;


public class TestInMemCommandStore extends AbstractTestCommandStore<InMemoryCommandStore>
{
    
    protected InMemoryCommandStore initStore() throws Exception
    {
        return new InMemoryCommandStore(DATABASE_NUM);
    }


    @Test
    @Override
    public void testGetNumRecordsOneDataStream() throws Exception
    {
        // add one command stream
        var csKey = addSimpleCommandStream(bigId(10), "out1");
        
        // add multiple commands
        addCommands(csKey.getInternalID(), Instant.parse("2000-01-01T00:00:00Z"), 100);
        
        // check that we have only one in store
        assertEquals(1, cmdStore.getNumRecords());
    }


    @Test
    @Override
    public void testGetNumRecordsTwoDataStreams() throws Exception
    {
        // add 2 command streams
        var cs1 = addSimpleCommandStream(bigId(1), "out1");
        var cs2 = addSimpleCommandStream(bigId(2), "out1");

        // add multiple commands to both streams
        addCommands(cs1.getInternalID(), Instant.parse("2000-06-21T14:36:12Z"), 100);
        addCommands(cs2.getInternalID(), Instant.parse("1970-01-01T00:00:00Z"), 50);
        
        // check that we have only 2 records, one in each stream
        assertEquals(2, cmdStore.getNumRecords());
        
        assertEquals(1, cmdStore.countMatchingEntries(new CommandFilter.Builder()
            .withCommandStreams(cs1.getInternalID())
            .build()));
        
        assertEquals(1, cmdStore.countMatchingEntries(new CommandFilter.Builder()
            .withCommandStreams(cs2.getInternalID())
            .build()));
    }
    
    
    private Map<BigId, ICommandData> getOnlyLatestCommands()
    {
        Map<BigId, Entry<BigId, ICommandData>> latestPerStream = new LinkedHashMap<>();
        for (var entry: allCommands.entrySet())
        {
            var cmd = entry.getValue();
            var savedEntry = latestPerStream.get(cmd.getCommandStreamID());
            
            if (savedEntry == null || savedEntry.getValue().getIssueTime().isBefore(cmd.getIssueTime()))
                latestPerStream.put(cmd.getCommandStreamID(), entry);
        }
        
        var onlyLatests = ImmutableMap.copyOf(latestPerStream.values());
        return onlyLatests;
    }
    
    
    private Map<BigId, ICommandData> keepOnlyLatestCommands(Map<BigId, ICommandData> expectedResults)
    {
        var onlyLatests = getOnlyLatestCommands();
        return Maps.filterKeys(expectedResults, k -> onlyLatests.containsKey(k));
    }
    
    
    @Override
    protected void checkSelectedEntries(Stream<Entry<BigId, ICommandData>> resultStream, Map<BigId, ICommandData> expectedResults, CommandFilter filter)
    {
        // keep only latest commands in expected results
        expectedResults = keepOnlyLatestCommands(expectedResults);
        super.checkSelectedEntries(resultStream, expectedResults, filter);
    }
    
    
    @Override
    protected void checkSelectedEntries(Stream<Entry<BigId, ICommandStatus>> resultStream, Map<BigId, ICommandStatus> expectedResults, CommandStatusFilter filter)
    {
        // keep only status of latest commands in expected results
        var onlyLatests = getOnlyLatestCommands();
        expectedResults = Maps.filterValues(expectedResults, s -> onlyLatests.containsKey(s.getCommandID()));
        
        super.checkSelectedEntries(resultStream, expectedResults, filter);
    }
    
    
    @Override
    protected void checkMapKeySet(Set<BigId> keySet)
    {
        var saveAllCommands = allCommands;
        allCommands = keepOnlyLatestCommands(allCommands);
        super.checkMapKeySet(keySet);
        allCommands = saveAllCommands; // revert to original map
    }
    
    
    @Override
    protected void checkGetCommands(int expectedNumObs) throws Exception
    {
        var saveAllCommands = allCommands;
        var latestCommands = allCommands = keepOnlyLatestCommands(allCommands);
        expectedNumObs = allCommands.size();
        super.checkGetCommands(expectedNumObs);
        if (allCommands == latestCommands)
        allCommands = saveAllCommands; // revert to original map
    }
    
    
    @Override
    protected void checkRemoveAllKeys() throws Exception
    {
        var saveAllCommands = allCommands;
        allCommands = keepOnlyLatestCommands(allCommands);
        super.checkRemoveAllKeys();
        allCommands = saveAllCommands; // revert to original map
        allCommands.clear();
    }


    @Test
    @Override
    public void testGetDatastoreName() throws Exception
    {
        assertEquals(InMemoryCommandStore.class.getSimpleName(), cmdStore.getDatastoreName());
    }


    @Override
    protected void forceReadBackFromStorage() throws Exception
    {        
    }

}
