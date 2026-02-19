/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.service;

import java.util.Map;
import javax.servlet.http.HttpServlet;
import org.sensorhub.api.module.IModule;
import org.sensorhub.api.module.ModuleConfig;


/**
 * <p>
 * Base interface for HTTP server implementations
 * </p>
 * 
 * @param <T> Type of module configuration
 *
 * @author Alex Robin
 * @since Apr 21, 2021
 */
public interface IHttpServer<T extends ModuleConfig> extends IModule<T>
{
    
    public void deployServlet(HttpServlet servlet, String path);
    
    
    public void deployServlet(HttpServlet servlet, Map<String, String> initParams, String... paths);
    
    
    public void undeployServlet(HttpServlet servlet);
    
    
    public void addServletSecurity(String pathSpec, boolean requireAuth);
    
    
    public void addServletSecurity(String pathSpec, boolean requireAuth, String... roles);
    
    
    /**
     * @return The base URL under which this server is exposed to consumers
     * (i.e it will start with the proxy URL if configured)
     */
    public String getServerBaseUrl();
    
    
    /**
     * @return The base URL under which web services and APIs are exposed by
     * this server
     */
    public String getServletsBaseUrl();
    
    
    /**
     * @param path desired URL path
     * @return The full endpoint URL allowing access to the given URL path
     */
    public String getPublicEndpointUrl(String path);
    
    
    /**
     * Check if authentication is enabled on the HTTP server
     * @return true if at least one authentication method is enabled, false otherwise
     */
    public boolean isAuthEnabled();
    
}
