/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2024 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.property;

import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.database.IProcedureDatabase;
import org.sensorhub.api.semantic.IDerivedProperty;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.impl.service.consys.resource.ResourceLink;


public class PropertyAssocs
{
    public static final String REL_DERIVED_PROPS = "derivedProperties";
    
    IProcedureDatabase db;
    IdEncoders idEncoders;
    
    
    public PropertyAssocs(IProcedureDatabase db, IdEncoders idEncoders)
    {
        this.db = db;
        this.idEncoders = idEncoders;
    }
    
    
    public ResourceLink getCanonicalLink(String propId)
    {
        return new ResourceLink.Builder()
            .rel("canonical")
            .href("/" + PropertyHandler.NAMES[0] + "/" + propId)
            .type(ResourceFormat.JSON.getMimeType())
            .build();
    }
    
    
    public ResourceLink getAlternateLink(String propId, ResourceFormat format, String formatName)
    {
        return new ResourceLink.Builder()
            .rel("alternate")
            .title("This property resource in " + formatName + " format")
            .href("/" + PropertyHandler.NAMES[0] + "/" + propId)
            .withFormat(format)
            .build();
    }
    
    
    public ResourceLink getDerivedPropertiesLink(IDerivedProperty prop, ResourceFormat format)
    {
        return new ResourceLink.Builder()
            .rel(REL_DERIVED_PROPS)
            .title("Derived properties")
            .href("/" + PropertyHandler.NAMES[0] + "?baseProperty=" + prop.getURI())
            .withFormat(format)
            .build();
    }
}
