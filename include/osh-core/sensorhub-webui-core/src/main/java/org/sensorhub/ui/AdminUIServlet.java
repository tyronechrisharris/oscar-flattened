/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2016 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.ui;

import java.io.IOException;
import java.security.Principal;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.vast.ows.OWSUtils;
import com.vaadin.server.VaadinServlet;


@SuppressWarnings("serial")
public class AdminUIServlet extends VaadinServlet
{
    final transient Logger log;
    final transient AdminUISecurity securityHandler;
    
    
    AdminUIServlet(AdminUISecurity securityHandler, Logger log)
    {
        this.log = log;
        this.securityHandler = securityHandler;
    }
    
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            Principal user = request.getUserPrincipal();
            if (user != null)
                securityHandler.setCurrentUser(user.getName());
            
            securityHandler.checkPermission(securityHandler.admin_access);
            
            this.getService().setClassLoader(this.getClass().getClassLoader());
            super.service(request, response);
        }
        catch (SecurityException e)
        {
            log.info("Access Forbidden: {}", e.getMessage());
            sendError(response, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }
        finally
        {
            securityHandler.clearCurrentUser();
        }
    }
    
    
    protected void sendError(HttpServletResponse resp, int errorCode, String errorMsg)
    {
        try
        {
            resp.sendError(errorCode, errorMsg);
        }
        catch (IOException e)
        {
            if (!OWSUtils.isClientDisconnectError(e) && log.isDebugEnabled())
                log.error("Cannot send error", e);
        } 
    }

}
