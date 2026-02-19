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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Authentication.User;
import org.sensorhub.api.ISensorHub;
import org.sensorhub.api.security.IAuthorizer;
import org.sensorhub.api.security.IPermission;
import org.sensorhub.api.security.IPermissionPath;
import org.sensorhub.api.security.ISecurityManager;
import org.sensorhub.api.security.IUserInfo;
import org.sensorhub.api.security.IUserRegistry;
import org.sensorhub.impl.module.ModuleRegistry;
import org.sensorhub.utils.Async;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.util.Asserts;


public class SecurityManagerImpl implements ISecurityManager
{
    private static final long TIMEOUT = 10000L;
    private static final Logger log = LoggerFactory.getLogger(SecurityManagerImpl.class);

    private static final String REGISTERING_LOG_MSG = "Registering {} module {}";
    private static final String UNREGISTERING_LOG_MSG = "Unregistering {} module {}";
    private static final String ALREADY_REGISTERED_ERROR = " implementation is already registered";
    private static final String CANNOT_UNREGISTER_ERROR = " implementation can only unregister itself";
    
    final ModuleRegistry moduleRegistry;
    final AuthenticatorWrapper authenticator;
    final AtomicReference<IUserRegistry> userDB;
    final AtomicReference<IAuthorizer> authorizer;
    final Map<String, IPermission> modulePermissions;
    
    
    public SecurityManagerImpl(ISensorHub hub)
    {
        this.moduleRegistry = hub.getModuleRegistry();
        this.authenticator = new AuthenticatorWrapper();
        this.userDB = new AtomicReference<>();
        this.authorizer = new AtomicReference<>();
        this.modulePermissions = Collections.synchronizedMap(new LinkedHashMap<>());
    }
    
    
    @Override
    public boolean isAccessControlEnabled()
    {
        return userDB.get() != null && authorizer.get() != null;
    }
    
    
    @Override
    public void registerAuthenticator(Authenticator authenticator)
    {
        if (!this.authenticator.delegate.compareAndSet(null, authenticator))
            throw new IllegalStateException("An Authenticator" + ALREADY_REGISTERED_ERROR);
        log.info(REGISTERING_LOG_MSG, "Authenticator", authenticator.toString());
    }
    
    
    @Override
    public void unregisterAuthenticator(Authenticator authenticator)
    {
        if (!this.authenticator.delegate.compareAndSet(authenticator, null))
            throw new IllegalStateException("An Authenticator" + CANNOT_UNREGISTER_ERROR);
        log.info(UNREGISTERING_LOG_MSG, "Authenticator", authenticator.toString());
    }
    
    
    @Override
    public void registerUserRegistry(IUserRegistry userRegistry)
    {
        if (!this.userDB.compareAndSet(null, userRegistry))
            throw new IllegalStateException("A UserRegistry" + ALREADY_REGISTERED_ERROR);
        log.info(REGISTERING_LOG_MSG, "User Registry", userRegistry.toString());
    }
    
    
    @Override
    public void unregisterUserRegistry(IUserRegistry userRegistry)
    {
        if (!this.userDB.compareAndSet(userRegistry, null))
            throw new IllegalStateException("A UserRegistry" + CANNOT_UNREGISTER_ERROR);
        log.info(UNREGISTERING_LOG_MSG, "User Registry", userRegistry.toString());
    }
    
    
    @Override
    public void registerAuthorizer(IAuthorizer authorizer)
    {
        if (!this.authorizer.compareAndSet(null, authorizer))
            throw new IllegalStateException("An Authorizer" + ALREADY_REGISTERED_ERROR);
        log.info(REGISTERING_LOG_MSG, "Authorizer", authorizer.toString());
    }
    
    
    @Override
    public void unregisterAuthorizer(IAuthorizer authorizer)
    {
        if (!this.authorizer.compareAndSet(authorizer, null))
            throw new IllegalStateException("An Authorizer" + CANNOT_UNREGISTER_ERROR);
        log.info(UNREGISTERING_LOG_MSG, "Authorizer", authorizer.toString());
    }
    
    
    @Override
    public IUserRegistry getUserRegistry()
    {
        Asserts.checkState(userDB.get() != null, "No IUserRegistry implementation registered");
        return userDB.get();
    }
    
    
    @Override
    public IUserInfo getUserInfo(String userID)
    {
        return getUserRegistry().get(userID);
    }
    
    
    @Override
    public boolean isAuthorized(IUserInfo user, IPermissionPath request)
    {
        Asserts.checkState(authorizer.get() != null, "No IAuthorizer implementation registered");
        return authorizer.get().isAuthorized(user, request);
    }


    @Override
    public void registerModulePermissions(IPermission perm)
    {
        modulePermissions.put(perm.getName(), perm);
    }


    @Override
    public void unregisterModulePermissions(IPermission perm)
    {
        modulePermissions.remove(perm.getName());
    }
    
    
    @Override
    public Authenticator getAuthenticator()
    {
        try
        {
            Async.waitForCondition(() -> authenticator.delegate.get() != null, TIMEOUT);
            return authenticator;
        }
        catch (TimeoutException e)
        {
            throw new IllegalStateException("No Authenticator implementation registered before timeout");
        }
    }


    @Override
    public IPermission getModulePermissions(String moduleIdString)
    {
        if (IPermission.WILDCARD.equals(moduleIdString))
            return new WildcardPermission("All Modules");
        
        return modulePermissions.get(moduleIdString);
    }
    
    
    @Override
    public Collection<IPermission> getAllModulePermissions()
    {
        return Collections.unmodifiableCollection(modulePermissions.values());
    }
    
    
    /*
     * We use a wrapper so we can change the implementation dynamically
     */
    static class AuthenticatorWrapper implements Authenticator
    {
        AtomicReference<Authenticator> delegate = new AtomicReference<>();
        
        protected Authenticator get()
        {
            Asserts.checkState(delegate.get() != null, "No Authenticator implementation registered");
            return delegate.get();
        }
        
        public void setConfiguration(AuthConfiguration configuration)
        {
            get().setConfiguration(configuration);
        }

        public String getAuthMethod()
        {
            return get().getAuthMethod();
        }

        public void prepareRequest(ServletRequest request)
        {
            get().prepareRequest(request);
        }

        public Authentication validateRequest(ServletRequest request, ServletResponse response, boolean mandatory) throws ServerAuthException
        {
            return get().validateRequest(request, response, mandatory);
        }

        public boolean secureResponse(ServletRequest request, ServletResponse response, boolean mandatory, User validatedUser) throws ServerAuthException
        {
            return get().secureResponse(request, response, mandatory, validatedUser);
        }
    }

}
