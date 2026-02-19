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
import java.util.Optional;
import org.sensorhub.impl.service.consys.home.CollectionHandler.CollectionInfo;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.resource.ResourceBindingHtml;
import org.sensorhub.impl.service.consys.resource.ResourceLink;
import static j2html.TagCreator.*;


public class CollectionHtml extends ResourceBindingHtml<String, CollectionInfo>
{
    
    public CollectionHtml(RequestContext ctx) throws IOException
    {
        super(ctx, null);
    }
    
    
    @Override
    protected String getCollectionTitle()
    {
        return "Available Collections";
    }
    
    
    @Override
    protected void writeFooter() throws IOException
    {
        super.writeFooter();
    }
    
    
    @Override
    public void serialize(String key, CollectionInfo col, boolean showLinks) throws IOException
    {
        if (isCollection)
            serializeSummary(key, col);
        else
            serializeSingleSummary(key, col);
    }
    
    
    protected void serializeSingleSummary(String key, CollectionInfo col) throws IOException
    {
        writeHeader();
        serializeSummary(key, col);
        writeFooter();
        writer.flush();
    }
    
    
    protected void serializeSummary(String key, CollectionInfo col) throws IOException
    {
        var itemsLink = col.links.stream()
            .filter(link -> ResourceLink.REL_ITEMS.equals(link.getRel()))
            .findFirst()
            .orElse(null);
        
        renderCard(
            a(
                span(col.title)
            ).withHref(ctx.getApiRootURL() + "/collections/" + col.id)
             .withClass("text-decoration-none"),
            iff(Optional.ofNullable(col.description), desc -> div(desc)
                .withClasses(CSS_CARD_SUBTITLE)),
            div(
                span("Item Type: ").withClass(CSS_BOLD),
                span(col.itemType)
            ).withClass("mt-2"),
            div(
                span("Feature Type: ").withClass(CSS_BOLD),
                span(col.featureType)
            ).withClass("mt-2"),
            div(
                itemsLink != null ? a(itemsLink.getTitle())
                    .withRel(itemsLink.getRel())
                    .withHref(getAbsoluteHref(itemsLink.getHref())) : null
            ).withClass("mt-2")
        );
        
        writer.flush();
    }
}
