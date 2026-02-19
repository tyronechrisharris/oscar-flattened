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
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.vast.ogc.OGCRegistry;
import org.vast.ogc.gml.GMLStaxBindings;
import org.vast.ogc.gml.IFeature;
import org.vast.ows.OWSUtils;
import org.vast.ows.sos.GetFeatureOfInterestRequest;
import org.vast.ows.sos.SOSUtils;
import org.vast.sensorML.SMLStaxBindings;
import org.vast.xml.IndentingXMLStreamWriter;
import org.vast.xml.XMLImplFinder;


/**
 * <p>
 * Feature serializer implementation for GML format
 * </p>
 *
 * @author Alex Robin
 * @date Nov 25, 2020
 */
public class FeatureSerializerGml extends AbstractAsyncSerializerStax<GetFeatureOfInterestRequest, IFeature> implements ISOSAsyncFeatureSerializer
{
    private static final String DEFAULT_VERSION = "2.0.0";
    private static final String SOS_PREFIX = "sos";
    private static final String SOS_NS_URI = OGCRegistry.getNamespaceURI(SOSUtils.SOS, DEFAULT_VERSION);
    
    GMLStaxBindings gmlBindings;
    boolean firstRecord;
    
    
    @Override
    public void init(SOSServlet servlet, AsyncContext asyncCtx, GetFeatureOfInterestRequest request) throws IOException
    {
        super.init(servlet, asyncCtx, request);
        
        // init XML stream writer and bindings
        try
        {
            XMLOutputFactory factory = XMLImplFinder.getStaxOutputFactory();
            XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(os, StandardCharsets.UTF_8.name());
            asyncCtx.getResponse().setContentType(OWSUtils.XML_MIME_TYPE);
            writer = new IndentingXMLStreamWriter(xmlWriter);
            
            // prepare GML features writing
            gmlBindings = new GMLStaxBindings();
            gmlBindings.registerFeatureBinding(new SMLStaxBindings());
            gmlBindings.declareNamespacesOnRootElement();
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

            // wrap with SOAP envelope if requested
            servlet.startSoapEnvelope(request, writer);
            
            // write response root element
            String sosNsUri = OGCRegistry.getNamespaceURI(SOSUtils.SOS, DEFAULT_VERSION);
            writer.writeStartElement(SOS_PREFIX, "GetFeatureOfInterestResponse", sosNsUri);
            writer.writeNamespace(SOS_PREFIX, sosNsUri);
            gmlBindings.writeNamespaces(writer);
            
            firstRecord = true;
        }
        catch (XMLStreamException e)
        {
            throw new IOException("Error starting XML document", e);
        }
    }
    
    
    @Override
    protected void writeRecord(IFeature foi) throws IOException
    {
        try
        {
            // write namespace on root because in many cases it is common to all features
            if (firstRecord)
            {
                gmlBindings.ensureNamespaceDecl(writer, GMLStaxBindings.getFeatureQName(foi));
                for (Entry<QName, Object> prop: foi.getProperties().entrySet())
                    gmlBindings.ensureNamespaceDecl(writer, prop.getKey());
                firstRecord = false;
            }

            writer.writeStartElement(SOS_NS_URI, "featureMember");
            gmlBindings.writeGenericFeature(writer, foi);
            writer.writeEndElement();
            writer.flush();
        }
        catch (XMLStreamException e)
        {
            throw new IOException("Error writing feature", e);
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
            throw new IOException("Error ending XML document", e);
        }
    }
}
