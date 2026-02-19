/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.data;

import org.sensorhub.api.module.IModule;
import org.sensorhub.api.module.ModuleConfig;
import org.sensorhub.api.system.ISystemDriver;


/**
 * <p>
 * Kept for backward compatibility; Will be removed later
 * </p>
 * 
 * @param <ConfigType> Type of config class
 *
 * @author Alex Robin
 * @date Sep 25, 2020
 */
public interface IDataProducerModule<ConfigType extends ModuleConfig> extends IModule<ConfigType>, ISystemDriver, IDataProducer 
{

}
