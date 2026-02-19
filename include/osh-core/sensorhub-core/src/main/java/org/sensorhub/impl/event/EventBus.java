/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.sensorhub.api.event.Event;
import org.sensorhub.api.event.IEventBus;
import org.sensorhub.api.event.IEventPublisher;
import org.sensorhub.api.event.SubscribeOptions;
import org.sensorhub.utils.NamedThreadFactory;
import org.sensorhub.api.event.ISubscriptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.util.Asserts;


/**
 * <p>
 * Event Bus: Main event management class in SensorHub.<br/>
 * All event producers and listeners registrations must be done through
 * this class (instead of directly with the source module) in order to
 * benefit from more advanced event dispatching implementations such as
 * distributed event messaging.<br/>
 * </p>
 *
 * @author Alex Robin
 * @since Mar 19, 2015
 */
public class EventBus implements IEventBus
{
    private static final Logger log = LoggerFactory.getLogger(EventBus.class);
    public static final int MAX_BUFFER_CAPACITY = 1024;
    public static final String MAIN_TOPIC = "_MAIN_";
        
    private ExecutorService threadPool;
    private Map<String, IEventPublisher> publishers;
    
    
    public EventBus()
    {
        publishers = new HashMap<>();
        
        // create thread pool that will be used by all asynchronous event handlers
        threadPool = new ThreadPoolExecutor(0, 100,
                                            10L, TimeUnit.SECONDS,
                                            new SynchronousQueue<Runnable>(),
                                            new NamedThreadFactory("EventBus"));
    }
    
    
    /*
     * Ensures a publisher is created for this topicID.
     * 1. If a publisher already exists, it is returned.
     * 2. If the previously registered publisher was just a placeholder it is
     *    converted to a full fledged publisher.
     * 3. Otherwise a new publisher is created.
     */
    protected IEventPublisher ensurePublisher(final String topicID, final Supplier<IEventPublisher> supplier)
    {
        return publishers.compute(topicID, (id, pub) -> {
            if (pub == null)
            {
                log.debug("Creating publisher for {}", topicID);
                var newPublisher = supplier.get();
                return newPublisher;
            }
            else
                return pub;
        });
    }
    
    
    @Override
    public synchronized IEventPublisher getPublisher(String topicID)
    {
        Asserts.checkNotNull(topicID, "topicID");
        
        return ensurePublisher(topicID,
            () -> new FilteredEventPublisher(topicID, threadPool, MAX_BUFFER_CAPACITY, log) );
    }
        
    
    @Override
    public synchronized IEventPublisher getPublisher(String groupID, String topicID)
    {
        Asserts.checkNotNull(groupID, "groupID");
        Asserts.checkNotNull(topicID, "topicID");
        
        IEventPublisher groupPublisher = getPublisher(groupID);
        return ensurePublisher(topicID, () -> {
            log.debug("Publisher group = {}", groupID);
            return new FilteredEventPublisherWrapper(groupPublisher, topicID, e -> topicID.equals(e.getSourceID()));
        });
    }


    @Override
    public ISubscriptionBuilder<Event> newSubscription()
    {
        return new SubscriptionBuilderImpl<>(Event.class);
    }


    @Override
    public <E extends Event> ISubscriptionBuilder<E> newSubscription(Class<E> eventClass)
    {
        return new SubscriptionBuilderImpl<>(eventClass);
    }
    
    
    @Override
    public int getNumberOfSubscribers(String sourceID)
    {
        IEventPublisher publisher = publishers.get(sourceID);
        if (publisher != null)
            return publisher.getNumberOfSubscribers();
        else
            return 0;
    }
    
    
    /*
     * Register one subscriber to a single source
     */
    synchronized void subscribe(String topicID, Subscriber<Event> subscriber)
    {
        //IEventPublisher publisher = publishers.computeIfAbsent(topicID, id -> new PlaceHolderPublisher());
        // don't use placeholder as it doesn't handle unsubscribe properly
        var publisher = getPublisher(topicID);
        publisher.subscribe(subscriber);
    }
    
    
    /*
     * Register subscriber to a single source with a filter
     */
    @SuppressWarnings("unchecked")
    synchronized <E extends Event> void subscribe(String topicID, Predicate<? super E> filter, Subscriber<? super E> subscriber)
    {
        if (filter == null)
            subscribe(topicID, (Subscriber<Event>)subscriber);
        else
            subscribe(topicID, (Subscriber<Event>)new FilteredSubscriber<E>(subscriber, filter));
    }
    
    
    /*
     * Register subscriber to multiple sources, with an optional filter
     */
    <E extends Event> void subscribeMulti(Set<String> topicIDs, Predicate<? super E> filter, Subscriber<? super E> subscriber)
    {
        AggregateSubscription<E> sub = new AggregateSubscription<>(subscriber, topicIDs.size());
        
        for (String topicID: topicIDs)
            subscribe(topicID, filter, sub);
    }
    
    
    /*
     * Register subscriber to multiple sources in the same group, with an optional filter
     */
    /*<E extends Event> void subscribeMulti(String groupID, Set<String> topicIDs, Predicate<? super E> filter, Subscriber<? super E> subscriber)
    {
        //  create filter with list of sources
        Predicate<? super E> groupFilter = filter != null ?
            e -> topicIDs.contains(e.getSourceID()) && filter.test(e) :
            e -> topicIDs.contains(e.getSourceID());
        
        // subscribe to entire group with filter
        subscribe(groupID, groupFilter, subscriber);
    }*/
    
    
    /*
     * Build the predicate corresponding to subscription options
     */
    <E extends Event> Predicate<? super E> buildPredicate(SubscribeOptions<E> opts)
    {
        Predicate<? super E> combinedFilter = opts.getFilter();
        
        // special case for a single event type selected since it's very common 
        if (opts.getEventTypes().size() == 1)
        {
            final Class<? extends E> eventClass = opts.getEventTypes().iterator().next();
            if (opts.getFilter() != null)
                combinedFilter = e -> eventClass.isAssignableFrom(e.getClass()) && opts.getFilter().test(e);
            else
                combinedFilter = e -> eventClass.isAssignableFrom(e.getClass());
        }
        
        else if (!opts.getEventTypes().isEmpty())
        {
            combinedFilter = e -> {
                for (Class<? extends E> c: opts.getEventTypes())
                {
                    if (c.isAssignableFrom(e.getClass()))
                        return true;
                }
                return false;
            };
        }
        
        return combinedFilter;
    }
    
    
    /*
     * Register subscriber with the specified subscription options
     */
    <E extends Event> void subscribe(SubscribeOptions<E> opts, Subscriber<? super E> subscriber)
    {
        Predicate<? super E> filter = buildPredicate(opts);
        
        if (opts.getSourceIDs().size() == 1)
            subscribe(opts.getSourceIDs().iterator().next(), filter, subscriber);
        else
            subscribeMulti(opts.getSourceIDs(), filter, subscriber);
    }
    
    
    @Override
    public void shutdown()
    {
        if (threadPool != null)
            threadPool.shutdownNow();
    }
    
    
    /*
     * Extended builder class adding methods to submit the subscription
     */
    public class SubscriptionBuilderImpl<E extends Event>
        extends SubscribeOptions.Builder<SubscriptionBuilderImpl<E>, E>
        implements ISubscriptionBuilder<E>
    {
        
        protected SubscriptionBuilderImpl(Class<E> eventClass)
        {
            super(eventClass);
        }
        
        protected CompletableFuture<Subscription> subscribe(Subscriber<? super E> subscriber, CompletableFuture<Subscription> future)
        {
            SubscribeOptions<E> opts = build();
            EventBus.this.subscribe(opts, subscriber);
            return future;
        }

        @Override
        public CompletableFuture<Subscription> subscribe(Subscriber<? super E> subscriber)
        {
            Asserts.checkNotNull(subscriber, Subscriber.class);
            CompletableFuture<Subscription> future = new CompletableFuture<>();
            
            // wrap subscriber to also notify future
            Subscriber<? super E> subscriberWithFuture = new DelegatingSubscriber<>(subscriber) {
                @Override
                public void onSubscribe(Subscription sub)
                {
                    super.onSubscribe(sub);
                    future.complete(sub);
                }
            };
            
            return subscribe(subscriberWithFuture, future);
        }
        
        @Override
        public CompletableFuture<Subscription> subscribe(Consumer<? super E> onNext)
        {
            CompletableFuture<Subscription> future = new CompletableFuture<>();
            return subscribe(new SubscriberConsumerAdapter<>(future::complete, onNext, true), future);
        }
        
        @Override
        public CompletableFuture<Subscription> subscribe(Consumer<? super E> onNext, Consumer<Throwable> onError)
        {
            CompletableFuture<Subscription> future = new CompletableFuture<>();
            return subscribe(new SubscriberConsumerAdapter<>(future::complete, onNext, onError, true), future);
        }
        
        @Override
        public CompletableFuture<Subscription> subscribe(Consumer<? super E> onNext, Consumer<Throwable> onError, Runnable onComplete)
        {
            CompletableFuture<Subscription> future = new CompletableFuture<>();
            return subscribe(new SubscriberConsumerAdapter<>(future::complete, onNext, onError, onComplete, true), future);
        }
        
        @Override
        public CompletableFuture<Subscription> consume(Consumer<? super E> consumer)
        {
            Asserts.checkNotNull(consumer, Consumer.class);
            CompletableFuture<Subscription> future = new CompletableFuture<>();
            return subscribe(new SubscriberConsumerAdapter<>(future::complete, consumer, false), future);
        }
    }
}
