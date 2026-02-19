/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are copyright (C) 2019, Sensia Software LLC
 All Rights Reserved. This software is the property of Sensia Software LLC.
 It cannot be duplicated, used, or distributed without the express written
 consent of Sensia Software LLC.

******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.event;

import static org.junit.Assert.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.sensorhub.api.event.Event;
import org.sensorhub.api.event.IEventBus;
import org.sensorhub.api.event.IEventPublisher;
import org.sensorhub.api.feature.FoiAddedEvent;
import org.sensorhub.test.AsyncTests;
import com.google.common.collect.Sets;


public class TestEventBus
{
    static final int TIMEOUT = 2; // in seconds
    static final String GROUP = "urn:test:group";
    static final String[] SOURCES = {
        "urn:test:source0",
        "urn:test:source1",
        "urn:test:source2",
        "urn:test:source3",
        "urn:test:source4"
    };


    static class PublisherInfo
    {
        String id;
        String groupId;
        int numGeneratedEvents = 10;

        PublisherInfo(String id, int numGeneratedEvents)
        {
            this.id = id;
            this.numGeneratedEvents = numGeneratedEvents;
        }

        PublisherInfo(String groupId, String id, int numGeneratedEvents)
        {
            this(id, numGeneratedEvents);
            this.groupId = groupId;
        }
    }


    static class SubscriberInfo
    {
        Set<String> sourceIds = new HashSet<>();
        int numRequestedEvents;
        int numExceptedEvents;
        Predicate<Event> filter;
        AtomicInteger numEventsReceived = new AtomicInteger();

        SubscriberInfo(String sourceId, int numRequestedEvents)
        {
            this(sourceId, numRequestedEvents, numRequestedEvents, null);
        }

        SubscriberInfo(Set<String> sourceIds, int numRequestedEvents)
        {
            this(sourceIds, numRequestedEvents, numRequestedEvents, null);
        }

        SubscriberInfo(String sourceId, int numRequestedEvents, int numExceptedEvents, Predicate<Event> filter)
        {
            this(Sets.newHashSet(sourceId), numRequestedEvents, numExceptedEvents, filter);
        }

        SubscriberInfo(Set<String> sourceIds, int numRequestedEvents, int numExceptedEvents, Predicate<Event> filter)
        {
            this.sourceIds = sourceIds;
            this.numRequestedEvents = numRequestedEvents;
            this.numExceptedEvents = numExceptedEvents;
            this.filter = filter;
        }

        boolean done()
        {
            return numEventsReceived.get() >= numExceptedEvents;
        }
    }


    @Before
    public void beforeTest()
    {
        System.out.println();
        System.out.println("-----------------------------------");
    }


    CountDownLatch createPublishersAndSubscribe(PublisherInfo[] sources, SubscriberInfo[] subscribers) throws Exception
    {
        IEventBus bus = new EventBus();
        CountDownLatch doneSignal = new CountDownLatch(subscribers.length);
        final Set<String> sourcesInGroup = new HashSet<>();

        for (PublisherInfo src: sources)
        {
            // get a publisher for this source
            if (src.groupId == null)
                bus.getPublisher(src.id);
            else
                bus.getPublisher(src.groupId, src.id);

            // add to group set
            if (src.groupId != null)
                sourcesInGroup.add(src.id);
        }

        // subscribe
        AtomicInteger subscriptionCount = new AtomicInteger();
        for (int i = 0; i < subscribers.length; i++)
        {
            final int subId = i;
            SubscriberInfo sub = subscribers[subId];

            bus.newSubscription()
                .withTopicIDs(sub.sourceIds)
                .withFilter(sub.filter)
                .subscribe(new Subscriber<Event>() {
                    @Override
                    public void onComplete()
                    {
                        System.out.println("Publisher done");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        e.printStackTrace();

                        // stop right away
                        while (doneSignal.getCount() > 0)
                            doneSignal.countDown();
                    }

                    @Override
                    public void onNext(Event e)
                    {
                        long now = System.currentTimeMillis();
                        System.out.println(e.getSourceID() + " -> sub" + subId + ": " +
                            e.getTimeStamp() + " @ " + now + ": " +
                            (e instanceof TestEvent ? "count="+((TestEvent)e).count : ""));

                        boolean eventOk;
                        if (sub.sourceIds.contains(GROUP))
                            eventOk = sourcesInGroup.contains(e.getSourceID());
                        else
                            eventOk = sub.sourceIds.contains(e.getSourceID());
                        assertTrue("Received event from incorrect source", eventOk);

                        if (now - e.getTimeStamp() > 100)
                            throw new IllegalStateException("Delivery too slow!");

                        if (sub.numEventsReceived.incrementAndGet() == sub.numExceptedEvents)
                            doneSignal.countDown();
                    }

                    @Override
                    public void onSubscribe(Subscription subscription)
                    {
                        System.out.println("Subscribed " + subId + " to " + sub.sourceIds);
                        subscriptionCount.incrementAndGet();
                        subscription.request(sub.numRequestedEvents);
                    }
                });
        }
        
        // wait until all subscriptions are active
        AsyncTests.waitForCondition(() -> subscriptionCount.get() == subscribers.length, TIMEOUT*1000);

        int count = 0;
        boolean done = false;
        while (!done)
        {
            done = true;

            for (PublisherInfo src: sources)
            {
                if (count >= src.numGeneratedEvents)
                    continue;

                IEventPublisher pub = bus.getPublisher(src.id);
                pub.publish(new TestEvent(src.id, "test" + count, count));
                done = false;
            }

            count++;
            Thread.sleep(1);
        }

        return doneSignal;
    }


    @Test
    public void testOnePublisherOneSubscriber() throws Exception
    {
        PublisherInfo[] sources = {
            new PublisherInfo(SOURCES[0], 10)
        };

        SubscriberInfo[] subscribers = {
            new SubscriberInfo(SOURCES[0], 10)
        };

        CountDownLatch doneSignal = createPublishersAndSubscribe(sources, subscribers);
        assertTrue("Timeout before enough events received", doneSignal.await(TIMEOUT, TimeUnit.SECONDS));
        for (SubscriberInfo sub: subscribers)
            assertEquals(sub.numExceptedEvents, sub.numEventsReceived.get());
    }


    @Test
    public void testOnePublisherOneSubscriberWithFilter() throws Exception
    {
        PublisherInfo[] sources = {
            new PublisherInfo(SOURCES[0], 10)
        };

        SubscriberInfo[] subscribers = {
            new SubscriberInfo(SOURCES[0], 10, 1, e -> ((TestEvent)e).getText().equals("test2"))
        };

        CountDownLatch doneSignal = createPublishersAndSubscribe(sources, subscribers);
        assertTrue("Timeout before enough events received", doneSignal.await(TIMEOUT, TimeUnit.SECONDS));
        for (SubscriberInfo sub: subscribers)
            assertEquals(sub.numExceptedEvents, sub.numEventsReceived.get());
    }


    @Test
    public void testOnePublisherSeveralSubscribers() throws Exception
    {
        PublisherInfo[] sources = {
            new PublisherInfo(SOURCES[0], 20)
        };

        SubscriberInfo[] subscribers = {
            new SubscriberInfo(SOURCES[0], 10),
            new SubscriberInfo(SOURCES[0], 15, 1, e -> ((TestEvent)e).getText().equals("test2")),
            new SubscriberInfo(SOURCES[0], 15),
            new SubscriberInfo(SOURCES[0], 5, 3, e -> ((TestEvent)e).getText().matches("test[0-2]"))
        };

        CountDownLatch doneSignal = createPublishersAndSubscribe(sources, subscribers);
        assertTrue("Timeout before enough events received", doneSignal.await(TIMEOUT, TimeUnit.SECONDS));
        for (SubscriberInfo sub: subscribers)
            assertEquals(sub.numExceptedEvents, sub.numEventsReceived.get());
    }


    @Test
    public void testSeveralPublishersSeveralSubscribers() throws Exception
    {
        PublisherInfo[] sources = {
            new PublisherInfo(SOURCES[0], 20),
            new PublisherInfo(SOURCES[1], 50),
            new PublisherInfo(SOURCES[4], 210),
            new PublisherInfo(SOURCES[3], 160)
        };

        SubscriberInfo[] subscribers = {
            new SubscriberInfo(SOURCES[0], 10),
            new SubscriberInfo(SOURCES[3], 15, 15, e -> ((TestEvent)e).getCount() % 3 == 0),
            new SubscriberInfo(SOURCES[4], 350, 210, null),
            new SubscriberInfo(SOURCES[0], 5, 3, e -> ((TestEvent)e).getText().matches("test[0-2]"))
        };

        CountDownLatch doneSignal = createPublishersAndSubscribe(sources, subscribers);
        assertTrue("Timeout before enough events received", doneSignal.await(TIMEOUT, TimeUnit.SECONDS));
        for (SubscriberInfo sub: subscribers)
            assertEquals(sub.numExceptedEvents, sub.numEventsReceived.get());
    }


    @Test
    public void testGroupPublisherSeveralSubscribers() throws Exception
    {
        PublisherInfo[] sources = {
            new PublisherInfo(GROUP, SOURCES[0], 35),
            new PublisherInfo(GROUP, SOURCES[1], 31),
            new PublisherInfo(GROUP, SOURCES[2], 56),
            new PublisherInfo(SOURCES[4], 23)
        };

        SubscriberInfo[] subscribers = {
            new SubscriberInfo(SOURCES[0], 20),
            new SubscriberInfo(SOURCES[2], 100, 19, e -> ((TestEvent)e).getCount() % 3 == 0),
            new SubscriberInfo(SOURCES[4], 230, 23, null),
            new SubscriberInfo(SOURCES[0], 5, 3, e -> ((TestEvent)e).getText().matches("test[0-2]"))
        };

        CountDownLatch doneSignal = createPublishersAndSubscribe(sources, subscribers);
        assertTrue("Timeout before enough events received", doneSignal.await(TIMEOUT, TimeUnit.SECONDS));
        for (SubscriberInfo sub: subscribers)
            assertEquals(sub.numExceptedEvents, sub.numEventsReceived.get());
    }


    @Test
    public void testGroupPublisherGroupSubscribers() throws Exception
    {
        PublisherInfo[] sources = {
            new PublisherInfo(GROUP, SOURCES[0], 35),
            new PublisherInfo(GROUP, SOURCES[1], 31),
            new PublisherInfo(GROUP, SOURCES[2], 56),
            new PublisherInfo(SOURCES[4], 23)
        };

        SubscriberInfo[] subscribers = {
            new SubscriberInfo(SOURCES[0], 20),
            new SubscriberInfo(GROUP, 500, 35+31+56, null),
            new SubscriberInfo(GROUP, 300, (35+31+56)/2+1, e -> ((TestEvent)e).getCount() % 2 == 0),
            new SubscriberInfo(GROUP, 11, 11, e -> ((TestEvent)e).getCount() % 3 == 0),
            new SubscriberInfo(SOURCES[4], 40, 23, null)
        };

        CountDownLatch doneSignal = createPublishersAndSubscribe(sources, subscribers);
        assertTrue("Timeout before enough events received", doneSignal.await(TIMEOUT, TimeUnit.SECONDS));
        for (SubscriberInfo sub: subscribers)
            assertEquals(sub.numExceptedEvents, sub.numEventsReceived.get());
    }


    @Test
    public void testGroupPublisherGroupMultiSubscribers() throws Exception
    {
        PublisherInfo[] sources = {
            new PublisherInfo(GROUP, SOURCES[0], 35),
            new PublisherInfo(GROUP, SOURCES[1], 31),
            new PublisherInfo(GROUP, SOURCES[2], 56),
            new PublisherInfo(SOURCES[4], 23)
        };

        SubscriberInfo[] subscribers = {
            new SubscriberInfo(SOURCES[4], 20),
            new SubscriberInfo(Sets.newHashSet(SOURCES[0], SOURCES[2]), 500, 35+56, null),
            new SubscriberInfo(Sets.newHashSet(SOURCES[2], SOURCES[1]), 300, (31+1)/2+56/2, e -> ((TestEvent)e).getCount() % 2 == 0)
        };

        CountDownLatch doneSignal = createPublishersAndSubscribe(sources, subscribers);
        assertTrue("Timeout before enough events received", doneSignal.await(TIMEOUT, TimeUnit.SECONDS));
        for (SubscriberInfo sub: subscribers)
            assertEquals(sub.numExceptedEvents, sub.numEventsReceived.get());
    }


    @Test
    public void testFilterByEventClass() throws InterruptedException
    {
        EventBus bus = new EventBus();
        IEventPublisher pub = bus.getPublisher(SOURCES[0]);

        CountDownLatch doneSignal = new CountDownLatch(5);
        AtomicInteger count = new AtomicInteger();

        bus.newSubscription(TestEvent.class)
            .withTopicID(SOURCES[0])
            .subscribe(new Subscriber<TestEvent>() {
                @Override
                public void onNext(TestEvent item)
                {
                    System.out.println(item.getClass());
                    count.incrementAndGet();
                    doneSignal.countDown();
                }

                @Override
                public void onSubscribe(Subscription subscription)
                {
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onComplete()
                {
                }

                @Override
                public void onError(Throwable throwable)
                {
                }
            });

        String sysUID = "source001";
        String foiUID = "foi123-658";
        for (int i = 0; i < 10; i++)
        {
            if (i % 2 == 0)
                pub.publish(new TestEvent(sysUID, "test", 0));
            else
                pub.publish(new FoiAddedEvent(System.currentTimeMillis(), sysUID, foiUID, Instant.now()));
        }

        assertTrue("Not enough events received", doneSignal.await(TIMEOUT, TimeUnit.SECONDS));
    }


    protected long publishAndCheckReceived(EventBus bus, int numGeneratedEvents, String... sources) throws InterruptedException
    {
        System.out.println("Publishing " + numGeneratedEvents + " events from " + sources.length + " source(s)");

        int numExpectedEvents = numGeneratedEvents*sources.length;
        CountDownLatch doneSignal = new CountDownLatch(numExpectedEvents);
        AtomicInteger count = new AtomicInteger();

        bus.newSubscription(TestEvent.class)
            .withTopicID(sources)
            .consume(e -> {
                count.incrementAndGet();
                doneSignal.countDown();
            });

        IEventPublisher[] pubs = new IEventPublisher[sources.length];
        for (int j = 0; j < sources.length; j++)
            pubs[j] = bus.getPublisher(sources[j]);

        long sleepTime = 5000;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < numGeneratedEvents; i++)
        {
            for (int j = 0; j < sources.length; j++)
                pubs[j].publish(new TestEvent(sources[j], "test", 0));
            long sleepEnd = System.nanoTime() + sleepTime;
            while (System.nanoTime() < sleepEnd);
        }

        assertTrue("Timeout before enough events received", doneSignal.await(TIMEOUT, TimeUnit.SECONDS));
        assertEquals("Incorrect number of events received", numExpectedEvents, count.get());
        return System.currentTimeMillis() - t0 - (sleepTime*numGeneratedEvents/1000000);
    }


    @Test
    public void testListenOneType() throws InterruptedException
    {
        EventBus bus = new EventBus();
        IEventPublisher[] pubs = new IEventPublisher[SOURCES.length];
        for (int i = 0; i < SOURCES.length; i++)
            pubs[i] = bus.getPublisher(SOURCES[i]);

        publishAndCheckReceived(bus, 10, SOURCES[1]);
        publishAndCheckReceived(bus, 100, SOURCES[0], SOURCES[3]);
        publishAndCheckReceived(bus, 33, SOURCES[4], SOURCES[1]);
        publishAndCheckReceived(bus, 25, SOURCES[4], SOURCES[1], SOURCES[2]);
    }


    /*@Test
    public void testThroughput() throws InterruptedException
    {
        EventBus bus = new EventBus();
        int numSources, numEvents;
        long dt;

        numSources = 1;
        IEventPublisher[] pubs = new IEventPublisher[numSources];
        for (int i = 0; i < numSources; i++)
            pubs[i] = bus.getPublisher(SOURCES[i]);

        dt = publishAndCheckReceived(bus, numEvents = 100, SOURCES[0]);
        System.out.println(numEvents*1000/dt + " events/s");

        dt = publishAndCheckReceived(bus, numEvents = 1000000, SOURCES[0]);
        System.out.println(numEvents*1000/dt + " events/s");
    }*/

}
