/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2024 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.deployment;

import java.util.Optional;
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.deployment.DeploymentFilter;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.impl.service.consys.resource.ResourceLink;


public class DeploymentAssocs
{
    public static final String REL_PARENT = "parent";
    public static final String REL_SUBDEPL = "subdeployments";
    public static final String REL_FOI = "featuresOfInterest";
    public static final String REL_SF = "samplingFeatures";
    public static final String REL_DATASTREAMS = "datastreams";
    
    IObsSystemDatabase db;
    IdEncoders idEncoders;
    
    
    public DeploymentAssocs(IObsSystemDatabase db, IdEncoders idEncoders)
    {
        this.db = db;
        this.idEncoders = idEncoders;
    }
    
    
    public ResourceLink getCanonicalLink(String deplId)
    {
        return new ResourceLink.Builder()
            .rel("canonical")
            .href("/" + DeploymentHandler.NAMES[0] + "/" + deplId)
            .type(ResourceFormat.JSON.getMimeType())
            .build();
    }
    
    
    public ResourceLink getAlternateLink(String deplId, ResourceFormat format, String formatName)
    {
        return new ResourceLink.Builder()
            .rel("alternate")
            .title("This deployment resource in " + formatName + " format")
            .href("/" + DeploymentHandler.NAMES[0] + "/" + deplId)
            .withFormat(format)
            .build();
    }
    
    
    public Optional<ResourceLink> getParentLink(String deplId, ResourceFormat format)
    {
        var internalId = idEncoders.getDeploymentIdEncoder().decodeID(deplId);
        
        var parentSysId = db.getDeploymentStore().getParent(internalId);
        if (parentSysId != null)
        {
            var encodedParentId = idEncoders.getDeploymentIdEncoder().encodeID(parentSysId);
            
            return Optional.of(
                new ResourceLink.Builder()
                    .rel(REL_PARENT)
                    .title("Parent deployment")
                    .href("/" + DeploymentHandler.NAMES[0] + "/" + encodedParentId)
                    .withFormat(format)
                    .build()
            );
        }
        
        return Optional.empty();
    }
    
    
    public Optional<ResourceLink> getSubdeploymentsLink(String deplId, ResourceFormat format)
    {
        var internalId = idEncoders.getDeploymentIdEncoder().decodeID(deplId);
        
        var hasSubDepl = db.getDeploymentStore().countMatchingEntries(new DeploymentFilter.Builder()
            .withParents(internalId)
            .withLimit(1)
            .build()) > 0;
            
        if (hasSubDepl)
        {
            return Optional.of(
                new ResourceLink.Builder()
                    .rel(REL_SUBDEPL)
                    .title("List of subdeployments")
                    .href("/" + DeploymentHandler.NAMES[0] + "/" + deplId + "/" + DeploymentMembersHandler.NAMES[0])
                    .withFormat(format)
                    .build()
            );
        }
        
        return Optional.empty();
    }
}
