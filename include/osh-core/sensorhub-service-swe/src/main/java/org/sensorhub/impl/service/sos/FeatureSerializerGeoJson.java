/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sos;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import javax.servlet.AsyncContext;
import org.vast.ogc.gml.GeoJsonBindings;
import org.vast.ogc.gml.IFeature;
import org.vast.ows.sos.GetFeatureOfInterestRequest;
import com.google.gson.stream.JsonWriter;


/**
 * <p>
 * Feature serializer implementation for GeoJSON format
 * </p>
 *
 * @author Alex Robin
 * @date Nov 25, 2020
 */
public class FeatureSerializerGeoJson extends AbstractAsyncSerializer<GetFeatureOfInterestRequest, IFeature> implements ISOSAsyncFeatureSerializer
{
    GeoJsonBindings geoJsonBindings;
    JsonWriter jsonWriter;
    
    
    @Override
    public void init(SOSServlet servlet, AsyncContext asyncCtx, GetFeatureOfInterestRequest request) throws IOException
    {
        super.init(servlet, asyncCtx, request);
        
        // init JSON writer and bindings
        try
        {
            asyncCtx.getResponse().setContentType(GeoJsonBindings.MIME_TYPE);
            var osw = new OutputStreamWriter(os, StandardCharsets.UTF_8.name());
            jsonWriter = new JsonWriter(osw);
            jsonWriter.setLenient(true);
            jsonWriter.setSerializeNulls(false);
            jsonWriter.setIndent("  ");
            
            // prepare GeoJson writer
            geoJsonBindings = new GeoJsonBindings(true);
        }
        catch (IOException e)
        {
            throw new IOException("Cannot create JSON writer", e);
        }
    }
    

    @Override
    protected void beforeRecords() throws IOException
    {
        jsonWriter.beginArray();
    }
    
    
    @Override
    protected void writeRecord(IFeature foi) throws IOException
    {
        try
        {
            geoJsonBindings.writeFeature(jsonWriter, foi);
        }
        catch (IOException e)
        {
            throw new IOException("Error writing feature", e);
        }
    }
    

    @Override
    protected void afterRecords() throws IOException
    {
        jsonWriter.endArray();
        jsonWriter.flush();
    }
}
