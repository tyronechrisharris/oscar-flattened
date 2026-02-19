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
import javax.servlet.AsyncContext;
import org.sensorhub.api.data.ObsEvent;
import org.sensorhub.impl.service.swe.RecordTemplate;
import org.sensorhub.impl.service.swe.SWEServlet;
import org.vast.cdm.common.DataStreamWriter;
import org.vast.ows.sos.GetResultRequest;
import org.vast.ows.sos.SOSException;
import org.vast.swe.AbstractDataWriter;
import org.vast.swe.FilteredWriter;
import org.vast.swe.SWEConstants;
import org.vast.swe.SWEHelper;
import org.vast.swe.fast.DataBlockProcessor;
import org.vast.swe.fast.FilterByDefinition;
import org.vast.util.Asserts;
import net.opengis.swe.v20.BinaryBlock;
import net.opengis.swe.v20.BinaryEncoding;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;


/**
 * <p>
 * Abstract base class for all SWE common format serializers
 * </p>
 *
 * @author Alex Robin
 * @date Apr 5, 2020
 */
public abstract class AbstractResultSerializerSwe extends AbstractAsyncSerializer<GetResultRequest, ObsEvent> implements ISOSAsyncResultSerializer
{
    static final String UNSUPPORTED_FORMAT = SWEServlet.INVALID_RESPONSE_FORMAT;
    
    DataStreamWriter writer;
    boolean multipleRecords;
    
    
    @Override
    public void init(SOSServlet servlet, AsyncContext asyncCtx, GetResultRequest request) throws IOException
    {
        Asserts.checkNotNull(null, RecordTemplate.class); // throw exception
    }
    
    
    @Override
    public void init(SOSServlet servlet, AsyncContext asyncCtx, GetResultRequest request, RecordTemplate resultTemplate) throws SOSException, IOException
    {
        super.init(servlet, asyncCtx, request);
        Asserts.checkNotNull(resultTemplate, RecordTemplate.class);
        var resultStructure = Asserts.checkNotNull(resultTemplate.getDataStructure(), DataComponent.class);
        var resultEncoding = Asserts.checkNotNull(resultTemplate.getDataEncoding(), DataEncoding.class);
        
        try
        {
            multipleRecords = !SOSProviderUtils.isWebSocketRequest(request) &&
                              !SOSProviderUtils.isStreamingRequest(request);
                        
            // prepare writer for selected encoding
            writer = SWEHelper.createDataWriter(resultEncoding);
            
            // if only certain observables are selected, we need to filter here
            // in case data provider hasn't modified the datablocks
            if (!request.getObservables().isEmpty())
            {
                // always keep sampling time
                request.getObservables().add(SWEConstants.DEF_SAMPLING_TIME);
                request.getObservables().add(SWEConstants.DEF_PHENOMENON_TIME);
    
                // temporary hack to switch btw old and new writer architecture
                if (writer instanceof AbstractDataWriter)
                    writer = new FilteredWriter((AbstractDataWriter)writer, request.getObservables());
                else
                    ((DataBlockProcessor)writer).setDataComponentFilter(new FilterByDefinition(request.getObservables()));
            }
            
            writer.setDataComponents(resultStructure);
            writer.setOutput(os);
        }
        catch (IOException e)
        {
            onError(e);
        }
    }
    
    
    @Override
    protected void writeRecord(ObsEvent item) throws IOException
    {
        for (var obs: item.getObservations())
        {
            writer.write(obs.getResult());
            if (!multipleRecords)
                writer.flush();
        }
    }
    
    
    protected boolean allowNonBinaryFormat(RecordTemplate resultTemplate)
    {
        if (resultTemplate.getDataEncoding() instanceof BinaryEncoding)
        {
            var enc = (BinaryEncoding)resultTemplate.getDataEncoding();
            for (var member: enc.getMemberList())
            {
                if (member instanceof BinaryBlock)
                    return false;
            }
        }
        
        return true;
    }
}
