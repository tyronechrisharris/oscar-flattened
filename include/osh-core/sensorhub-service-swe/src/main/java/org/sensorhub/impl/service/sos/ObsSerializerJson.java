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
import org.vast.json.JsonStreamWriter;
import org.vast.ows.OWSUtils;
import org.vast.ows.sos.GetObservationRequest;
import org.vast.swe.json.SWEJsonStreamWriter;
import org.vast.xml.DOMHelper;
import org.w3c.dom.Element;


/**
 * <p>
 * Observation serializer implementation for O&M JSON format
 * </p>
 *
 * @author Alex Robin
 * @date Apr 5, 2020
 */
public class ObsSerializerJson extends AbstractObsSerializerStax implements ISOSAsyncObsSerializer
{
        
    @Override
    public void init(SOSServlet servlet, AsyncContext asyncCtx, GetObservationRequest request) throws IOException
    {
        super.init(servlet, asyncCtx, request);
        
        // init JSON stream writer
        asyncCtx.getResponse().setContentType(OWSUtils.JSON_MIME_TYPE);
        writer = new SWEJsonStreamWriter(os, StandardCharsets.UTF_8);
    }
    

    @Override
    protected void beforeRecords() throws IOException
    {
        ((JsonStreamWriter)writer).beginArray();
    }
    
    
    @Override
    protected void serializeObsElement(DOMHelper dom, Element obsElt) throws XMLStreamException
    {
        dom.writeToStreamWriter(obsElt, writer);
        writer.flush();
        ((JsonStreamWriter)writer).resetContext();
    }
    

    @Override
    protected void afterRecords() throws IOException
    {
        ((JsonStreamWriter)writer).endArray();
    }
}
