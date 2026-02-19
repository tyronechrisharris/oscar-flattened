/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.h2;

import org.sensorhub.api.module.IModule;
import org.sensorhub.api.module.ModuleConfig;
import org.sensorhub.impl.module.JarModuleProvider;


/**
 * <p>
 * Descriptor class for {@link MVObsSystemDatabase} module.
 * </p>
 *
 * @author Alex Robin
 * @date Sep 23, 2019
 */
public class MVObsSystemDatabaseDescriptor extends JarModuleProvider
{
    @Override
    public String getModuleName()
    {
        return "H2 Historical Obs Database";
    }
    

    @Override
    public String getModuleDescription()
    {
        return "Historical observation database backed by an H2 MVStore";
    }
    

    @Override
    public Class<? extends IModule<?>> getModuleClass()
    {
        return MVObsSystemDatabase.class;
    }
    

    @Override
    public Class<? extends ModuleConfig> getModuleConfigClass()
    {
        return MVObsSystemDatabaseConfig.class;
    }
}
