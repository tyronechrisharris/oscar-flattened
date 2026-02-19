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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicInteger;
import org.sensorhub.api.event.Event;
import org.vast.util.Asserts;


/**
 * <p>
 * Subscribes to multiple channels and delivers multiplexed events.
 * This class tries to order events by time if they are received within a
 * small enough time window.
 * </p>
 *
 * @author Alex Robin
 * @param <E> Type of events
 * @date Feb 21, 2019
 */
public class AggregateSubscription<E extends Event> implements Subscription, Subscriber<E>
{
    int numSubscriptions;
    Subscriber<? super E> subscriber;
    Collection<Subscription> subscriptions;
    AtomicInteger numCompleted = new AtomicInteger();
    //PriorityQueue<Event> queue;
    //AtomicLong numRequestedEvents = new AtomicLong();
    
    
    public AggregateSubscription(Subscriber<? super E> subscriber, int numSubscriptions)
    {
        Asserts.checkNotNull(subscriber, Subscriber.class);
        Asserts.checkArgument(numSubscriptions > 0);
        
        this.subscriber = subscriber;
        this.numSubscriptions = numSubscriptions;
        this.subscriptions = new ArrayList<>(numSubscriptions);
        
        //this.queue = new PriorityQueue<>(10, (e1, e2) ->
        //    Long.compare(e1.getTimeStamp(), e2.getTimeStamp()));
    }
    
    
    @Override
    public void onComplete()
    {
        if (numCompleted.incrementAndGet() == numSubscriptions)
            subscriber.onComplete();
    }
    

    @Override
    public void onError(Throwable throwable)
    {
        subscriber.onError(throwable);
        cancel();
    }
    

    @Override
    public void onNext(E e)
    {
        // add to sorted
        //queue.add(e);
        
        subscriber.onNext(e);
        //numRequestedEvents.decrementAndGet();
    }
    

    @Override
    public synchronized void onSubscribe(Subscription subscription)
    {
        subscriptions.add(subscription);
        if (subscriptions.size() == numSubscriptions)
            subscriber.onSubscribe(this);
    }
    

    @Override
    public void cancel()
    {
        for (Subscription sub: subscriptions)
            sub.cancel();        
    }
    

    @Override
    public void request(long n)
    {
        for (Subscription sub: subscriptions)
            sub.request(n);
        //numRequestedEvents.addAndGet(n);
    }

}
