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
import org.vast.ogc.om.IProcedure;
import org.vast.ows.OWSUtils;
import org.vast.ows.swe.DescribeSensorRequest;
import org.vast.sensorML.SMLStaxBindings;
import org.vast.sensorML.json.SMLJsonStreamWriter;
import net.opengis.sensorml.v20.AbstractProcess;


/**
 * <p>
 * Procedure serializer implementation for SensorML JSON format
 * </p>
 *
 * @author Alex Robin
 * @date Apr 5, 2020
 */
public class ProcedureSerializerJson extends AbstractAsyncSerializerStax<DescribeSensorRequest, IProcedure> implements ISOSAsyncProcedureSerializer
{
    SMLStaxBindings smlBindings;
    
    
    @Override
    public void init(SOSServlet servlet, AsyncContext asyncCtx, DescribeSensorRequest request) throws IOException
    {
        super.init(servlet, asyncCtx, request);
        
        // init JSON stream writer
        asyncCtx.getResponse().setContentType(OWSUtils.JSON_MIME_TYPE);
        writer = new SMLJsonStreamWriter(os, StandardCharsets.UTF_8);
        smlBindings = new SMLStaxBindings();
    }
    

    @Override
    protected void beforeRecords() throws IOException
    {
        ((JsonStreamWriter)writer).beginArray();
    }
    
    
    @Override
    protected void writeRecord(IProcedure proc) throws IOException
    {
        try
        {
            smlBindings.writeAbstractProcess(writer, (AbstractProcess)proc);
            ((JsonStreamWriter)writer).resetContext();
        }
        catch (XMLStreamException e)
        {
            throw new IOException("Error writing JSON procedure description", e);
        }
    }
    

    @Override
    protected void afterRecords() throws IOException
    {
        ((JsonStreamWriter)writer).endArray();
    }
}
