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
import java.util.concurrent.Flow.Subscriber;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.WriteListener;


/**
 * <p>
 * Base interface for all asynchronous response serializers
 * </p>
 * 
 * @param <R> The service request type
 * @param <T> The serialized item type
 *
 * @author Alex Robin
 * @date Apr 28, 2020
 */
public interface IAsyncResponseSerializer<R, T> extends Subscriber<T>, WriteListener, AsyncListener
{
    
    public void init(SOSServlet servlet, AsyncContext asyncCtx, R request) throws IOException;
}
