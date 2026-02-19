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
import java.util.Map.Entry;
import javax.servlet.AsyncContext;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.vast.ogc.OGCRegistry;
import org.vast.ows.OWSUtils;
import org.vast.ows.sos.GetObservationRequest;
import org.vast.ows.sos.SOSUtils;
import org.vast.xml.DOMHelper;
import org.vast.xml.IndentingXMLStreamWriter;
import org.vast.xml.XMLImplFinder;
import org.w3c.dom.Element;


/**
 * <p>
 * Observation serializer implementation for O&M XML format
 * </p>
 *
 * @author Alex Robin
 * @date Apr 5, 2020
 */
public class ObsSerializerXml extends AbstractObsSerializerStax implements ISOSAsyncObsSerializer
{
    private static final String SOS_PREFIX = "sos";
    private static final String SOS_NS_URI = OGCRegistry.getNamespaceURI(SOSUtils.SOS, DEFAULT_VERSION);
    
    
    @Override
    public void init(SOSServlet servlet, AsyncContext asyncCtx, GetObservationRequest request) throws IOException
    {
        super.init(servlet, asyncCtx, request);
        
        // init XML stream writer
        try
        {
            XMLOutputFactory factory = XMLImplFinder.getStaxOutputFactory();
            XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(os, StandardCharsets.UTF_8.name());
            asyncCtx.getResponse().setContentType(OWSUtils.XML_MIME_TYPE);
            writer = new IndentingXMLStreamWriter(xmlWriter);
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
            // start XML response
            writer.writeStartDocument();
            
            // wrap with SOAP envelope if needed
            servlet.startSoapEnvelope(request, writer);
            
            // wrap all observations inside response
            writer.writeStartElement(SOS_PREFIX, "GetObservationResponse", SOS_NS_URI);
            writer.writeNamespace(SOS_PREFIX, SOS_NS_URI);
            
            firstObs = true;
        }
        catch (XMLStreamException e)
        {
            throw new IOException("Error starting XML document", e);
        }
    }
    
    
    @Override
    protected void serializeObsElement(DOMHelper dom, Element obsElt) throws XMLStreamException
    {
        // write common namespaces on root element
        if (firstObs)
        {
            for (Entry<String, String> nsDef: dom.getXmlDocument().getNSTable().entrySet())
                writer.writeNamespace(nsDef.getKey(), nsDef.getValue());        
            firstObs = false;
        }
        
        writer.writeStartElement(SOS_PREFIX, "observationData", SOS_NS_URI);
        dom.writeToStreamWriter(obsElt, writer);
        writer.writeEndElement();
        writer.flush();
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
            throw new IOException("Error ending XML document", e);
        }
    }
}
