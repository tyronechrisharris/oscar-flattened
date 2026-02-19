/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.obs;

import java.io.IOException;
import java.util.Collection;
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.api.datastore.obs.DataStreamKey;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.resource.ResourceBindingJson;
import org.sensorhub.impl.service.consys.resource.ResourceLink;
import org.vast.data.ScalarIterator;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.opengis.swe.v20.Category;
import net.opengis.swe.v20.Count;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.HasUom;
import net.opengis.swe.v20.Quantity;
import net.opengis.swe.v20.ScalarComponent;
import net.opengis.swe.v20.Text;
import net.opengis.swe.v20.Time;
import net.opengis.swe.v20.Vector;


/**
 * <p>
 * Experimental bindings for serializing a datastream logical schema.
 * The role of the logical schema is to provide an encoding independent view
 * of the datastream contents. It is based on JSON schema standard, with
 * additional extension keywords in the "x-ogc" namespace.
 * </p>
 *
 * @author Alex Robin
 * @since Oct 6, 2023
 */
public class DataStreamSchemaBindingLogicalJsonSchema extends ResourceBindingJson<DataStreamKey, IDataStreamInfo>
{
    String rootURL;
    
    
    public DataStreamSchemaBindingLogicalJsonSchema(RequestContext ctx, IdEncoders idEncoders, boolean forReading) throws IOException
    {
        super(ctx, idEncoders, forReading);
        init(ctx, forReading);
    }
    
    
    public DataStreamSchemaBindingLogicalJsonSchema(RequestContext ctx, IdEncoders idEncoders, JsonReader reader) throws IOException
    {
        super(ctx, idEncoders, reader);
        init(ctx, true);
    }
    
    
    public DataStreamSchemaBindingLogicalJsonSchema(RequestContext ctx, IdEncoders idEncoders, JsonWriter writer) throws IOException
    {
        super(ctx, idEncoders, writer);
        init(ctx, false);
    }
    
    
    void init(RequestContext ctx, boolean forReading)
    {
        this.rootURL = ctx.getApiRootURL();
    }
    
    
    @Override
    public IDataStreamInfo deserialize(JsonReader reader) throws IOException
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public void serialize(DataStreamKey key, IDataStreamInfo dsInfo, boolean showLinks, JsonWriter writer) throws IOException
    {
        writer.beginObject();
        writer.name("type").value("object");
        
        try
        {
            writer.name("title").value(dsInfo.getName());
            
            if (dsInfo.getDescription() != null)
                writer.name("description").value(dsInfo.getDescription());
            
            writer.name("properties").beginObject();
            
            var it = new ScalarIterator(dsInfo.getRecordStructure());
            while (it.hasNext())
            {
                var f = it.next();
                writer.name(f.getName()).beginObject();
                
                if (f.getLabel() != null)
                    writer.name("title").value(f.getLabel());
                
                if (f.getDescription() != null)
                    writer.name("description").value(f.getDescription());
                
                appendFieldType(writer, f);
                
                if (f.getDefinition() != null)
                    writer.name("x-ogc-definition").value(f.getDefinition());
                
                appendRefFrame(writer, f);
                
                var uom = getUom(f);
                if (uom != null)
                    writer.name("x-ogc-unit").value(uom);
                
                writer.endObject();
            }
            
            writer.endObject();
        }
        catch (Exception e)
        {
            throw new IOException("Error writing logical JSON schema", e);
        }
        
        writer.endObject();
        writer.flush();
    }
    
    
    void appendFieldType(JsonWriter writer, DataComponent c) throws IOException
    {
        writer.name("type");
        
        if (c instanceof Quantity)
        {
            writer.value("number");
        }
        else if (c instanceof Count)
        {
            writer.value("integer");
        }
        else if (c instanceof Time)
        {
            writer.value("string");
            writer.name("format").value("date-time");
        }
        else if (c instanceof Category)
        {
            writer.value("string");
            if (((Category) c).getConstraint() != null)
            {
                var valueList = ((Category) c).getConstraint().getValueList();
                if (valueList != null)
                {
                    writer.name("enum").beginArray();
                    for (var w: valueList)
                        writer.value(w);
                    writer.endArray();
                }
                
                
            }
        }
        else if (c instanceof Text)
        {
            writer.value("string");
        }
        
        else
            writer.value("null");
    }
    
    
    void appendRefFrame(JsonWriter writer, ScalarComponent c) throws IOException
    {
        if (c.getReferenceFrame() != null)
        {
            writer.name("x-ogc-refFrame").value(c.getReferenceFrame());
        }
        else if (c.getParent() != null && c.getParent() instanceof Vector)
        {
            var refFrame = ((Vector)c.getParent()).getReferenceFrame();
            if (refFrame != null)
                writer.name("x-ogc-refFrame").value(refFrame);
        }
        
        if (c.getAxisID() != null)
            writer.name("x-ogc-axis").value(c.getAxisID());
    }
    
    
    String getUom(DataComponent c)
    {
        if (c instanceof HasUom)
        {
            var uom = ((HasUom) c).getUom();
            return uom.getCode() != null ? uom.getCode() : uom.getHref();
        }
        
        return null;
    }


    @Override
    public void startCollection() throws IOException
    {
        startJsonCollection(writer);
    }


    @Override
    public void endCollection(Collection<ResourceLink> links) throws IOException
    {
        endJsonCollection(writer, links);
    }
}
