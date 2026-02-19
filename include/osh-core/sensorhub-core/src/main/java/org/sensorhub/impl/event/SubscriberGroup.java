/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * <p>
 * This class is used to group several subscribers so that they share the
 * same subscription. Each item is delivered to only one of the subscribers
 * in the group.
 * </p>
 * 
 * @param <T> The subscribed item type 
 *
 * @author Alex Robin
 * @date Mar 8, 2021
 */
public class SubscriberGroup<T> implements Subscriber<T>
{
    List<SharedSubscription> subscriptions = new CopyOnWriteArrayList<>();
    Subscription mainSubscription;
    AtomicInteger nextSubIdx = new AtomicInteger(0);
    
    
    class SharedSubscription implements Subscription, Comparable<SharedSubscription>
    {
        Subscriber<T> subscriber;
        AtomicLong requested = new AtomicLong(0L);
        
        SharedSubscription(Subscriber<T> subscriber)
        {
            this.subscriber = subscriber;
        }
        
        @Override
        public void request(long n)
        {
            requested.addAndGet(n);
            mainSubscription.request(n);
        }

        @Override
        public void cancel()
        {
            subscriptions.remove(this);
            
            if (subscriptions.isEmpty())
                mainSubscription.cancel();
        }

        @Override
        public int compareTo(SubscriberGroup<T>.SharedSubscription other)
        {
            return Long.compare(this.requested.get(), other.requested.get());
        }
    }
    
    
    public synchronized void addConsumer(Subscriber<T> subscriber)
    {
        // sync in case onSubscribe is called concurrently
        var sharedSub = new SharedSubscription(subscriber);
        subscriptions.add(sharedSub);
            
        // in case group member is added after we're already subscribed
        if (mainSubscription != null)
            subscriber.onSubscribe(sharedSub);
    }
    
    
    @Override
    public synchronized void onSubscribe(Subscription subscription)
    {
        // sync in case addConsumer is called concurrently
        this.mainSubscription = subscription;
        
        for (var sub: subscriptions)
            sub.subscriber.onSubscribe(sub);
    }
    

    @Override
    public void onNext(T item)
    {
        // select next sub with requested > 0
        SharedSubscription nextSub;
        do
        {
            int nextSubIdx = safeGetAndIncrementIndex();
            nextSub = subscriptions.get(nextSubIdx);
        }
        while (nextSub.requested.get() <= 0);
        
        nextSub.requested.decrementAndGet();
        nextSub.subscriber.onNext(item);
    }
    
    
    private int safeGetAndIncrementIndex()
    {
        int oldValue = 0;
        int newValue = 0;
        
        do
        {
            oldValue = nextSubIdx.get();
            newValue = (oldValue == subscriptions.size()-1) ? 0 : (oldValue + 1);
        }
        while (!nextSubIdx.compareAndSet(oldValue, newValue));
        
        return oldValue;
    }
    

    @Override
    public void onError(Throwable throwable)
    {
        // TODO Auto-generated method stub
        
    }
    

    @Override
    public void onComplete()
    {
        // TODO Auto-generated method stub
        
    }

}
