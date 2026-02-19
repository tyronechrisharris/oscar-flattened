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
import java.util.Collection;
import org.sensorhub.impl.service.consys.ConSysApiServiceConfig;
import org.sensorhub.impl.service.consys.deployment.DeploymentHandler;
import org.sensorhub.impl.service.consys.feature.FoiHandler;
import org.sensorhub.impl.service.consys.obs.DataStreamHandler;
import org.sensorhub.impl.service.consys.obs.ObsHandler;
import org.sensorhub.impl.service.consys.procedure.ProcedureHandler;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.resource.ResourceBindingJson;
import org.sensorhub.impl.service.consys.resource.ResourceLink;
import org.sensorhub.impl.service.consys.system.SystemHandler;
import org.vast.util.ResponsibleParty;
import com.google.common.base.Strings;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;


public class HomePageJson extends ResourceBindingJson<Long, ConSysApiServiceConfig>
{
    
    public HomePageJson(RequestContext ctx) throws IOException
    {
        super(ctx, null, false);
    }
    
    
    @Override
    public void serialize(Long key, ConSysApiServiceConfig config, boolean showLinks, JsonWriter writer) throws IOException
    {
        writer.beginObject();
        
        // title and description
        var serviceInfo = config.ogcCapabilitiesInfo;
        var title = config.name;
        var description = config.description;
        if (serviceInfo != null)
        {
            if (!Strings.isNullOrEmpty(serviceInfo.title))
                title = serviceInfo.title;
            if (!Strings.isNullOrEmpty(serviceInfo.description))
                description = serviceInfo.description;
        }
        writer.name("title").value(title);
        if (description != null)
            writer.name("description").value(description);
        
        // service provider info
        if (serviceInfo != null && serviceInfo.serviceProvider != null)
            writeResponsibleParty(serviceInfo.serviceProvider, writer);
            
        // links
        writer.name("links").beginArray();
        
        writer.beginObject();
        writer.name("rel").value("service-desc");
        writer.name("title").value("Definition of the API Part 1 in OpenAPI 3.1");
        writer.name("href").value(HomePageHandler.APISPEC_URL1);
        writer.name("type").value("application/vnd.oai.openapi;version=3.1");
        writer.endObject();
        
        writer.beginObject();
        writer.name("rel").value("service-desc");
        writer.name("title").value("Definition of the API Part 2 in OpenAPI 3.1");
        writer.name("href").value(HomePageHandler.APISPEC_URL2);
        writer.name("type").value("application/vnd.oai.openapi;version=3.1");
        writer.endObject();
        
        writer.beginObject();
        writer.name("rel").value("conformance");
        writer.name("title").value("OGC API conformance classes implemented by this server");
        writer.name("href").value(ctx.getApiRootURL() + "/" + ConformanceHandler.NAMES[0]);
        writer.name("type").value("application/json");
        writer.endObject();
        
        writer.beginObject();
        writer.name("rel").value("collections");
        writer.name("title").value("Collections available on this server");
        writer.name("href").value(ctx.getApiRootURL() + "/" + CollectionHandler.NAMES[0]);
        writer.name("type").value("application/json");
        writer.endObject();
        
        writer.beginObject();
        writer.name("rel").value("systems");
        writer.name("title").value("System instances registered on this server");
        writer.name("href").value(ctx.getApiRootURL() + "/" + SystemHandler.NAMES[0]);
        writer.name("type").value("application/json");
        writer.endObject();
        
        writer.beginObject();
        writer.name("rel").value("deployments");
        writer.name("title").value("System deployments registered on this server");
        writer.name("href").value(ctx.getApiRootURL() + "/" + DeploymentHandler.NAMES[0]);
        writer.name("type").value("application/json");
        writer.endObject();
        
        writer.beginObject();
        writer.name("rel").value("procedures");
        writer.name("title").value("System datasheets and procedures registered on this server");
        writer.name("href").value(ctx.getApiRootURL() + "/" + ProcedureHandler.NAMES[0]);
        writer.name("type").value("application/json");
        writer.endObject();
        
        writer.beginObject();
        writer.name("rel").value("samplingFeatures");
        writer.name("title").value("Sampling features linked to systems registered on this server");
        writer.name("href").value(ctx.getApiRootURL() + "/" + FoiHandler.NAMES[0]);
        writer.name("type").value("application/json");
        writer.endObject();
        
        writer.beginObject();
        writer.name("rel").value("datastreams");
        writer.name("title").value("Datastreams available through this server");
        writer.name("href").value(ctx.getApiRootURL() + "/" + DataStreamHandler.NAMES[0]);
        writer.name("type").value("application/json");
        writer.endObject();
        
        writer.beginObject();
        writer.name("rel").value("observations");
        writer.name("title").value("Observations available through this server");
        writer.name("href").value(ctx.getApiRootURL() + "/" + ObsHandler.NAMES[0]);
        writer.name("type").value("application/json");
        writer.endObject();
        
        writer.endArray();
        writer.endObject();
        writer.flush();
    }
    
    
    void writeResponsibleParty(ResponsibleParty rp, JsonWriter writer) throws IOException
    {
        writer.name("serviceProvider").beginObject();
        
        if (!Strings.isNullOrEmpty(rp.getOrganizationName()))
            writer.name("organisationName").value(rp.getOrganizationName());
        
        if (!Strings.isNullOrEmpty(rp.getIndividualName()))
            writer.name("individualName").value(rp.getIndividualName());
        
        if (!Strings.isNullOrEmpty(rp.getPositionName()))
            writer.name("positionName").value(rp.getPositionName());
        
        if (rp.hasContactInfo())
        {
            writer.name("contactInfo").beginObject();
            
            if (!rp.getVoiceNumbers().isEmpty() || !rp.getFaxNumbers().isEmpty())
            {
                writer.name("voice").beginObject();
                
                if (!rp.getVoiceNumbers().isEmpty())
                {
                    writer.name("phone").beginArray();
                    for (var num: rp.getVoiceNumbers())
                        writer.value(num);
                    writer.endArray();
                }
                
                if (!rp.getFaxNumbers().isEmpty())
                {
                    writer.name("facsimile").beginArray();
                    for (var num: rp.getVoiceNumbers())
                        writer.value(num);
                    writer.endArray();
                }
                
                writer.endObject();
            }
                
            if (!Strings.isNullOrEmpty(rp.getDeliveryPoint()))
                writer.name("deliveryPoint").value(rp.getDeliveryPoint());
            
            if (rp.hasAddress())
            {
                writer.name("address").beginObject();
                
                if (!Strings.isNullOrEmpty(rp.getDeliveryPoint()))
                    writer.name("deliveryPoint").value(rp.getDeliveryPoint());
                
                if (!Strings.isNullOrEmpty(rp.getCity()))
                    writer.name("city").value(rp.getCity());
                
                if (!Strings.isNullOrEmpty(rp.getAdministrativeArea()))
                    writer.name("administrativeArea").value(rp.getAdministrativeArea());
                
                if (!Strings.isNullOrEmpty(rp.getPostalCode()))
                    writer.name("postalCode").value(rp.getPostalCode());
                
                if (!Strings.isNullOrEmpty(rp.getCountry()))
                    writer.name("country").value(rp.getCountry());
                
                if (!Strings.isNullOrEmpty(rp.getEmail()))
                    writer.name("electronicMailAddress").value(rp.getEmail());
                
                writer.endObject();
            }
            
            writer.endObject();
        }
        
        writer.endObject();
    }


    @Override
    public ConSysApiServiceConfig deserialize(JsonReader reader) throws IOException
    {
        // this should never be called since home page is read-only
        throw new UnsupportedOperationException();
    }
    
    
    @Override
    public void startCollection() throws IOException
    {
        // this should never be called since home page is not a collection
        throw new UnsupportedOperationException();
    }


    @Override
    public void endCollection(Collection<ResourceLink> links) throws IOException
    {
        // this should never be called since home page is not a collection
        throw new UnsupportedOperationException();
    }
}
