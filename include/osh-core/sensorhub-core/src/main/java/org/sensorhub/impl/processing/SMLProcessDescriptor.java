/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
The Initial Developer is Botts Innovative Research Inc. Portions created by the Initial
Developer are Copyright (C) 2014 the Initial Developer. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.processing;

import org.sensorhub.api.module.IModule;
import org.sensorhub.api.module.ModuleConfig;


public class SMLProcessDescriptor extends AbstractProcessProvider
{
    
    public SMLProcessDescriptor()
    {
        addImpl(StreamDataSource.INFO);
        addImpl(DataStreamSource.INFO);
        addImpl(CommandStreamSink.INFO);
    }
    
    
    @Override
    public String getModuleName()
    {
        return "SensorML Stream Process";
    }
    

    @Override
    public String getModuleDescription()
    {
        return "Processing module configured using a SensorML description";
    }
    

    @Override
    public Class<? extends IModule<?>> getModuleClass()
    {
        return SMLProcessImpl.class;
    }
    

    @Override
    public Class<? extends ModuleConfig> getModuleConfigClass()
    {
        return SMLProcessConfig.class;
    }
}
