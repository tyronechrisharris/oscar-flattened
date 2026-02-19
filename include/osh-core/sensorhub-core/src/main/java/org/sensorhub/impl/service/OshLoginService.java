/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2016 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service;

import java.security.Principal;
import javax.security.auth.Subject;
import javax.servlet.ServletRequest;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.security.Credential;
import org.sensorhub.api.security.ISecurityManager;
import org.sensorhub.api.security.IUserInfo;


public class OshLoginService implements LoginService
{
    final ISecurityManager securityManager;
    IdentityService identityService = new DefaultIdentityService();
    
    
    public static class UserPrincipal implements Principal
    {
        private final IUserInfo user;
        
        public UserPrincipal(IUserInfo user)
        {
            this.user = user;
        }
        
        @Override
        public String getName()
        {
            return user.getId();
        }
        
        @Override
        public String toString()
        {
            return getName();
        }
    }
    
    
    public static class RolePrincipal implements Principal
    {
        private final String _roleName;
        
        public RolePrincipal(String name)
        {
            _roleName=name;
        }
        
        @Override
        public String getName()
        {
            return _roleName;
        }
    }
    
    
    public OshLoginService(ISecurityManager securityManager)
    {
        this.securityManager = securityManager;
    }
    
    
    @Override
    public IdentityService getIdentityService()
    {
        return identityService;
    }


    @Override
    public String getName()
    {
        return "OpenSensorHub: Authentication Required";
    }


    @Override
    public UserIdentity login(String username, Object credentials, ServletRequest request)
    {
        if (username == null)
            return null;
        
        boolean isCert = false;
        if (username.startsWith("CN="))
        {
            username = username.substring(3, username.indexOf(','));
            isCert = true;
        }
        
        IUserInfo user = securityManager.getUserInfo(username);
        if (user == null)
            return null;
        
        UserIdentity identity = null;
        if (!isCert)
        {
            Credential storedCredential = Credential.getCredential(user.getPassword());
            if (storedCredential.check(credentials))
                identity = createUserIdentity(user, credentials);
        }
        else
            identity = createUserIdentity(user, credentials);
        
        return identity;
    }
    
    
    protected UserIdentity createUserIdentity(final IUserInfo user, Object credential)
    {
        Principal principal = new UserPrincipal(user);
        Subject subject = new Subject();
        subject.getPrincipals().add(principal);
        subject.getPrivateCredentials().add(credential);
        subject.setReadOnly();
        
        String[] roles = user.getRoles().toArray(new String[0]);
        UserIdentity identity = identityService.newUserIdentity(subject, principal, roles);
        return identity;
    }


    @Override
    public void logout(UserIdentity user)
    {
    }


    @Override
    public void setIdentityService(IdentityService identityService)
    {
        this.identityService = identityService;
    }


    @Override
    public boolean validate(UserIdentity user)
    {
        return true;
    }

}
