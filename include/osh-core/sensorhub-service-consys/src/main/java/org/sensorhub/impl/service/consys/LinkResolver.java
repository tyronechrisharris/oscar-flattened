/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2023 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys;

import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.database.IDatabase;
import org.sensorhub.api.database.IFeatureDatabase;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.database.IProcedureDatabase;
import org.sensorhub.impl.service.consys.feature.FoiHandler;
import org.sensorhub.impl.service.consys.procedure.ProcedureHandler;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.system.SystemHandler;
import org.vast.ogc.xlink.IXlinkReference;


public class LinkResolver
{

    public static boolean resolveProcedureLink(final RequestContext ctx, final IXlinkReference<?> link, final IProcedureDatabase db, final IdEncoders idEncoders)
    {
        if (link == null)
            return false;
        
        synchronized(link)
        {
            // resolve URN to URL
            if (link.getHref() != null && link.getHref().startsWith("urn") && db.getProcedureStore() != null)
            {
                var urnParts = extractFragment(link.getHref());
                var urn = urnParts[0];
                var fragment = urnParts[1];
                
                var procKey = db.getProcedureStore().getCurrentVersionKey(urn);
                if (procKey != null)
                {
                    link.setTargetUID(urn);
                    var procId = idEncoders.getProcedureIdEncoder().encodeID(procKey.getInternalID());
                    link.setHref(ctx.getApiRootURL() + "/" + ProcedureHandler.NAMES[0] + "/" + procId + fragment);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    
    public static boolean resolveSystemLink(final RequestContext ctx, final IXlinkReference<?> link, final IObsSystemDatabase db, final IdEncoders idEncoders)
    {
        if (link == null)
            return false;
        
        synchronized(link)
        {
            // resolve URN to URL
            if (link.getHref() != null && link.getHref().startsWith("urn") && db.getSystemDescStore() != null)
            {
                var urnParts = extractFragment(link.getHref());
                var urn = urnParts[0];
                var fragment = urnParts[1];
                
                var sysKey = db.getSystemDescStore().getCurrentVersionKey(urn);
                if (sysKey != null)
                {
                    link.setTargetUID(urn);
                    var procId = idEncoders.getSystemIdEncoder().encodeID(sysKey.getInternalID());
                    link.setHref(ctx.getApiRootURL() + "/" + SystemHandler.NAMES[0] + "/" + procId + fragment);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    
    public static boolean resolveFeatureLink(final RequestContext ctx, final IXlinkReference<?> link, final IFeatureDatabase db, final IdEncoders idEncoders)
    {
        if (link == null)
            return false;
        
        synchronized(link)
        {
            // resolve URN to URL
            if (link.getHref() != null && link.getHref().startsWith("urn") && db.getFeatureStore() != null)
            {
                var urnParts = extractFragment(link.getHref());
                var urn = urnParts[0];
                var fragment = urnParts[1];
                
                var fKey = db.getFeatureStore().getCurrentVersionKey(urn);
                if (fKey != null)
                {
                    link.setTargetUID(urn);
                    var fid = idEncoders.getFeatureIdEncoder().encodeID(fKey.getInternalID());
                    link.setHref(ctx.getApiRootURL() + "/" + FoiHandler.NAMES[0] + "/" + fid + fragment);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    
    public static boolean resolveLink(final RequestContext ctx, final IXlinkReference<?> link, final IDatabase db, final IdEncoders idEncoders)
    {
        synchronized(link)
        {
            // resolve URN to URL
            if (link != null && link.getHref() != null && link.getHref().startsWith("urn"))
            {
                // try to resolve as System
                if (db instanceof IObsSystemDatabase && ((IObsSystemDatabase)db).getSystemDescStore() != null)
                {
                    if (resolveSystemLink(ctx, link, ((IObsSystemDatabase)db), idEncoders))
                        return true;
                }
                
                // try to resolve as Feature
                if (db instanceof IFeatureDatabase && ((IFeatureDatabase)db).getFeatureStore() != null)
                {
                    if (resolveFeatureLink(ctx, link, ((IFeatureDatabase)db), idEncoders))
                        return true;
                }
                
                // try to resolve as Procedure
                if (db instanceof IProcedureDatabase && ((IProcedureDatabase)db).getProcedureStore() != null)
                {
                    if (resolveProcedureLink(ctx, link, ((IProcedureDatabase)db), idEncoders))
                        return true;
                }
            }
        }
        
        return false;
    }
    
    
    private static String[] extractFragment(String urn)
    {
        var parts = new String[2];
        var fragmentIdx = urn.lastIndexOf('#');
        
        if (fragmentIdx > 0)
        {
            parts[0] = urn.substring(0, fragmentIdx);
            parts[1] = urn.substring(fragmentIdx);
        }
        else
        {
            parts[0] = urn;
            parts[1] = "";
        }
        
        return parts;
    }
}
