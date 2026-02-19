/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2025 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.module;


/**
 * <p>
 * Generic interface for all submodules.
 * </p><p>
 * Submodules are reusable components such as comm providers, message queues,
 * data loggers, etc. that are managed by a parent module.
 * </p>
 *
 * @author Alex Robin
 * @since Feb 28, 2025
 */
public interface ISubModule<T extends SubModuleConfig> extends IModuleBase<T>
{
    
    /**
     * Sets the submodule parent module
     * @param parentModule
     */
    public void setParentModule(IModule<?> parentModule);
    
    
    /**
     * @return The submodule parent module
     */
    public IModule<?> getParentModule();
    
}
