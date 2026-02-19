 /***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.procedure;

import java.io.IOException;
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.database.IProcedureDatabase;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.api.procedure.IProcedureWithDesc;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.impl.service.consys.sensorml.SmlFeatureBindingHtml;
import j2html.tags.specialized.DivTag;
import static j2html.TagCreator.*;


/**
 * <p>
 * HTML formatter for procedure resources
 * </p>
 *
 * @author Alex Robin
 * @since March 31, 2022
 */
public class ProcedureBindingHtml extends SmlFeatureBindingHtml<IProcedureWithDesc, IProcedureDatabase>
{
    final ProcedureAssocs assocs;
    final String collectionTitle;
    
    
    public ProcedureBindingHtml(RequestContext ctx, IdEncoders idEncoders, IProcedureDatabase db, boolean isSummary) throws IOException
    {
        super(ctx, idEncoders, db, isSummary, false);
        this.assocs = new ProcedureAssocs(db, idEncoders);
        this.collectionTitle = "Datasheets and Procedures";
    }
    
    
    @Override
    protected String getResourceName()
    {
        return "Procedure";
    }
    
    
    @Override
    protected String getCollectionTitle()
    {
        return collectionTitle;
    }
    
    
    @Override
    protected String getResourceUrl(FeatureKey key)
    {
        var procId = idEncoders.getProcedureIdEncoder().encodeID(key.getInternalID());
        return ctx.getApiRootURL() + "/" + ProcedureHandler.NAMES[0] + "/" + procId;
    }
    
    
    @Override
    protected DivTag getLinks(String resourceUrl, FeatureKey key, IProcedureWithDesc f)
    {
        return div(
            getLinkButton("Implementing Systems", 
                assocs.getImplementingSystemsLink(f, ResourceFormat.HTML).getHref())
        );
    }
}
