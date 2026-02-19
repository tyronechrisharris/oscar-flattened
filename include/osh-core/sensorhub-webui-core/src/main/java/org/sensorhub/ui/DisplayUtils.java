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

import java.io.PrintWriter;
import java.io.StringWriter;
import org.sensorhub.api.module.IModule;
import org.sensorhub.utils.ModuleUtils;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;


@SuppressWarnings("deprecation")
public class DisplayUtils
{
    
    public static String getPrettyName(String text)
    {
        StringBuilder buf = new StringBuilder(text.substring(text.lastIndexOf('.')+1));
        for (int i=0; i<buf.length()-1; i++)
        {
            char c = buf.charAt(i);
            
            if (i == 0)
            {
                char newcar = Character.toUpperCase(c);
                buf.setCharAt(i, newcar);
            }
                    
            else if (Character.isUpperCase(c) && Character.isLowerCase(buf.charAt(i+1)))
            {
                buf.insert(i, ' ');
                i++;
            }
        }
        
        return buf.toString();
    }
    
    
    public static void showOperationSuccessful(String text)
    {
        showOperationSuccessful(text, 2000);
    }
    
    
    public static void showOperationSuccessful(String text, int delayMs)
    {
        Notification notif = new Notification(
                "<span style=\"color:green\">" + FontAwesome.CHECK_CIRCLE_O.getHtml() +
                "</span>&nbsp;&nbsp;" + text, Notification.Type.WARNING_MESSAGE);
        notif.setHtmlContentAllowed(true);
        notif.setDelayMsec(delayMs);
        notif.show(UI.getCurrent().getPage());
    }
    
    
    public static void showUnauthorizedAccess(String text)
    {
        Notification notif = new Notification(
                "<span style=\"color:white\">" + FontAwesome.MINUS_CIRCLE.getHtml() +
                "</span>&nbsp;&nbsp;" + text, Notification.Type.ERROR_MESSAGE);
        notif.setHtmlContentAllowed(true);
        notif.show(UI.getCurrent().getPage());
    }

    
    public static void showErrorPopup(String msg, Throwable e)
    {
        if (e != null)
        {
            ((AdminUI)UI.getCurrent()).getOshLogger().error(msg, e);
            msg += "<br/>" + e.getMessage();
        }
        
        new Notification(
                "Error<br/>",
                msg,
                Notification.Type.ERROR_MESSAGE, true)
                .show(UI.getCurrent().getPage());
    }
    
    
    public static void showErrorDetails(IModule<?> module, Throwable e)
    {
        if (e == null)
            return;
        
        StringWriter writer = new StringWriter();
        
        // scan causes for NoClassDefFoundErrors
        // -> warn of a potential dependency problem
        Throwable error = e;
        while (error != null)
        {
            if (error instanceof NoClassDefFoundError || error instanceof ClassNotFoundException)
            {
                writer.append(getDependencyErrorMessage(module.getClass()));
                writer.append('\n');
                break;
            }
            
            error = error.getCause();
        }
        
        e.printStackTrace(new PrintWriter(writer));
        String stackTrace = "<pre>" + writer.toString() + "</pre>";
        
        new Notification(
                "Error<br/>",
                stackTrace,
                Notification.Type.ERROR_MESSAGE, true)
                .show(UI.getCurrent().getPage());
    }
    
    
    public static void showDependencyError(Class<?> clazz, Throwable e)
    {
        showErrorPopup(getDependencyErrorMessage(clazz), e);
    }
    
    
    private static String getDependencyErrorMessage(Class<?> clazz)
    {
        StringBuilder msg = new StringBuilder();
        msg.append("A class could not be found at runtime.\nPlease check that the following dependencies are installed:\n\n");
        for (String dep: ModuleUtils.getBundleDependencies(clazz))
            msg.append(dep).append('\n');
        return msg.toString();
    }
}
