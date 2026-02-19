/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.system;

import java.io.IOException;
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.impl.service.consys.feature.FeatureUtils;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.impl.service.consys.sensorml.SmlFeatureBindingHtml;
import j2html.tags.specialized.DivTag;
import static j2html.TagCreator.*;


/**
 * <p>
 * HTML formatter for system resources
 * </p>
 *
 * @author Alex Robin
 * @since March 31, 2022
 */
public class SystemBindingHtml extends SmlFeatureBindingHtml<ISystemWithDesc, IObsSystemDatabase>
{
    final SystemAssocs assocs;
    final String collectionTitle;
    
    
    public SystemBindingHtml(RequestContext ctx, IdEncoders idEncoders, IObsSystemDatabase db, boolean isSummary) throws IOException
    {
        super(ctx, idEncoders, db, isSummary, true);
        this.assocs = new SystemAssocs(db, idEncoders);
        
        // set collection title depending on path
        if (ctx.getParentID() != null)
        {
            // fetch parent system name
            var parentSys = FeatureUtils.getClosestToNow(db.getSystemDescStore(), ctx.getParentID());
            
            if (isHistory)
                this.collectionTitle = "History of " + parentSys.getName();
            else
                this.collectionTitle = "Subsystems of " + parentSys.getName();
        }
        else
            this.collectionTitle = "System Instances";
    }
    
    
    @Override
    protected String getResourceName()
    {
        return "System";
    }
    
    
    @Override
    protected String getCollectionTitle()
    {
        return collectionTitle;
    }
    
    
    @Override
    protected String getResourceUrl(FeatureKey key)
    {
        var sysId = idEncoders.getSystemIdEncoder().encodeID(key.getInternalID());
        var sysUrl = ctx.getApiRootURL() + "/" + SystemHandler.NAMES[0] + "/" + sysId;
        if (isHistory)
            sysUrl += "/history/" + key.getValidStartTime().toString();
        return sysUrl;
    }
    
    
    @Override
    protected DivTag getLinks(String resourceUrl, FeatureKey key, ISystemWithDesc f)
    {
        var sysId = idEncoders.getSystemIdEncoder().encodeID(key.getInternalID());
        
        return div(
            !isHistory ? each(
                iff(assocs.getParentLink(sysId, ResourceFormat.HTML),
                    link -> getLinkButton("Parent System", link.getHref())),
                iff(assocs.getSubsystemsLink(sysId, ResourceFormat.HTML),
                    link -> getLinkButton("Subsystems", link.getHref())),
                iff(assocs.getSamplingFeaturesLink(sysId, ResourceFormat.HTML),
                    link -> getLinkButton("Sampling Features", link.getHref())),
                iff(assocs.getDataStreamsLink(sysId, ResourceFormat.HTML),
                    link -> getLinkButton("Datastreams", link.getHref())),
                iff(assocs.getControlStreamsLink(sysId, ResourceFormat.HTML),
                    link -> getLinkButton("Control Channels", link.getHref())),
                iff(assocs.getHistoryLink(sysId, ResourceFormat.HTML),
                    link -> getLinkButton("History", link.getHref()))
            ) : null
        );
    }
}
