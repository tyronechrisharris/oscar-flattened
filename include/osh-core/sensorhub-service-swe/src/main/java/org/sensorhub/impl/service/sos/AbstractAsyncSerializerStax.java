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
import javax.xml.stream.XMLStreamWriter;
import org.vast.ows.OWSRequest;


/**
 * <p>
 * Base class for all asynchronous response serializers using StAX
 * </p>
 * 
 * @param <R> The service request type
 * @param <T> The serialized item type
 *
 * @author Alex Robin
 * @date Apr 5, 2020
 */
public abstract class AbstractAsyncSerializerStax<R extends OWSRequest, T> extends AbstractAsyncSerializer<R, T>
{
    protected XMLStreamWriter writer;
    
    
    @Override
    public void init(SOSServlet servlet, AsyncContext asyncCtx, R request) throws IOException
    {
        super.init(servlet, asyncCtx, request);
    }

}
