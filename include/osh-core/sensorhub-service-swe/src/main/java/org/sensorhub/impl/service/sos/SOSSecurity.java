/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2016 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sos;

import org.sensorhub.api.security.IPermission;
import org.sensorhub.impl.module.ModuleSecurity;
import org.sensorhub.impl.security.ItemPermission;
import org.sensorhub.impl.security.ModulePermissions;


public class SOSSecurity extends ModuleSecurity
{    
    private static final String LABEL_CAPS = "Capabilities";
    private static final String LABEL_SENSOR = "Sensor Descriptions";
    private static final String LABEL_FOI = "Features of Interest";
    private static final String LABEL_OBS = "Observations";    
    
    final IPermission sos_read;
    final IPermission sos_read_caps;
    final IPermission sos_read_sensor;
    final IPermission sos_read_foi;
    final IPermission sos_read_obs;
    final IPermission sos_insert;
    final IPermission sos_insert_sensor;
    final IPermission sos_insert_obs;
    final IPermission sos_update;
    final IPermission sos_update_sensor;
    final IPermission sos_update_obs;
    final IPermission sos_delete;
    final IPermission sos_delete_sensor;
    final IPermission sos_delete_obs;
    
    
    public SOSSecurity(SOSService sos, boolean enable)
    {
        super(sos, "sos", enable);
        
        // register permission structure
        sos_read = new ItemPermission(rootPerm, "get");
        sos_read_caps = new ItemPermission(sos_read, "caps", LABEL_CAPS);
        sos_read_sensor = new ItemPermission(sos_read, "sensor", LABEL_SENSOR);
        sos_read_foi = new ItemPermission(sos_read, "foi", LABEL_FOI);
        sos_read_obs = new ItemPermission(sos_read, "obs", LABEL_OBS);
        
        sos_insert = new ItemPermission(rootPerm, "insert");
        sos_insert_sensor = new ItemPermission(sos_insert, "sensor", LABEL_SENSOR);
        sos_insert_obs = new ItemPermission(sos_insert, "obs", LABEL_OBS);
        
        sos_update = new ItemPermission(rootPerm, "update");
        sos_update_sensor = new ItemPermission(sos_update, "sensor", LABEL_SENSOR);
        sos_update_obs = new ItemPermission(sos_update, "obs", LABEL_OBS);
        
        sos_delete = new ItemPermission(rootPerm, "delete");
        sos_delete_sensor = new ItemPermission(sos_delete, "sensor", LABEL_SENSOR);
        sos_delete_obs = new ItemPermission(sos_delete, "obs", LABEL_OBS);
        
        // register wildcard permission tree usable for all SOS services
        // do it at this point so we don't include specific offering permissions
        ModulePermissions wildcardPerm = rootPerm.cloneAsTemplatePermission("SOS Services");
        sos.getParentHub().getSecurityManager().registerModulePermissions(wildcardPerm);
        
        // register permission tree
        sos.getParentHub().getSecurityManager().registerModulePermissions(rootPerm);
    }
    
    
    protected void addProcedurePermissions(String systemUID)
    {
        String permName = getProcedurePermissionName(systemUID);
        new ItemPermission(sos_read_caps, permName);
        new ItemPermission(sos_read_sensor, permName);
        new ItemPermission(sos_read_foi, permName);
        new ItemPermission(sos_read_obs, permName);
        new ItemPermission(sos_insert_obs, permName);
        new ItemPermission(sos_update_obs, permName);
        new ItemPermission(sos_delete_obs, permName);
        new ItemPermission(sos_update_sensor, permName);
        new ItemPermission(sos_delete_sensor, permName);
    }
    
    
    public void checkPermission(String systemUID, IPermission perm) throws SecurityException
    {
        /*String permName = getProcedurePermissionName(systemUID);
        IPermission offPerm = perm.getChildren().get(permName);
        Asserts.checkNotNull(offPerm, "Invalid permission check");
        checkPermission(offPerm);*/
    }
    
    
    protected String getProcedurePermissionName(String systemUID)
    {
        return "procedure[" + systemUID + "]";
    }
}
