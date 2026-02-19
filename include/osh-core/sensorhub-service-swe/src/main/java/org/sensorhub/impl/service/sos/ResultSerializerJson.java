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
import org.vast.data.JSONEncodingImpl;
import org.vast.ows.OWSUtils;
import org.vast.ows.sos.GetResultRequest;
import org.vast.ows.sos.SOSException;


/**
 * <p>
 * Result serializer implementation for JSON format
 * </p>
 *
 * @author Alex Robin
 * @date Apr 5, 2020
 */
public class ResultSerializerJson extends AbstractResultSerializerSwe
{

    @Override
    public void init(SOSServlet servlet, AsyncContext asyncCtx, GetResultRequest req, RecordTemplate resultTemplate) throws SOSException, IOException
    {
        if (!allowNonBinaryFormat(resultTemplate))
            throw new SOSException(SOSException.invalid_param_code, "responseFormat",
                req.getFormat(), UNSUPPORTED_FORMAT + req.getFormat());
        
        resultTemplate = new RecordTemplate(resultTemplate.getDataStructure(), new JSONEncodingImpl());
        super.init(servlet, asyncCtx, req, resultTemplate);
        
        if (asyncCtx != null)
            asyncCtx.getResponse().setContentType(OWSUtils.JSON_MIME_TYPE);
    }
    
    
    @Override
    protected void beforeRecords() throws IOException
    {
        writer.startStream(multipleRecords);
    }
    
    
    @Override
    protected void writeRecord(ObsEvent item) throws IOException
    {
        super.writeRecord(item);
        if (!multipleRecords)
            os.write('\n');
    }
    

    @Override
    protected void afterRecords() throws IOException
    {
        writer.endStream();
        writer.flush();
    }

}
