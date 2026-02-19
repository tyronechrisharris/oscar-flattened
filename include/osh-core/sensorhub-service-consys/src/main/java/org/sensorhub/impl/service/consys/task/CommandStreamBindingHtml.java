/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.task;

import java.io.IOException;
import org.sensorhub.api.command.ICommandStreamInfo;
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.command.CommandStreamKey;
import org.sensorhub.impl.service.consys.SWECommonUtils;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.resource.ResourceBindingHtml;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.impl.service.consys.system.SystemHandler;
import com.google.common.collect.ImmutableList;
import static j2html.TagCreator.*;


/**
 * <p>
 * HTML formatter for command stream resources
 * </p>
 *
 * @author Alex Robin
 * @since March 31, 2022
 */
public class CommandStreamBindingHtml extends ResourceBindingHtml<CommandStreamKey, ICommandStreamInfo>
{
    final CommandStreamAssocs assocs;
    final boolean isSummary;
    final String collectionTitle;
    
    
    public CommandStreamBindingHtml(RequestContext ctx, IdEncoders idEncoders, IObsSystemDatabase db, boolean isSummary, String collectionTitle) throws IOException
    {
        super(ctx, idEncoders);
        
        this.assocs = new CommandStreamAssocs(db, idEncoders);
        this.isSummary = isSummary;
        
        if (ctx.getParentID() != null)
        {
            // fetch parent system name
            var parentSys = db.getSystemDescStore().getCurrentVersion(ctx.getParentID());
            this.collectionTitle = collectionTitle.replace("{}", parentSys.getName());
        }
        else
            this.collectionTitle = collectionTitle;
    }
    
    
    public CommandStreamBindingHtml(RequestContext ctx, IdEncoders idEncoders) throws IOException
    {
        super(ctx, idEncoders);
        this.assocs = null;
        this.isSummary = false;
        this.collectionTitle = null;
    }
    
    
    @Override
    protected String getCollectionTitle()
    {
        return collectionTitle;
    }
    
    
    @Override
    public void serialize(CommandStreamKey key, ICommandStreamInfo dsInfo, boolean showLinks) throws IOException
    {
        if (isSummary)
        {
            if (isCollection)
                serializeSummary(key, dsInfo);
            else
                serializeSingleSummary(key, dsInfo);
        }
        else
            serializeDetails(key, dsInfo);
    }
    
    
    protected void serializeSummary(CommandStreamKey key, ICommandStreamInfo dsInfo) throws IOException
    {
        var dsId = idEncoders.getCommandStreamIdEncoder().encodeID(key.getInternalID());
        var dsUrl = ctx.getApiRootURL() + "/" + CommandStreamHandler.NAMES[0] + "/" + dsId;
        
        var sysId = idEncoders.getSystemIdEncoder().encodeID(dsInfo.getSystemID().getInternalID());
        var sysUrl = ctx.getApiRootURL() + "/" + SystemHandler.NAMES[0] + "/" + sysId;
        
        renderCard(
            a(dsInfo.getName())
                .withHref(dsUrl)
                .withClass("text-decoration-none"),
            iff(dsInfo.getDescription() != null, div(
                dsInfo.getDescription()
            ).withClasses(CSS_CARD_SUBTITLE)),
            div(
                span("Receiving System: ").withClass(CSS_BOLD),
                a(dsInfo.getSystemID().getUniqueID()).withHref(sysUrl)
            ),
            div(
                span("Control Input: ").withClass(CSS_BOLD),
                span(dsInfo.getControlInputName())
            ).withClass("mb-2"),
            div(
                span("Valid Time: ").withClass(CSS_BOLD),
                getTimeExtentHtml(dsInfo.getValidTime(), "Always")
            ).withClass("mb-2"),
            div(
                span("Issue Time: ").withClass(CSS_BOLD),
                getTimeExtentHtml(dsInfo.getIssueTimeRange(), "No Data")
            ).withClass("mb-2"),
            div(
                span("Execution Time: ").withClass(CSS_BOLD),
                getTimeExtentHtml(dsInfo.getExecutionTimeRange(), "No Data")
            ).withClass("mb-2"),
            iffElse(isCollection,
                div(
                    span("Controllable Properties: ").withClass(CSS_BOLD),
                    div(
                        each(ImmutableList.copyOf(SWECommonUtils.getProperties(dsInfo.getRecordStructure())),
                            comp -> span(getComponentLabel(comp) + ", "))
                    ).withClass("ps-4")
                ).withClass("mb-2"),
                div(
                    span("Controllable Properties: ").withClass(CSS_BOLD),
                    div(
                        each(ImmutableList.copyOf(SWECommonUtils.getProperties(dsInfo.getRecordStructure())),
                            comp -> getPropertyHtml(comp))
                    ).withClass("ps-4")
                ).withClass("mb-2")
            ),
            
            p(
                iff(isCollection,
                    getLinkButton("Details", assocs.getCanonicalLink(dsId).getHref())
                ),
                getLinkButton("Parent System", assocs.getParentLink(dsInfo, ResourceFormat.HTML).getHref()),
                getLinkButton("Commands", assocs.getCommandsLink(dsId, ResourceFormat.JSON).getHref()),
                getLinkButton("Status", assocs.getStatusLink(dsId, ResourceFormat.JSON).getHref()),
                getLinkButton("Schema", dsUrl + "/schema")
            ).withClass("mt-4"));
    }
    
    
    protected void serializeSingleSummary(CommandStreamKey key, ICommandStreamInfo dsInfo) throws IOException
    {
        writeHeader();
        serializeSummary(key, dsInfo);
        writeFooter();
        writer.flush();
    }
    
    
    protected void serializeDetails(CommandStreamKey key, ICommandStreamInfo dsInfo) throws IOException
    {
        writeHeader();
        
        div(
            h3(dsInfo.getName()),
            h5("Command Message Structure"),
            small(
                getComponentHtml(dsInfo.getRecordStructure()),
                getEncodingHtml(dsInfo.getRecordEncoding())
            )
        ).render(html);
        
        writeFooter();
        writer.flush();
    }
}
