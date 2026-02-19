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

import java.util.concurrent.Flow.Subscriber;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import org.sensorhub.api.event.Event;
import org.sensorhub.api.event.IEventPublisher;


/**
 * <p>
 * Publisher wrapper for filtering events using the provided predicate.<br/>
 * For instance, this is used to extract events from one or more sources from
 * a publisher group channel. 
 * </p>
 *
 * @author Alex Robin
 * @date Mar 22, 2019
 */
public class FilteredEventPublisherWrapper implements IEventPublisher
{
    String sourceID;
    IEventPublisher wrappedPublisher;
    Predicate<Event> filter;
    

    public FilteredEventPublisherWrapper(IEventPublisher wrappedPublisher, String sourceID, final Predicate<Event> filter)
    {
        this.sourceID = sourceID;
        this.wrappedPublisher = wrappedPublisher;
        this.filter = filter;
    }


    @Override
    public void subscribe(Subscriber<? super Event> subscriber)
    {
        /*if (wrappedPublisher instanceof FilteredSubmissionPublisherV10)
            ((FilteredSubmissionPublisherV10<Event>)wrappedPublisher).subscribe(subscriber, filter);
        else if (wrappedPublisher instanceof FilteredSubmissionPublisherV11)
            ((FilteredSubmissionPublisherV11<Event>)wrappedPublisher).subscribe(subscriber, filter);
        else*/
            wrappedPublisher.subscribe(new FilteredSubscriber<Event>(subscriber, filter));
    }


    @Override
    public int getNumberOfSubscribers()
    {
        return wrappedPublisher.getNumberOfSubscribers();
    }


    @Override
    public void publish(Event e)
    {
        wrappedPublisher.publish(e);
    }


    @Override
    public void publish(Event e, BiPredicate<Subscriber<? super Event>, ? super Event> onDrop)
    {
        wrappedPublisher.publish(e, onDrop);
    }

}
