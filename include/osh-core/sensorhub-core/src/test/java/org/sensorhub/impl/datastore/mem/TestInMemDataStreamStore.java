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

import org.sensorhub.impl.datastore.AbstractTestDataStreamStore;


public class TestInMemDataStreamStore extends AbstractTestDataStreamStore<InMemoryDataStreamStore>
{
       
    
    protected InMemoryDataStreamStore initStore() throws Exception
    {
        needValidTimeAdjustment = false;
        return (InMemoryDataStreamStore)new InMemoryObsStore(DATABASE_NUM).getDataStreams();
    }
    
    
    protected void forceReadBackFromStorage()
    {
    }
    
    
    protected void testAddAndSelectLatestValidTime_ExpectedResults()
    {
        addToExpectedResults(2, 4, 7);
    }
    
    
    protected void testAddAndSelectByTimeRange_ExpectedResults(int testCaseIdx)
    {
        switch (testCaseIdx)
        {
            case 1: addToExpectedResults(1, 3, 5, 7); break;
            case 2: addToExpectedResults(1, 7); break;
            case 3: addToExpectedResults(7); break;
        }        
    }
    
    
    @Override
    protected void testAddAndSelectByOutputName_ExpectedResults()
    {
        addToExpectedResults(1, 3, 5);
    }
    
    
    protected void testAddAndSelectBySystemID_ExpectedResults()
    {
        addToExpectedResults(3, 4, 5);
    }
    
    
    protected void testAddAndSelectByKeywords_ExpectedResults(int testCaseIdx)
    {
        switch (testCaseIdx)
        {
            case 1: addToExpectedResults(4, 5); break;
            case 2: addToExpectedResults(1, 4, 5); break;
            case 3: addToExpectedResults(2, 4, 5); break;
            case 4: addToExpectedResults(4); break;
        }        
    }
}
