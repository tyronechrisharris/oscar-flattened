/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sps;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.AsyncContext;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.vast.ogc.OGCRegistry;
import org.vast.ogc.om.IProcedure;
import org.vast.ows.OWSRequest;
import org.vast.ows.OWSUtils;
import org.vast.ows.sos.SOSUtils;
import org.vast.ows.swe.DescribeSensorRequest;
import org.vast.sensorML.SMLStaxBindings;
import org.vast.util.Asserts;
import org.vast.xml.IndentingXMLStreamWriter;
import org.vast.xml.XMLImplFinder;
import net.opengis.sensorml.v20.AbstractProcess;


/**
 * <p>
 * Procedure serializer implementation for SensorML XML format
 * </p>
 *
 * @author Alex Robin
 * @date Apr 5, 2020
 */
public class ProcedureSerializerXml
{
    private static final String DEFAULT_VERSION = "2.0.0";
    private static final String SWES_PREFIX = "swe";
    private static final String SWES_NS_URI = OGCRegistry.getNamespaceURI(SOSUtils.SWES, DEFAULT_VERSION);
    
    SPSServlet servlet;
    DescribeSensorRequest request;
    SMLStaxBindings smlBindings;
    XMLStreamWriter writer;
    
    
    public ProcedureSerializerXml(SPSServlet servlet, DescribeSensorRequest request, AsyncContext asyncCtx) throws IOException
    {
        this.servlet = Asserts.checkNotNull(servlet, SPSServlet.class);
        this.request = Asserts.checkNotNull(request, OWSRequest.class);
        var os = asyncCtx.getResponse().getOutputStream();
        
        // init XML stream writer and bindings
        try
        {
            XMLOutputFactory factory = XMLImplFinder.getStaxOutputFactory();
            XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(os, StandardCharsets.UTF_8.name());
            asyncCtx.getResponse().setContentType(OWSUtils.XML_MIME_TYPE);
            writer = new IndentingXMLStreamWriter(xmlWriter);
            
            smlBindings = new SMLStaxBindings();
            smlBindings.setNamespacePrefixes(writer);
            smlBindings.declareNamespacesOnRootElement();
        }
        catch (XMLStreamException e)
        {
            throw new IOException("Cannot create XML stream writer", e);
        }
    }
    

    protected void beforeRecords() throws IOException
    {
        try
        {
            // wrap with SOAP envelope if requested
            servlet.startSoapEnvelope(request, writer);
            
            writer.writeStartElement(SWES_PREFIX, "DescribeSensorResponse", SWES_NS_URI);
            writer.writeNamespace(SWES_PREFIX, SWES_NS_URI);
            
            writer.writeStartElement(SWES_PREFIX, "procedureDescriptionFormat", SWES_NS_URI);
            writer.writeCharacters(DescribeSensorRequest.DEFAULT_FORMAT);
            writer.writeEndElement();
        }
        catch (XMLStreamException e)
        {
            throw new IOException("Error starting XML response", e);
        }
    }
    
    
    protected void writeRecord(IProcedure proc) throws IOException
    {
        try
        {
            writer.writeStartElement(SWES_PREFIX, "description", SWES_NS_URI);
            writer.writeStartElement(SWES_PREFIX, "SensorDescription", SWES_NS_URI);
            writer.writeStartElement(SWES_PREFIX, "data", SWES_NS_URI);
            
            smlBindings.writeAbstractProcess(writer, (AbstractProcess)proc);
            
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();
        }
        catch (XMLStreamException e)
        {
            throw new IOException("Error writing XML procedure description", e);
        }
    }
    

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
