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
import java.util.function.BiPredicate;
import org.sensorhub.api.event.Event;
import org.sensorhub.api.event.IEventPublisher;
import org.slf4j.Logger;


/**
 * <p>
 * Wrapper for filtered publisher also implementing IEventPublisher
 * </p>
 *
 * @author Alex Robin
 * @date Feb 21, 2019
 */
public class FilteredEventPublisher extends FilteredAsyncPublisher<Event> implements IEventPublisher
//public class FilteredEventPublisher extends FilteredSubmissionPublisherV10<Event> implements IEventPublisher
{
    String sourceID;
    Logger log;
    
    
    public FilteredEventPublisher(String sourceID, Executor executor, int maxBufferCapacity, Logger logger)
    {
        super(executor, maxBufferCapacity);
        this.sourceID = sourceID;
        this.log = logger;
    }


    @Override
    public void publish(Event e)
    {
        this.offer(e, (sub, ev) -> {
            log.warn("{} from {} dropped by subscriber {}", ev.getClass().getSimpleName(), sourceID, sub);
            return false;
        });
    }


    @Override
    public void publish(Event e, BiPredicate<Subscriber<? super Event>, ? super Event> onDrop)
    {
        this.offer(e, onDrop);
    }
}
