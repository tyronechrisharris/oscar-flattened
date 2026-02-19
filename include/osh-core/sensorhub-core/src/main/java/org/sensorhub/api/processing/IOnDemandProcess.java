/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.processing;

import java.util.Arrays;
import java.util.Collection;


public interface IOnDemandProcess extends IDataProcess
{
    public static final String ON_DEMAND_PROCESS_DEF = "http://sensorml.com/ont/swe/system/OnDemandProcess";
    
    /**
     * Executes the process using the current input and parameter data
     * for all available features of interest.
     */
    public void execute();
    
    
    /**
     * Executes the process using the current input and parameter data,
     * only for the specified features of interest (i.e. the process
     * will generate outputs only for the specified FOIs)
     * @param foiUIDs Collections of feature of interest UID (must not
     * be null or empty)
     */
    public void execute(Collection<String> foiUIDs);
    
    
    /**
     * Executes the process using the current input and parameter data,
     * only for the specified features of interest (i.e. the process
     * will generate outputs only for the specified FOIs)
     * @param foiUIDs One or more feature of interest UIDs
     */
    public default void execute(String... foiUIDs)
    {
        execute(Arrays.asList(foiUIDs));
    }

}
