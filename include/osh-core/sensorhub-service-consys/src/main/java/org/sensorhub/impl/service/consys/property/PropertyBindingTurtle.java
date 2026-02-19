/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.property;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.datastore.property.PropertyKey;
import org.sensorhub.api.semantic.IDerivedProperty;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.resource.ResourceBinding;
import org.sensorhub.impl.service.consys.resource.ResourceLink;


public class PropertyBindingTurtle extends ResourceBinding<PropertyKey, IDerivedProperty>
{
    final String rootURL;
    
    
    public PropertyBindingTurtle(RequestContext ctx, IdEncoders idEncoders, boolean forReading) throws IOException
    {
        super(ctx, idEncoders);
        this.rootURL = ctx.getApiRootURL();
    }


    @Override
    public IDerivedProperty deserialize() throws IOException
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public void serialize(PropertyKey key, IDerivedProperty prop, boolean showLinks) throws IOException
    {
        var conceptUri = prop.getURI();
        if (conceptUri.startsWith("#"))
            conceptUri = ctx.getApiRootURL() + "/" + PropertyHandler.NAMES[0] + "/" + conceptUri.substring(1);
        
        try (var writer = new OutputStreamWriter(ctx.getOutputStream()))
        {
            writer.append("@prefix qudt: <http://qudt.org/schema/qudt/> .\n");
            writer.append("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n");
            writer.append("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n");
            writer.append("@prefix skos: <http://www.w3.org/2004/02/skos/core#> .\n");
            writer.append('\n');
            
            writer.append('<').append(conceptUri).append('>')
                  .append(" rdf:type <http://qudt.org/schema/qudt/QuantityKind> ;\n");
            
            writer.append("  rdfs:label \"").append(prop.getName()).append("\" ;\n");
            
            if (prop.getDescription() != null)
                writer.append("  skos:definition \"").append(prop.getDescription()).append("\" ;\n");
            
            writer.append("  skos:broader <").append(prop.getBaseProperty()).append("> ;\n");
        }
    }


    @Override
    public void startCollection() throws IOException
    {
        
    }


    @Override
    public void endCollection(Collection<ResourceLink> links) throws IOException
    {
    }
    
    
}
