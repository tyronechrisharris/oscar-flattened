/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2024 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.system;

import java.util.Optional;
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.command.CommandStreamFilter;
import org.sensorhub.api.datastore.feature.FoiFilter;
import org.sensorhub.api.datastore.obs.DataStreamFilter;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.impl.service.consys.feature.FoiHandler;
import org.sensorhub.impl.service.consys.obs.DataStreamHandler;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.impl.service.consys.resource.ResourceLink;
import org.sensorhub.impl.service.consys.task.CommandStreamHandler;


public class SystemAssocs
{
    public static final String REL_PARENT = "parent";
    public static final String REL_SUBSYSTEMS = "subsystems";
    public static final String REL_SF = "samplingFeatures";
    public static final String REL_DATASTREAMS = "datastreams";
    public static final String REL_CONTROLSTREAMS = "controlstreams";
    public static final String REL_HISTORY = "history";
    
    IObsSystemDatabase db;
    IdEncoders idEncoders;
    
    
    public SystemAssocs(IObsSystemDatabase db, IdEncoders idEncoders)
    {
        this.db = db;
        this.idEncoders = idEncoders;
    }
    
    
    public ResourceLink getCanonicalLink(String sysId)
    {
        return new ResourceLink.Builder()
            .rel("canonical")
            .href("/" + SystemHandler.NAMES[0] + "/" + sysId)
            .type(ResourceFormat.JSON.getMimeType())
            .build();
    }
    
    
    public ResourceLink getAlternateLink(String sysId, ResourceFormat format, String formatName)
    {
        return new ResourceLink.Builder()
            .rel("alternate")
            .title("This system resource in " + formatName + " format")
            .href("/" + SystemHandler.NAMES[0] + "/" + sysId)
            .withFormat(format)
            .build();
    }
    
    
    public Optional<ResourceLink> getParentLink(String sysId, ResourceFormat format)
    {
        var internalId = idEncoders.getSystemIdEncoder().decodeID(sysId);
        
        var parentSysId = db.getSystemDescStore().getParent(internalId);
        if (parentSysId != null)
        {
            var encodedParentId = idEncoders.getSystemIdEncoder().encodeID(parentSysId);
            
            return Optional.of(
                new ResourceLink.Builder()
                    .rel(REL_PARENT)
                    .title("Parent system")
                    .href("/" + SystemHandler.NAMES[0] + "/" + encodedParentId)
                    .withFormat(format)
                    .build()
            );
        }
        
        return Optional.empty();
    }
    
    
    public Optional<ResourceLink> getSubsystemsLink(String sysId, ResourceFormat format)
    {
        var internalId = idEncoders.getSystemIdEncoder().decodeID(sysId);
        
        var hasSubSystems = db.getSystemDescStore().countMatchingEntries(new SystemFilter.Builder()
            .withParents(internalId)
            .withCurrentVersion()
            .withLimit(1)
            .build()) > 0;
            
        if (hasSubSystems)
        {
            return Optional.of(
                new ResourceLink.Builder()
                    .rel(REL_SUBSYSTEMS)
                    .title("List of subsystems")
                    .href("/" + SystemHandler.NAMES[0] + "/" + sysId + "/" + SystemMembersHandler.NAMES[0])
                    .withFormat(format)
                    .build()
            );
        }
        
        return Optional.empty();
    }
    
    
    public Optional<ResourceLink> getDataStreamsLink(String sysId, ResourceFormat format)
    {
        var internalId = idEncoders.getSystemIdEncoder().decodeID(sysId);
        
        var hasDataStreams = db.getDataStreamStore().countMatchingEntries(new DataStreamFilter.Builder()
            .withSystems()
                .withInternalIDs(internalId)
                .includeMembers(true)
                .done()
            //.withCurrentVersion()
            .withLimit(1)
            .build()) > 0;
            
        if (hasDataStreams)
        {
            return Optional.of(
                new ResourceLink.Builder()
                    .rel(REL_DATASTREAMS)
                    .title("List of system datastreams")
                    .href("/" + SystemHandler.NAMES[0] + "/" + sysId + "/" + DataStreamHandler.NAMES[0])
                    .withFormat(format)
                    .build()
            );
        }
        
        return Optional.empty();
    }
    
    
    public Optional<ResourceLink> getControlStreamsLink(String sysId, ResourceFormat format)
    {
        var internalId = idEncoders.getSystemIdEncoder().decodeID(sysId);
        
        var hasControls = db.getCommandStreamStore().countMatchingEntries(new CommandStreamFilter.Builder()
            .withSystems()
                .withInternalIDs(internalId)
                .includeMembers(true)
                .done()
            .withCurrentVersion()
            .withLimit(1)
            .build()) > 0;
        
        if (hasControls)
        {
            return Optional.of(
                new ResourceLink.Builder()
                    .rel(REL_CONTROLSTREAMS)
                    .title("List of system controlstreams")
                    .href("/" + SystemHandler.NAMES[0] + "/" + sysId + "/" + CommandStreamHandler.NAMES[0])
                    .withFormat(format)
                    .build()
            );
        }
        
        return Optional.empty();
    }
    
    
    public Optional<ResourceLink> getSamplingFeaturesLink(String sysId, ResourceFormat format)
    {
        var internalId = idEncoders.getSystemIdEncoder().decodeID(sysId);
        
        var hasFois = db.getFoiStore().countMatchingEntries(new FoiFilter.Builder()
            .withParents()
                .withInternalIDs(internalId)
                .includeMembers(true)
                .done()
            .includeMembers(true)
            //.withCurrentVersion()
            .withLimit(1)
            .build()) > 0;
            
        if (hasFois)
        {
            return Optional.of(
                new ResourceLink.Builder()
                    .rel(REL_SF)
                    .title("List of system sampling features")
                    .href("/" + SystemHandler.NAMES[0] + "/" + sysId + "/" + FoiHandler.NAMES[0])
                    .withFormat(format)
                    .build()
            );
        }
        
        return Optional.empty();
    }
    
    
    public Optional<ResourceLink> getHistoryLink(String sysId, ResourceFormat format)
    {
        var internalId = idEncoders.getSystemIdEncoder().decodeID(sysId);
        
        var hasHistory = db.getSystemDescStore().countMatchingEntries(new SystemFilter.Builder()
            .withInternalIDs(internalId)
            .withAllVersions()
            .withLimit(2)
            .build()) > 1;
        
        if (hasHistory)
        {
            return Optional.of(
                new ResourceLink.Builder()
                    .rel(REL_HISTORY)
                    .title("System history")
                    .href("/" + SystemHandler.NAMES[0] + "/" + sysId + "/" + SystemHistoryHandler.NAMES[0])
                    .withFormat(format)
                    .build()
            );
        }
        
        return Optional.empty();
    }
}
