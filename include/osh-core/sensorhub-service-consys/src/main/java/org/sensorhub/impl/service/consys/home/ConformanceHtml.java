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
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.resource.ResourceBindingHtml;
import org.vast.util.Asserts;
import static j2html.TagCreator.*;


public class ConformanceHtml extends ResourceBindingHtml<Long, Set<String>>
{
    
    public ConformanceHtml(RequestContext ctx) throws IOException
    {
        super(ctx, null);
    }
    
    
    @Override
    public void serialize(Long key, Set<String> confClasses, boolean showLinks) throws IOException
    {
        Asserts.checkNotNullOrEmpty(confClasses, "ConformanceClasses");
        writeHeader();
        
        div(
            h3("Conformance Declaration"),
            p("This API implements the conformance classes from standards and community specifications that are listed below. Conformance classes are identified by a URI."),
            ul(
                each(confClasses, s -> li(span(s).withClass("text-primary")))
            )
        ).render(html);
        
        writeFooter();
        writer.flush();
    }
}
