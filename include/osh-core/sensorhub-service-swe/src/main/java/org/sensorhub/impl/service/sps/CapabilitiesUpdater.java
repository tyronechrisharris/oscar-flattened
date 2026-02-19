/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sps;

import java.util.LinkedHashMap;
import java.util.Map;
import org.sensorhub.api.datastore.command.CommandStreamFilter;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.api.system.ISystemWithDesc;
import org.vast.ows.sps.SPSOfferingCapabilities;
import org.vast.ows.swe.SWESOfferingCapabilities;
import com.google.common.base.Strings;


public class CapabilitiesUpdater
{
    public static final String OFFERING_NAME_PLACEHOLDER = "{%offering_name%}";
    public static final String PROC_UID_PLACEHOLDER = "{%procedure_uid%}";
    public static final String PROC_NAME_PLACEHOLDER = "{%procedure_name%}";
    public static final String PROC_DESC_PLACEHOLDER = "{%procedure_description%}";
    
    
    public void updateOfferings(final SPSServlet servlet)
    {
        Map<String, SPSOfferingCapabilities> offerings = new LinkedHashMap<>();
        
        var db = servlet.getReadDatabase();
        var providerConfigs = servlet.connectorConfigs;
        
        db.getSystemDescStore().selectEntries(new SystemFilter.Builder().build())
            .forEach(entry -> {
                var proc = entry.getValue();
                
                String sysUID = proc.getUniqueIdentifier();
                var customConfig = providerConfigs.get(sysUID);
                
                var numCommands = db.getCommandStreamStore().countMatchingEntries(new CommandStreamFilter.Builder()
                    .withSystems().withUniqueIDs(sysUID).done()
                    .build());
                if (numCommands == 0)
                    return;
                
                // retrieve or create new offering
                SPSOfferingCapabilities offering = offerings.get(sysUID);
                if (offering == null)
                {
                    offering = new SPSOfferingCapabilities();
                    offering.setIdentifier(sysUID); // use procedure UID as offering ID
                    offering.getProcedures().add(sysUID);
                    
                    // use name and description from custom config if set
                    // otherwise default to name and description of procedure 
                    offering.setTitle(customConfig != null && !Strings.isNullOrEmpty(customConfig.name) ?
                        replaceVariables(customConfig.name, proc, customConfig) : proc.getName());
                    offering.setDescription(customConfig != null && !Strings.isNullOrEmpty(customConfig.description) ?
                        replaceVariables(customConfig.description, proc, customConfig) : proc.getDescription());
                    
                    // add supported formats
                    offering.getProcedureFormats().add(SWESOfferingCapabilities.FORMAT_SML2);
                    //offering.getProcedureFormats().add(SWESOfferingCapabilities.FORMAT_SML2_JSON);
                    
                    offerings.put(sysUID, offering);
                }             
            });
        
        servlet.capabilities.getLayers().clear();
        servlet.capabilities.getLayers().addAll(offerings.values());
    }
    
    
    protected String replaceVariables(String textField, ISystemWithDesc proc, SPSConnectorConfig config)
    {
        textField = textField.replace(PROC_UID_PLACEHOLDER, proc.getUniqueIdentifier());
        
        if (config.name != null)
            textField = textField.replace(OFFERING_NAME_PLACEHOLDER, config.name);
        else if (proc.getName() != null)
            textField = textField.replace(OFFERING_NAME_PLACEHOLDER, proc.getName());
        
        if (proc.getName() != null)
            textField = textField.replace(PROC_NAME_PLACEHOLDER, proc.getName());
        
        if (proc.getDescription() != null)
            textField = textField.replace(PROC_DESC_PLACEHOLDER, proc.getDescription());
        
        return textField;
    }    
}
