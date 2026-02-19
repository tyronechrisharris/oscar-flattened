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
import org.sensorhub.impl.service.consys.ConSysApiServiceConfig;
import org.sensorhub.impl.service.consys.deployment.DeploymentHandler;
import org.sensorhub.impl.service.consys.feature.FoiHandler;
import org.sensorhub.impl.service.consys.obs.DataStreamHandler;
import org.sensorhub.impl.service.consys.obs.ObsHandler;
import org.sensorhub.impl.service.consys.procedure.ProcedureHandler;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.resource.ResourceBindingHtml;
import org.sensorhub.impl.service.consys.system.SystemHandler;
import com.google.common.base.Strings;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.Tag;
import static j2html.TagCreator.*;


public class HomePageHtml extends ResourceBindingHtml<Long, ConSysApiServiceConfig>
{
    
    public HomePageHtml(RequestContext ctx) throws IOException
    {
        super(ctx, null);
    }
    
    
    @Override
    public void serialize(Long key, ConSysApiServiceConfig config, boolean showLinks) throws IOException
    {
        writeHeader();
        
        // title and description
        var serviceInfo = config.ogcCapabilitiesInfo;
        var title = config.name;
        var description = config.description;
        var orgName = "OpenSensorHub";
        if (serviceInfo != null)
        {
            if (!Strings.isNullOrEmpty(serviceInfo.title))
                title = serviceInfo.title;
            if (!Strings.isNullOrEmpty(serviceInfo.description))
                description = serviceInfo.description;
            if (serviceInfo.serviceProvider != null && !Strings.isNullOrEmpty(serviceInfo.serviceProvider.getOrganizationName()))
                orgName = serviceInfo.serviceProvider.getOrganizationName();
        }
        
        div(
            h3(title),
            div(
                iff(!Strings.isNullOrEmpty(description), div(
                    description
                ).withClasses("card-subtitle", "text-muted"))
            )
        ).render(html);
        
        // API info
        renderCard("API Information",
            p(strong("Service Provider: "), text(orgName)),
            p(a("Definition of the API Part 1 in OpenAPI 3.1").withHref(HomePageHandler.APISPEC_URL1)),
            p(a("Definition of the API Part 2 in OpenAPI 3.1").withHref(HomePageHandler.APISPEC_URL2)),
            p(a("Interactive Documentation of the API").withHref(HomePageHandler.APITEST_URL)),
            p(a("OGC API conformance classes implemented by this server").withHref(ctx.getApiRootURL() + "/" + ConformanceHandler.NAMES[0]))
        );
        
        // links
        renderCard("Available Resources",
            p(a("Data collections available on this server").withHref(ctx.getApiRootURL() + "/" + CollectionHandler.NAMES[0])),
            p(a("System instances").withHref(ctx.getApiRootURL() + "/" + SystemHandler.NAMES[0])),
            p(a("System datasheets and procedures").withHref(ctx.getApiRootURL() + "/" + ProcedureHandler.NAMES[0])),
            p(a("System deployments").withHref(ctx.getApiRootURL() + "/" + DeploymentHandler.NAMES[0])),
            p(a("Sampling features").withHref(ctx.getApiRootURL() + "/" + FoiHandler.NAMES[0])),
            p(a("Datastreams").withHref(ctx.getApiRootURL() + "/" + DataStreamHandler.NAMES[0])),
            p(a("Observations").withHref(ctx.getApiRootURL() + "/" + ObsHandler.NAMES[0]))
        );
        
        writeFooter();
        writer.flush();
    }
    
    
    @Override
    protected ContainerTag<?> getCard(Tag<?> title, DomContent... content)
    {
        return div()
            .withClass("card mt-4")
            .with(
                div()
                .withClass("card-body")
                .with(
                    h4(title).withClasses("card-title"),
                    each(content)
                 )
            );
    }
}
