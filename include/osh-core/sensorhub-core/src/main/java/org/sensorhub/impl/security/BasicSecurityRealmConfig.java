/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2016 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.sensorhub.api.config.DisplayInfo;
import org.sensorhub.api.config.DisplayInfo.FieldType;
import org.sensorhub.api.config.DisplayInfo.IdField;
import org.sensorhub.api.security.IPermissionPath;
import org.sensorhub.api.security.IUserInfo;
import org.sensorhub.api.security.IUserPermissions;
import org.sensorhub.api.security.IUserRole;
import org.sensorhub.api.security.SecurityModuleConfig;
import org.sensorhub.impl.module.ModuleRegistry;
import com.google.gson.annotations.SerializedName;


public class BasicSecurityRealmConfig extends SecurityModuleConfig
{
    @DisplayInfo(desc="List of users allowed access to this system")
    public List<UserConfig> users = new ArrayList<>();
    
    @DisplayInfo(desc="List of security roles")
    public List<RoleConfig> roles = new ArrayList<>();
    
    
    public static class PermissionsConfig implements IUserPermissions
    {
        public List<String> allow = new ArrayList<>();
        public List<String> deny = new ArrayList<>();
        transient Collection<IPermissionPath> allowList = new ArrayList<>();
        transient Collection<IPermissionPath> denyList = new ArrayList<>();
        
        @Override
        public Collection<IPermissionPath> getAllowList()
        {
            return Collections.unmodifiableCollection(allowList);
        }
        
        @Override
        public Collection<IPermissionPath> getDenyList()
        {
            return Collections.unmodifiableCollection(denyList);
        }
        
        public void refreshPermissionLists(ModuleRegistry moduleRegistry)
        {
            var newAllowList = new ArrayList<IPermissionPath>();
            for (String path: allow)
                newAllowList.add(PermissionFactory.newPermissionSetting(moduleRegistry, path));
            
            var newDenyList = new ArrayList<IPermissionPath>();
            for (String path: deny)
                newDenyList.add(PermissionFactory.newPermissionSetting(moduleRegistry, path));
            
            this.allowList = newAllowList;
            this.denyList = newDenyList;
        }
    }
    
    
    @IdField("userID")
    public static class UserConfig extends PermissionsConfig implements IUserInfo
    {
        @SerializedName("id")
        @DisplayInfo(label="User ID")
        public String userID;
        public String name;
        @FieldType(FieldType.Type.PASSWORD)
        public String password;
        //public String certificate;
        public List<String> roles = new ArrayList<>();
        
        @Override
        public String getId()
        {
            return userID;
        }
        
        @Override
        public String getName()
        {
            return name;
        }
        
        @Override
        public String getDescription()
        {
            return null;
        }
        
        @Override
        public String getPassword()
        {
            return password;
        }
        
        @Override
        public Collection<String> getRoles()
        {
            return roles;
        }
        
        @Override
        public Map<String, Object> getAttributes()
        {
            return Collections.emptyMap();
        }
    }
    
    
    @IdField("roleID")
    public static class RoleConfig extends PermissionsConfig implements IUserRole
    {
        @SerializedName("id")
        @DisplayInfo(label="Role ID")
        public String roleID;
        public String name;
        public String description;

        @Override
        public String getId()
        {
            return roleID;
        }
        
        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public String getDescription()
        {
            return description;
        }
    }
    
    
    public BasicSecurityRealmConfig()
    {
        this.moduleClass = BasicSecurityRealm.class.getCanonicalName();
    }
}
