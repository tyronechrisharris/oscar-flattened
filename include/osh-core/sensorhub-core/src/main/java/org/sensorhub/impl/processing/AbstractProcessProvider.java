/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2017 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.processing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.sensorhub.api.processing.IProcessProvider;
import org.sensorhub.impl.module.JarModuleProvider;
import org.vast.process.ProcessInfo;


public abstract class AbstractProcessProvider extends JarModuleProvider implements IProcessProvider
{
    private final Map<String, ProcessInfo> map = new HashMap<>();
    
    
    protected void addImpl(ProcessInfo info)
    {
        map.put(info.getUri(), info);
    }
    
    
    @Override
    public Map<String, ProcessInfo> getProcessMap()
    {
        return Collections.unmodifiableMap(map);
    }
}
