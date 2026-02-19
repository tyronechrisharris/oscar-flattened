/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Authentication.User;
import org.slf4j.Logger;
import org.vast.util.Asserts;


public class HttpLogoutWrapper implements Authenticator
{
    private Authenticator delegate;
    private Logger log;
    
    
    public HttpLogoutWrapper(Authenticator delegate, Logger log)
    {
        this.delegate = Asserts.checkNotNull(delegate, Authenticator.class);
        this.log = Asserts.checkNotNull(log, Logger.class);
    }
    
    
    @Override
    public void setConfiguration(AuthConfiguration configuration)
    {
        delegate.setConfiguration(configuration);
    }


    @Override
    public String getAuthMethod()
    {
        return delegate.getAuthMethod();
    }


    @Override
    public void prepareRequest(ServletRequest request)
    {
        delegate.prepareRequest(request);
    }


    @Override
    public Authentication validateRequest(ServletRequest req, ServletResponse res, boolean mandatory) throws ServerAuthException
    {
        // catch logout case
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;
        if (request.getServletPath() != null && request.getServletPath().endsWith("/logout"))
        {
            try
            {
                request.logout();
                var session = request.getSession(false);
                if (session != null)
                {
                    session.invalidate();
                    log.debug("Logging out {}", session.getId());
                }
                
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                var adminUrl = request.getRequestURL().toString().replace("logout", "admin");
                var redirectResp = "<html><script>window.location.href=\"" + adminUrl + "\";</script></html>";
                response.getOutputStream().write(redirectResp.getBytes());
                return Authentication.SEND_CONTINUE;
            }
            catch (Exception e)
            {
                log.error("Error while logging out", e);
            }
            
        }
        
        return delegate.validateRequest(request, response, mandatory);
    }


    @Override
    public boolean secureResponse(ServletRequest request, ServletResponse response, boolean mandatory, User validatedUser) throws ServerAuthException
    {
        return delegate.secureResponse(request, response, mandatory, validatedUser);
    }

}
