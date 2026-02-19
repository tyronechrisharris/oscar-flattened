/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.home;

import java.io.IOException;
import java.util.Set;
import org.sensorhub.impl.service.consys.BaseHandler;
import org.sensorhub.impl.service.consys.InvalidRequestException;
import org.sensorhub.impl.service.consys.ServiceErrors;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;


public class ConformanceHandler extends BaseHandler
{
    public static final String[] NAMES = { "conformance" };
    
    final Set<String> confClasses;
    
    
    public ConformanceHandler(Set<String> confClasses)
    {
        this.confClasses = confClasses;
    }
    
    
    @Override
    public String[] getNames()
    {
        return NAMES;
    }
    
    
    @Override
    public void doGet(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException
    {
        var format = parseFormat(ctx.getParameterMap());
        
        // set content type
        ctx.setResponseContentType(format.getMimeType());
        
        if (format.equals(ResourceFormat.AUTO) && ctx.isBrowserHtmlRequest())
            new ConformanceHtml(ctx).serialize(0L, confClasses, true);
        else if (format.isOneOf(ResourceFormat.AUTO, ResourceFormat.JSON))
            new ConformanceJson(ctx).serialize(0L, confClasses, true);
        else
            throw ServiceErrors.unsupportedFormat(format);
    }
    
    
    @Override
    public void doPost(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException
    {
        ServiceErrors.unsupportedOperation("");
    }
    

    @Override
    public void doPut(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException
    {
        ServiceErrors.unsupportedOperation("");
    }
    

    @Override
    public void doDelete(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException
    {
        ServiceErrors.unsupportedOperation("");
    }

}
