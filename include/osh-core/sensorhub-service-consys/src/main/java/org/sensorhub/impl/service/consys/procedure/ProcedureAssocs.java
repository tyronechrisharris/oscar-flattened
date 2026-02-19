/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2024 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.procedure;

import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.database.IProcedureDatabase;
import org.sensorhub.api.procedure.IProcedureWithDesc;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.impl.service.consys.resource.ResourceLink;
import org.sensorhub.impl.service.consys.system.SystemHandler;


public class ProcedureAssocs
{
    public static final String REL_SYSTEMS = "implementingSystems";
    
    IProcedureDatabase db;
    IdEncoders idEncoders;
    
    
    public ProcedureAssocs(IProcedureDatabase db, IdEncoders idEncoders)
    {
        this.db = db;
        this.idEncoders = idEncoders;
    }
    
    
    public ResourceLink getCanonicalLink(String procId)
    {
        return new ResourceLink.Builder()
            .rel("canonical")
            .href("/" + ProcedureHandler.NAMES[0] + "/" + procId)
            .type(ResourceFormat.JSON.getMimeType())
            .build();
    }
    
    
    public ResourceLink getAlternateLink(String procId, ResourceFormat format, String formatName)
    {
        return new ResourceLink.Builder()
            .rel("alternate")
            .title("This procedure resource in " + formatName + " format")
            .href("/" + ProcedureHandler.NAMES[0] + "/" + procId)
            .withFormat(format)
            .build();
    }
    
    
    public ResourceLink getImplementingSystemsLink(IProcedureWithDesc proc, ResourceFormat format)
    {
        return new ResourceLink.Builder()
            .rel(REL_SYSTEMS)
            .title("Systems implementing this procedure")
            .href("/" + SystemHandler.NAMES[0] + "?procedure=" + proc.getUniqueIdentifier() + "&searchMembers=true")
            .withFormat(format)
            .build();
    }
}
