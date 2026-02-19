/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.ui.filter;

import java.util.LinkedHashMap;
import java.util.Map;
import org.sensorhub.api.datastore.command.CommandFilter;
import org.sensorhub.api.datastore.command.CommandStreamFilter;
import org.sensorhub.api.datastore.obs.DataStreamFilter;
import org.sensorhub.api.datastore.obs.ObsFilter;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.ui.GenericConfigForm;
import org.sensorhub.ui.data.BaseProperty;


@SuppressWarnings({"serial"})
public class DatabaseViewConfigForm extends GenericConfigForm
{
    
    @Override
    public Map<String, Class<?>> getPossibleTypes(String propId, BaseProperty<?> prop)
    {
        if (propId.endsWith("includeFilter"))
        {
            Map<String, Class<?>> classList = new LinkedHashMap<>();
            classList.put("System Filter", SystemFilter.Builder.class);
            classList.put("Datastream Filter", DataStreamFilter.Builder.class);
            classList.put("Command Stream Filter", CommandStreamFilter.Builder.class);
            classList.put("Obs Filter", ObsFilter.Builder.class);
            classList.put("Command Filter", CommandFilter.Builder.class);
            return classList;
        }
        
        return super.getPossibleTypes(propId, prop);
    }
}
