/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2023 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.ui;

import java.util.HashSet;
import java.util.Set;
import org.sensorhub.api.module.IModule;
import com.vaadin.server.ErrorMessage;
import com.vaadin.shared.ui.ErrorLevel;


@SuppressWarnings("serial")
public class ModuleErrors implements ErrorMessage
{
    Set<IModule<?>> modulesOnError = new HashSet<>();
    
    
    @Override
    public ErrorLevel getErrorLevel()
    {
        return ErrorLevel.CRITICAL;
    }


    @Override
    public String getFormattedHtmlMessage()
    {
        return "Module Error";
    }
    
    
    public boolean setModuleErrorState(IModule<?> m, boolean onError)
    {
        if (onError)
            modulesOnError.add(m);
        else
            modulesOnError.remove(m);
        
        return !modulesOnError.isEmpty();
    }

}
