/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2024 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.task;

import org.sensorhub.api.command.ICommandStreamInfo;
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.impl.service.consys.resource.ResourceLink;
import org.sensorhub.impl.service.consys.system.SystemHandler;


public class CommandStreamAssocs
{
    public static final String REL_PARENT = "system";
    public static final String REL_COMMANDS = "commands";
    public static final String REL_STATUS = "status";
    
    IObsSystemDatabase db;
    IdEncoders idEncoders;
    
    
    public CommandStreamAssocs(IObsSystemDatabase db, IdEncoders idEncoders)
    {
        this.db = db;
        this.idEncoders = idEncoders;
    }
    
    
    public ResourceLink getCanonicalLink(String dsId)
    {
        return new ResourceLink.Builder()
            .rel("canonical")
            .href("/" + CommandStreamHandler.NAMES[0] + "/" + dsId)
            .type(ResourceFormat.JSON.getMimeType())
            .build();
    }
    
    
    public ResourceLink getAlternateLink(String dsId, ResourceFormat format, String formatName)
    {
        return new ResourceLink.Builder()
            .rel("alternate")
            .title("This controlstream resource in " + formatName + " format")
            .href("/" + CommandStreamHandler.NAMES[0] + "/" + dsId)
            .withFormat(format)
            .build();
    }
    
    
    public ResourceLink getParentLink(ICommandStreamInfo ds, ResourceFormat format)
    {
        var encodedParentId = idEncoders.getSystemIdEncoder().encodeID(ds.getSystemID().getInternalID());
        
        return new ResourceLink.Builder()
            .rel(REL_PARENT)
            .title("Parent system")
            .href("/" + SystemHandler.NAMES[0] + "/" + encodedParentId)
            .withFormat(format)
            .build();
    }
    
    
    public ResourceLink getCommandsLink(String dsId, ResourceFormat format)
    {
        return new ResourceLink.Builder()
            .rel(REL_COMMANDS)
            .title("Commands")
            .href("/" + CommandStreamHandler.NAMES[0] + "/" + dsId + "/" + CommandHandler.NAMES[0])
            .withFormat(format)
            .build();
    }
    
    
    public ResourceLink getStatusLink(String dsId, ResourceFormat format)
    {
        return new ResourceLink.Builder()
            .rel(REL_STATUS)
            .title("Status")
            .href("/" + CommandStreamHandler.NAMES[0] + "/" + dsId + "/" + CommandStatusHandler.NAMES[0])
            .withFormat(format)
            .build();
    }
}
