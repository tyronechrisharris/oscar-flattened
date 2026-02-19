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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.sensorhub.impl.service.swe.RecordTemplate;
import org.vast.ogc.OGCRegistry;
import org.vast.ows.OWSUtils;
import org.vast.ows.sos.DataStructFilter;
import org.vast.ows.sos.GetResultTemplateRequest;
import org.vast.ows.sos.SOSUtils;
import org.vast.swe.SWEConstants;
import org.vast.swe.SWEStaxBindings;
import org.vast.xml.IndentingXMLStreamWriter;
import org.vast.xml.XMLImplFinder;
import net.opengis.swe.v20.DataComponent;


/**
 * <p>
 * Result template serializer implementation for SWE JSON format
 * </p>
 *
 * @author Alex Robin
 * @date Nov 29, 2020
 */
public class ResultTemplateSerializerXml extends AbstractAsyncSerializerStax<GetResultTemplateRequest, RecordTemplate> implements ISOSAsyncResultTemplateSerializer
{
    private static final String DEFAULT_VERSION = "2.0.0";
    private static final String SOS_PREFIX = "sos";
    private static final String SOS_NS_URI = OGCRegistry.getNamespaceURI(SOSUtils.SOS, DEFAULT_VERSION);
    
    SWEStaxBindings sweBindings;
    
    
    @Override
    public void init(SOSServlet servlet, AsyncContext asyncCtx, GetResultTemplateRequest request) throws IOException
    {
        super.init(servlet, asyncCtx, request);
        
        // init XML stream writer and bindings
        try
        {
            XMLOutputFactory factory = XMLImplFinder.getStaxOutputFactory();
            XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(os, StandardCharsets.UTF_8.name());
            asyncCtx.getResponse().setContentType(OWSUtils.XML_MIME_TYPE);
            writer = new IndentingXMLStreamWriter(xmlWriter);
            
            sweBindings = new SWEStaxBindings();
            sweBindings.setNamespacePrefixes(writer);
            sweBindings.declareNamespacesOnRootElement();
        }
        catch (XMLStreamException e)
        {
            throw new IOException("Cannot create XML stream writer", e);
        }        
    }
    

    @Override
    protected void beforeRecords() throws IOException
    {
        try
        {
            // wrap with SOAP envelope if requested
            servlet.startSoapEnvelope(request, writer);
            
            writer.writeStartElement(SOS_PREFIX, "GetResultTemplateResponse", SOS_NS_URI);
            writer.writeNamespace(SOS_PREFIX, SOS_NS_URI);
            sweBindings.writeNamespaces(writer);
        }
        catch (XMLStreamException e)
        {
            throw new IOException("Error starting XML response", e);
        }
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
            
            // write response
            writer.writeStartElement(SOS_PREFIX, "resultStructure", SOS_NS_URI);            
            sweBindings.writeDataComponent(writer, dataStruct, false);
            writer.writeEndElement();
            
            writer.writeStartElement(SOS_PREFIX, "resultEncoding", SOS_NS_URI);            
            sweBindings.writeAbstractEncoding(writer, resultTemplate.getDataEncoding());
            writer.writeEndElement();            
        }
        catch (XMLStreamException e)
        {
            throw new IOException("Error writing XML result template", e);
        }
    }
    

    @Override
    protected void afterRecords() throws IOException
    {
        try
        {
            writer.writeEndElement();
            servlet.endSoapEnvelope(request, writer); 
            writer.writeEndDocument();
        }
        catch (XMLStreamException e)
        {
            throw new IOException("Error ending XML response", e);
        }
    }
}
