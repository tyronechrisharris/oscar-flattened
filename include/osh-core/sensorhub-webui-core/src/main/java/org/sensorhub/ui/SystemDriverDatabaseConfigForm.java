/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.ui;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.sensorhub.api.database.IDatabaseModuleDescriptor;
import org.sensorhub.api.module.IModuleProvider;
import org.sensorhub.impl.database.system.MaxAgeAutoPurgeConfig;
import org.sensorhub.impl.database.system.SystemDriverDatabaseConfig;
import org.sensorhub.ui.api.IModuleConfigForm;
import org.sensorhub.ui.data.BaseProperty;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.ui.Field;


@SuppressWarnings("serial")
public class SystemDriverDatabaseConfigForm extends GenericConfigForm implements IModuleConfigForm
{
    public static final String PROP_DB_CONFIG = "dbConfig";
    public static final String PROP_DATABASE_ID = "databaseID";
    public static final String PROP_AUTOPURGE = "autoPurgeConfig";
    
    
    @Override
    protected Field<?> buildAndBindField(String label, String propId, Property<?> prop)
    {
        Field<Object> field = (Field<Object>)super.buildAndBindField(label, propId, prop);
        
        if (propId.equals(PROP_DB_CONFIG + PROP_SEP + PROP_NAME))
            return null;
        if (propId.equals(PROP_DB_CONFIG + PROP_SEP + PROP_DATABASE_ID))
            return null;
        
        return field;
    }
    
    
    @Override
    public Map<String, Class<?>> getPossibleTypes(String propId, BaseProperty<?> prop)
    {
        if (propId.equals(PROP_AUTOPURGE))
        {
            Map<String, Class<?>> classList = new LinkedHashMap<String, Class<?>>();
            classList.put("Auto Purge by Maximum Age", MaxAgeAutoPurgeConfig.class);
            return classList;
        }
        
        return super.getPossibleTypes(propId, prop);
    }


    @Override
    public Collection<IModuleProvider> getPossibleModuleTypes(String propId, Class<?> configType)
    {
        Collection<IModuleProvider> storageModules = super.getPossibleModuleTypes(propId, configType);
        
        // remove all stream and read-only storage implementation from list
        Iterator<IModuleProvider> it = storageModules.iterator();
        while (it.hasNext())
        {
            IModuleProvider provider = it.next();
            if (provider.getModuleConfigClass().isAssignableFrom(SystemDriverDatabaseConfig.class))
                it.remove();
            else if (provider instanceof IDatabaseModuleDescriptor && ((IDatabaseModuleDescriptor)provider).isReadOnly())
                it.remove();
        }
        
        return storageModules;
    }

}
