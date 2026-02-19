/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.processing;

import org.sensorhub.api.data.IDataProducerModule;


/**
 * <p>
 * Interface for all event stream processors.<br/>
 * This type of process is started in a persistent manner and listens to
 * incoming events. The algorithm is triggered repeatedly each time enough
 * input data events have been received or some time has ellapsed.<br/>
 * Each process can listen to multiple event streams and produce a different
 * type of result (and corresponding event) on each of its output.
 * </p>
 *
 * @author Alex Robin
 * @param <ConfigType> Type of configuration class
 * @since Feb 20, 2015
 */
public interface IProcessModule<ConfigType extends ProcessConfig> extends IDataProducerModule<ConfigType>, IDataProcess
{   
    
    
}
