/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.feature;

import java.io.IOException;
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.api.datastore.feature.FoiFilter;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.system.SystemHandler;
import org.vast.ogc.gml.IFeature;
import j2html.tags.specialized.DivTag;
import static j2html.TagCreator.*;


/**
 * <p>
 * HTML formatter for feature of interest resources
 * </p>
 *
 * @author Alex Robin
 * @since March 31, 2022
 */
public class FoiBindingHtml extends AbstractFeatureBindingHtml<IFeature, IObsSystemDatabase>
{
    final String collectionTitle;
    
    
    public FoiBindingHtml(RequestContext ctx, IdEncoders idEncoders, IObsSystemDatabase db, boolean isSummary) throws IOException
    {
        super(ctx, idEncoders, db, isSummary, true);
        
        // set collection title depending on path
        if (ctx.getParentID() != null)
        {
            if (ctx.getParentRef().type instanceof SystemHandler)
            {
                // fetch parent system name
                var parentSys = FeatureUtils.getClosestToNow(db.getSystemDescStore(), ctx.getParentID());
                this.collectionTitle = "Features observed by " + parentSys.getName();
            }
            else
            {
                // fetch parent feature name
                var parentFeature = FeatureUtils.getClosestToNow(db.getFoiStore(), ctx.getParentID());
                
                if (isHistory)
                    this.collectionTitle = "History of " + parentFeature.getName();
                else
                    this.collectionTitle = "Members of " + parentFeature.getName();
            }
        }
        else
            this.collectionTitle = "All Features of Interest";
    }
    
    
    @Override
    protected String getResourceName()
    {
        return "Feature";
    }
    
    
    @Override
    protected String getCollectionTitle()
    {
        return collectionTitle;
    }
    
    
    @Override
    protected String getResourceUrl(FeatureKey key)
    {
        var featureId = idEncoders.getFoiIdEncoder().encodeID(key.getInternalID());
        var featureUrl = ctx.getApiRootURL() + "/" + FoiHandler.NAMES[0] + "/" + featureId;
        if (isHistory)
            featureUrl += "/history/" + key.getValidStartTime().toString();
        return featureUrl;
    }
    
    
    @Override
    protected DivTag getLinks(String resourceUrl, FeatureKey key, IFeature f)
    {
        /*var hasMembers = db.getFoiStore().countMatchingEntries(new FoiFilter.Builder()
            .withParents(key.getInternalID())
            .withLimit(1)
            .build()) > 0;*/
        var hasMembers = false;
        
        var hasHistory = !isHistory && db.getFoiStore().countMatchingEntries(new FoiFilter.Builder()
            .withInternalIDs(key.getInternalID())
            .withAllVersions()
            .withLimit(2)
            .build()) > 1;
            
        return div(
            iff(hasMembers,
                a("Members").withHref(resourceUrl + "/members").withClasses(CSS_LINK_BTN_CLASSES)),
            iff(hasHistory,
                a("History").withHref(resourceUrl + "/history").withClasses(CSS_LINK_BTN_CLASSES))
        );
    }


    @Override
    protected void serializeDetails(FeatureKey key, IFeature res) throws IOException
    {
        serializeSingleSummary(key, res);
    }
}
