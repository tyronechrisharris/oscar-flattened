/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.command.CommandStreamKey;
import org.sensorhub.api.datastore.obs.DataStreamKey;
import org.sensorhub.api.module.ModuleConfig;
import org.sensorhub.api.security.IAuthorizer;
import org.sensorhub.api.security.IPermission;
import org.sensorhub.impl.module.AbstractModule;
import org.sensorhub.impl.module.ModuleSecurity;
import org.sensorhub.impl.security.ItemPermission;
import org.sensorhub.impl.security.ItemWithIdPermission;
import org.sensorhub.impl.security.ItemWithParentPermission;
import org.sensorhub.impl.security.ModulePermissions;
import org.sensorhub.impl.security.WildcardPermission;
import org.sensorhub.impl.service.consys.RestApiServlet.ResourcePermissions;
import org.vast.util.Asserts;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


public class ConSysApiSecurity extends ModuleSecurity implements RestApiSecurity
{
    private static final String NAME_PROCEDURE = "procedures";
    private static final String NAME_PROPERTY = "properties";
    private static final String NAME_SYSTEM = "systems";
    private static final String NAME_DEPLOYMENT = "deployments";
    private static final String NAME_FOI = "fois";
    private static final String NAME_DATASTREAM = "datastreams";
    private static final String NAME_OBS = "obs";
    private static final String NAME_CONTROLS = "controls";
    private static final String NAME_COMMANDS = "commands";
    private static final String NAME_COMMANDS_STATUS = "status";
    
    private static final String LABEL_PROCEDURE = "Procedures";
    private static final String LABEL_PROPERTY = "Properties";
    private static final String LABEL_SYSTEM = "Systems";
    private static final String LABEL_DEPLOYMENT = "Deployments";
    private static final String LABEL_FOI = "Features of Interest";
    private static final String LABEL_DATASTREAM = "Datastreams";
    private static final String LABEL_OBS = "Observations";
    private static final String LABEL_CONTROLS = "Control Channels";
    private static final String LABEL_COMMANDS = "Commands";
    private static final String LABEL_COMMANDS_STATUS = "Commands Status";
    
    public final IPermission api_get;
    public final IPermission api_list;
    public final IPermission api_stream;
    public final IPermission api_create;
    public final IPermission api_update;
    public final IPermission api_delete;
    public final IPermission api_allOps;
    
    public final ResourcePermissions procedure_permissions = new ResourcePermissions();
    public final ResourcePermissions property_permissions = new ResourcePermissions();
    public final ResourcePermissions system_permissions = new ResourcePermissions();
    public final ResourcePermissions deployment_permissions = new ResourcePermissions();
    public final ResourcePermissions foi_permissions = new ResourcePermissions();
    public final ResourcePermissions datastream_permissions = new ResourcePermissions();
    public final ResourcePermissions obs_permissions = new ResourcePermissions();
    public final ResourcePermissions commandstream_permissions = new ResourcePermissions();
    public final ResourcePermissions command_permissions = new ResourcePermissions();
    public final ResourcePermissions command_status_permissions = new ResourcePermissions();
    
    ConSysApiService service;
    IObsSystemDatabase db;
    IAuthorizer authorizer;
    
    
    static class DummyModule extends AbstractModule<ModuleConfig>
    {
        @Override
        public String getLocalID()
        {
            return "0";
        }

        @Override
        protected void doStart() throws SensorHubException
        {
        }

        @Override
        protected void doStop() throws SensorHubException
        {
        }
    }
    
    
    public ConSysApiSecurity(ConSysApiService service, IObsSystemDatabase db, boolean enable)
    {
        super(service, "csapi", enable);
        this.service = Asserts.checkNotNull(service, ConSysApiService.class);
        this.db = Asserts.checkNotNull(db, IObsSystemDatabase.class);
        
        // register permission structure
        api_get = new ItemPermission(rootPerm, "get");
        procedure_permissions.get = new ItemPermission(api_get, NAME_PROCEDURE, LABEL_PROCEDURE);
        property_permissions.get = new ItemPermission(api_get, NAME_PROPERTY, LABEL_PROPERTY);
        system_permissions.get = new ItemPermission(api_get, NAME_SYSTEM, LABEL_SYSTEM);
        deployment_permissions.get = new ItemPermission(api_get, NAME_DEPLOYMENT, LABEL_DEPLOYMENT);
        foi_permissions.get = new ItemPermission(api_get, NAME_FOI, LABEL_FOI);
        datastream_permissions.get = new ItemPermission(api_get, NAME_DATASTREAM, LABEL_DATASTREAM);
        obs_permissions.get = new ItemPermission(api_get, NAME_OBS, LABEL_OBS);
        commandstream_permissions.get = new ItemPermission(api_get, NAME_CONTROLS, LABEL_CONTROLS);
        command_permissions.get = new ItemPermission(api_get, NAME_COMMANDS, LABEL_COMMANDS);
        command_status_permissions.get = new ItemPermission(api_get, NAME_COMMANDS_STATUS, LABEL_COMMANDS_STATUS);
        
        api_list = new ItemPermission(rootPerm, "list");
        procedure_permissions.list = new ItemPermission(api_list, NAME_PROCEDURE, LABEL_PROCEDURE);
        property_permissions.list = new ItemPermission(api_list, NAME_PROPERTY, LABEL_PROPERTY);
        system_permissions.list = new ItemPermission(api_list, NAME_SYSTEM, LABEL_SYSTEM);
        deployment_permissions.list = new ItemPermission(api_list, NAME_DEPLOYMENT, LABEL_DEPLOYMENT);
        foi_permissions.list = new ItemPermission(api_list, NAME_FOI, LABEL_FOI);
        datastream_permissions.list = new ItemPermission(api_list, NAME_DATASTREAM, LABEL_DATASTREAM);
        obs_permissions.list = new ItemPermission(api_list, NAME_OBS, LABEL_OBS);
        commandstream_permissions.list = new ItemPermission(api_list, NAME_CONTROLS, LABEL_CONTROLS);
        command_permissions.list = new ItemPermission(api_list, NAME_COMMANDS, LABEL_COMMANDS);
        command_status_permissions.list = new ItemPermission(api_list, NAME_COMMANDS_STATUS, LABEL_COMMANDS_STATUS);
        
        api_stream = new ItemPermission(rootPerm, "stream");
        procedure_permissions.stream = new ItemPermission(api_stream, NAME_PROCEDURE, LABEL_PROCEDURE);
        property_permissions.stream = new ItemPermission(api_stream, NAME_PROPERTY, LABEL_PROPERTY);
        system_permissions.stream = new ItemPermission(api_stream, NAME_SYSTEM, LABEL_SYSTEM);
        deployment_permissions.stream = new ItemPermission(api_stream, NAME_DEPLOYMENT, LABEL_DEPLOYMENT);
        foi_permissions.stream = new ItemPermission(api_stream, NAME_FOI, LABEL_FOI);
        datastream_permissions.stream = new ItemPermission(api_stream, NAME_DATASTREAM, LABEL_DATASTREAM);
        obs_permissions.stream = new ItemPermission(api_stream, NAME_OBS, LABEL_OBS);
        commandstream_permissions.stream = new ItemPermission(api_stream, NAME_CONTROLS, LABEL_CONTROLS);
        command_permissions.stream = new ItemPermission(api_stream, NAME_COMMANDS, LABEL_COMMANDS);
        command_status_permissions.stream = new ItemPermission(api_stream, NAME_COMMANDS_STATUS, LABEL_COMMANDS_STATUS);
        
        api_create = new ItemPermission(rootPerm, "create");
        procedure_permissions.create = new ItemPermission(api_create, NAME_PROCEDURE, LABEL_PROCEDURE);
        property_permissions.create = new ItemPermission(api_create, NAME_PROPERTY, LABEL_PROPERTY);
        system_permissions.create = new ItemPermission(api_create, NAME_SYSTEM, LABEL_SYSTEM);
        deployment_permissions.create = new ItemPermission(api_create, NAME_DEPLOYMENT, LABEL_DEPLOYMENT);
        foi_permissions.create = new ItemPermission(api_create, NAME_FOI, LABEL_FOI);
        datastream_permissions.create = new ItemPermission(api_create, NAME_DATASTREAM, LABEL_DATASTREAM);
        obs_permissions.create = new ItemPermission(api_create, NAME_OBS, LABEL_OBS);
        commandstream_permissions.create = new ItemPermission(api_create, NAME_CONTROLS, LABEL_CONTROLS);
        command_permissions.create = new ItemPermission(api_create, NAME_COMMANDS, LABEL_COMMANDS);
        command_status_permissions.create = new ItemPermission(api_create, NAME_COMMANDS_STATUS, LABEL_COMMANDS_STATUS);
        
        api_update = new ItemPermission(rootPerm, "update");
        procedure_permissions.update = new ItemPermission(api_update, NAME_PROCEDURE, LABEL_PROCEDURE);
        property_permissions.update = new ItemPermission(api_update, NAME_PROPERTY, LABEL_PROPERTY);
        system_permissions.update = new ItemPermission(api_update, NAME_SYSTEM, LABEL_SYSTEM);
        deployment_permissions.update = new ItemPermission(api_update, NAME_DEPLOYMENT, LABEL_DEPLOYMENT);
        foi_permissions.update = new ItemPermission(api_update, NAME_FOI, LABEL_FOI);
        datastream_permissions.update = new ItemPermission(api_update, NAME_DATASTREAM, LABEL_DATASTREAM);
        obs_permissions.update = new ItemPermission(api_update, NAME_OBS, LABEL_OBS);
        commandstream_permissions.update = new ItemPermission(api_update, NAME_CONTROLS, LABEL_CONTROLS);
        command_permissions.update = new ItemPermission(api_update, NAME_COMMANDS, LABEL_COMMANDS);
        command_status_permissions.update = new ItemPermission(api_update, NAME_COMMANDS_STATUS, LABEL_COMMANDS_STATUS);
        
        api_delete = new ItemPermission(rootPerm, "delete");
        procedure_permissions.delete = new ItemPermission(api_delete, NAME_PROCEDURE, LABEL_PROCEDURE);
        property_permissions.delete = new ItemPermission(api_delete, NAME_PROPERTY, LABEL_PROPERTY);
        system_permissions.delete = new ItemPermission(api_delete, NAME_SYSTEM, LABEL_SYSTEM);
        deployment_permissions.delete = new ItemPermission(api_delete, NAME_DEPLOYMENT, LABEL_DEPLOYMENT);
        foi_permissions.delete = new ItemPermission(api_delete, NAME_FOI, LABEL_FOI);
        datastream_permissions.delete = new ItemPermission(api_delete, NAME_DATASTREAM, LABEL_DATASTREAM);
        obs_permissions.delete = new ItemPermission(api_delete, NAME_OBS, LABEL_OBS);
        commandstream_permissions.delete = new ItemPermission(api_delete, NAME_CONTROLS, LABEL_CONTROLS);
        command_permissions.delete = new ItemPermission(api_delete, NAME_COMMANDS, LABEL_COMMANDS);
        command_status_permissions.delete = new ItemPermission(api_delete, NAME_COMMANDS_STATUS, LABEL_COMMANDS_STATUS);
        
        // wildcard permissions
        api_allOps = new WildcardPermission(rootPerm, "All Ops");
        procedure_permissions.allOps = new ItemPermission(api_allOps, NAME_PROCEDURE, LABEL_PROCEDURE);
        property_permissions.allOps = new ItemPermission(api_allOps, NAME_PROPERTY, LABEL_PROPERTY);
        system_permissions.allOps = new ItemPermission(api_allOps, NAME_SYSTEM, LABEL_SYSTEM);
        deployment_permissions.allOps = new ItemPermission(api_allOps, NAME_DEPLOYMENT, LABEL_DEPLOYMENT);
        foi_permissions.allOps = new ItemPermission(api_allOps, NAME_FOI, LABEL_FOI);
        datastream_permissions.allOps = new ItemPermission(api_allOps, NAME_DATASTREAM, LABEL_DATASTREAM);
        obs_permissions.allOps = new ItemPermission(api_allOps, NAME_OBS, LABEL_OBS);
        commandstream_permissions.allOps = new ItemPermission(api_allOps, NAME_CONTROLS, LABEL_CONTROLS);
        command_permissions.allOps = new ItemPermission(api_allOps, NAME_COMMANDS, LABEL_COMMANDS);
        command_status_permissions.allOps = new ItemPermission(api_allOps, NAME_COMMANDS_STATUS, LABEL_COMMANDS_STATUS);
        
        
        // register wildcard permission tree usable for all SWE API services
        // do it at this point so we don't include specific offering permissions
        ModulePermissions wildcardPerm = rootPerm.cloneAsTemplatePermission("ConSys API Services");
        service.getParentHub().getSecurityManager().registerModulePermissions(wildcardPerm);
        
        // register permission tree
        service.getParentHub().getSecurityManager().registerModulePermissions(rootPerm);
    }
    
    
    protected List<String> appendSystemParents(final List<String> parentIds, final BigId sysId)
    {
        var idEncoder = service.getParentHub().getIdEncoders().getSystemIdEncoder();
        BigId parentId = sysId;
        
        while ((parentId = db.getSystemDescStore().getParent(parentId)) != null)
            parentIds.add(idEncoder.encodeID(parentId));
        
        return parentIds;
    }
    
    
    protected List<String> appendFoiParents(final List<String> parentIds, final BigId foiId)
    {
        var sysId = db.getFoiStore().getParent(foiId);
        
        var idEncoder = service.getParentHub().getIdEncoders().getSystemIdEncoder();
        parentIds.add(idEncoder.encodeID(sysId));
        appendSystemParents(parentIds, sysId);
        
        return parentIds;
    }
    
    
    protected void appendDataStreamParents(final List<String> parentIds, final BigId dsId)
    {
        var dsInfo = db.getDataStreamStore().get(new DataStreamKey(dsId));
        if (dsInfo == null)
            throw new IllegalArgumentException();
        
        var idEncoder = service.getParentHub().getIdEncoders().getSystemIdEncoder();
        var sysId = dsInfo.getSystemID().getInternalID();
        parentIds.add(idEncoder.encodeID(sysId));
        appendSystemParents(parentIds, sysId);
    }
    
    
    protected void appendCommandStreamParents(final List<String> parentIds, final BigId csId)
    {
        var csInfo = db.getCommandStreamStore().get(new CommandStreamKey(csId));
        if (csInfo == null)
            throw new IllegalArgumentException();
        
        var idEncoder = service.getParentHub().getIdEncoders().getSystemIdEncoder();
        var sysId = csInfo.getSystemID().getInternalID();
        parentIds.add(idEncoder.encodeID(sysId));
        appendSystemParents(parentIds, sysId);
    }
    
    
    protected Collection<String> getParentIds(IPermission perm)
    {
        /*parentsIds = dsParentsCache.get(dsId, () -> {
        var parentIds = new ArrayList<String>();
        appendDataStreamParents(parentIds, dsId);
        return parentIds;
        });*/
        
        var parentIds = new ArrayList<String>();
        
        // retrieve ids of resource parents recursively
        // special case for create permission, since the id is the parent in this case
        if (perm instanceof ItemWithParentPermission)
        {
            var resourceType = ((ItemWithParentPermission) perm).getResourceType();
            var id = ((ItemWithParentPermission) perm).getParentId();
            parentIds.add(id);
            
            if (resourceType.equals(NAME_OBS))
            {
                var dsId = service.getParentHub().getIdEncoders().getDataStreamIdEncoder().decodeID(id);
                appendDataStreamParents(parentIds, dsId);
            }
            else if (resourceType.equals(NAME_COMMANDS))
            {
                var csId = service.getParentHub().getIdEncoders().getCommandStreamIdEncoder().decodeID(id);
                appendCommandStreamParents(parentIds, csId);
            }
            else 
            {
                var sysId = service.getParentHub().getIdEncoders().getSystemIdEncoder().decodeID(id);
                appendSystemParents(parentIds, sysId);
            }
        }
        else if (perm instanceof ItemWithIdPermission)
        {
            var resourceType = ((ItemWithIdPermission) perm).getResourceType();
            var id = ((ItemWithIdPermission) perm).getId();
            
            if (resourceType.equals(NAME_SYSTEM))
            {
                var sysId = service.getParentHub().getIdEncoders().getSystemIdEncoder().decodeID(id);
                appendSystemParents(parentIds, sysId);
            }
            else if (resourceType.equals(NAME_FOI))
            {
                var foiId = service.getParentHub().getIdEncoders().getFoiIdEncoder().decodeID(id);
                appendFoiParents(parentIds, foiId);
            }
            else if (resourceType.equals(NAME_DATASTREAM))
            {
                var dsId = service.getParentHub().getIdEncoders().getDataStreamIdEncoder().decodeID(id);
                appendDataStreamParents(parentIds, dsId);
            }
            else if (resourceType.equals(NAME_CONTROLS))
            {
                var csId = service.getParentHub().getIdEncoders().getCommandStreamIdEncoder().decodeID(id);
                appendCommandStreamParents(parentIds, csId);
            }
        }
        
        return parentIds;
    }
    
    
    Cache<BigId, List<String>> dsParentsCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();
    
    
    @Override
    public boolean hasPermission(IPermission perm)
    {
        if (super.hasPermission(perm))
            return true;
        
        // check if permission is inherited from any of the ancestors
        for (var parentId: getParentIds(perm))
        {
            IPermission parentPerm;
            
            if (perm instanceof ItemWithParentPermission)
            {
                var perm2 = new ItemPermission(perm.getParent(), ((ItemWithParentPermission) perm).getResourceType());
                parentPerm = new ItemWithParentPermission(perm2, parentId);
            }
            else
                parentPerm = new ItemWithParentPermission(perm.getParent(), parentId);
            
            if (super.hasPermission(parentPerm))
                return true;
        }
        
        return false;
    }
    
    
    @Override
    public void checkResourcePermission(IPermission perm, String id) throws IOException
    {
        try
        {
            var idPerm = id == null ? perm : new ItemWithIdPermission(perm, id);
            super.checkPermission(idPerm);
        }
        catch (IllegalArgumentException e)
        {
            throw ServiceErrors.notFound(id);
        }
    }
    
    
    @Override
    public void checkParentPermission(IPermission perm, String parentId) throws IOException
    {
        try
        {
            var parentPerm = parentId == null ? perm : new ItemWithParentPermission(perm, parentId);
            super.checkPermission(parentPerm);
        }
        catch (IllegalArgumentException e)
        {
            throw ServiceErrors.notFound(parentId);
        }
    }
}
