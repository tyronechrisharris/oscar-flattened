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
import java.nio.charset.StandardCharsets;
import javax.servlet.AsyncContext;
import javax.xml.stream.XMLStreamException;
import org.sensorhub.impl.service.swe.RecordTemplate;
import org.vast.ows.OWSUtils;
import org.vast.ows.sos.DataStructFilter;
import org.vast.ows.sos.GetResultTemplateRequest;
import org.vast.swe.SWEConstants;
import org.vast.swe.SWEStaxBindings;
import org.vast.swe.json.SWEJsonStreamWriter;
import net.opengis.swe.v20.DataComponent;


/**
 * <p>
 * Result template serializer implementation for SWE JSON format
 * </p>
 *
 * @author Alex Robin
 * @date Nov 29, 2020
 */
public class ResultTemplateSerializerJson extends AbstractAsyncSerializerStax<GetResultTemplateRequest, RecordTemplate> implements ISOSAsyncResultTemplateSerializer
{
    SWEStaxBindings sweBindings;
    
    
    @Override
    public void init(SOSServlet servlet, AsyncContext asyncCtx, GetResultTemplateRequest request) throws IOException
    {
        super.init(servlet, asyncCtx, request);
        
        // init JSON stream writer
        writer = new SWEJsonStreamWriter(os, StandardCharsets.UTF_8);
        sweBindings = new SWEStaxBindings();
            
        if (asyncCtx != null)
            asyncCtx.getResponse().setContentType(OWSUtils.JSON_MIME_TYPE);        
    }
    

    @Override
    protected void beforeRecords() throws IOException
    {
    }
    
    
    @Override
    protected void writeRecord(RecordTemplate resultTemplate) throws IOException
    {
        try
        {
            DataComponent dataStruct = resultTemplate.getDataStructure().copy();
            
            if (!request.getObservables().isEmpty())
            {
                // build filtered component tree, always keeping sampling time
                request.getObservables().add(SWEConstants.DEF_SAMPLING_TIME);
                request.getObservables().add(SWEConstants.DEF_PHENOMENON_TIME);
                dataStruct.accept(new DataStructFilter(request.getObservables()));
            }
            
            sweBindings.writeDataComponent(writer, dataStruct, false);
        }
        catch (XMLStreamException e)
        {
            throw new IOException("Error writing JSON result template", e);
        }
    }
    

    @Override
    protected void afterRecords() throws IOException
    {
    }
}
