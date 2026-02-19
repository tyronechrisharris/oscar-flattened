/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.event;

import java.util.concurrent.Executor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Predicate;


/**
 * <p>
 * Extension of {@link SubmissionPublisher} to support filtering events before
 * dispatching to clients.
 * </p>
 * <p>
 * <i>Note that this could be optimized by filtering items before they are
 * added to consumer queues, but this requires modifying SubmissionPublisher
 * internal class BufferedSubscription that is marked as final.</i>
 * </p>
 *
 * @author Alex Robin
 * @param <T> the published item type 
 * @date Feb 21, 2019
 */
public class FilteredAsyncPublisher<T> extends SubmissionPublisher<T>
{
    
    public FilteredAsyncPublisher(Executor executor, int maxBufferCapacity)
    {
        super(executor, maxBufferCapacity);
    }
    
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void subscribe(final Subscriber<? super T> subscriber, final Predicate<? super T> filter)
    {
        subscribe((Subscriber<T>)new FilteredSubscriber(subscriber, filter));        
    }
}
