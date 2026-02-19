/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2016 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.module;

import java.security.AccessControlException;
import org.sensorhub.api.module.IModule;
import org.sensorhub.api.security.IPermission;
import org.sensorhub.api.security.ISecurityManager;
import org.sensorhub.api.security.IUserInfo;
import org.sensorhub.impl.security.ModulePermissions;
import org.sensorhub.impl.security.PermissionRequest;


public class ModuleSecurity
{    
    protected final IModule<?> module;
    public final ModulePermissions rootPerm;
    protected boolean enable = true;
    ThreadLocal<IUserInfo> currentUser = new ThreadLocal<>();
        
    
    public ModuleSecurity(IModule<?> module, String moduleTypeAlias, boolean enable)
    {
        this.module = module;
        this.rootPerm = new ModulePermissions(module, moduleTypeAlias);
        this.enable = enable;
        
        // register basic module permissions
        //module_init = new ItemPermission(rootPerm, "init", "Initialize Module");
        //module_start = new ItemPermission(rootPerm, "start", "Start Module");
        //module_stop = new ItemPermission(rootPerm, "stop", "Stop Module");
        //module_update = new ItemPermission(rootPerm, "update_config", "Update Module Configuration");
        
        //SensorHub.getInstance().getSecurityManager().registerModulePermissions(rootPerm);
    }
    
    
    protected boolean isAccessControlEnabled()
    {
        return module.getParentHub().getSecurityManager().isAccessControlEnabled();
    }
    
    
    /**
     * Checks if the current user has the given permission
     * @param perm
     * @return true if user is permitted, false otherwise
     */
    public boolean hasPermission(IPermission perm)
    {
        if (!enable || !isAccessControlEnabled())
            return true;
            
        // retrieve currently logged in user
        IUserInfo user = currentUser.get();
        if (user == null)
            throw new AccessControlException(perm.getErrorMessage() + ": No user specified");
        
        // request authorization
        return module.getParentHub().getSecurityManager().isAuthorized(user, new PermissionRequest(perm));
    }
    
    
    /**
     * Checks if the current user has the given permission and throws
     * exception if it doesn't
     * @param perm
     * @throws SecurityException
     */
    public void checkPermission(IPermission perm)
    {
        // request authorization
        if (!hasPermission(perm))
            throw new AccessControlException("User " + currentUser.get().getId() + ", " + perm.getErrorMessage());
    }
    
    
    /**
     * Sets the user attempting to use this module in the current thread
     * @param userID
     */
    public void setCurrentUser(String userID)
    {
        // do nothing if access control is not enabled (e.g. no realm was setup)
        if (!isAccessControlEnabled())
            return;
        
        // lookup user info 

        IUserInfo user = module.getParentHub().getSecurityManager().getUserInfo(userID);
        if (user != null)        
            currentUser.set(user);
    }
    
    
    /**
     * Clears the user associated to the current thread
     */
    public void clearCurrentUser()
    {
        currentUser.remove();
    }
    
    
    /**
     * @return The currently authenticated user
     */
    public IUserInfo getCurrentUser()
    {
        return currentUser.get();
    }
    
    
    /**
     * Unregister permission tree from security manager
     */
    public void unregister()
    {
        module.getParentHub().getSecurityManager().unregisterModulePermissions(rootPerm);
    }
    
    
    public ISecurityManager getSecurityManager()
    {
        return module.getParentHub().getSecurityManager();
    }
}
