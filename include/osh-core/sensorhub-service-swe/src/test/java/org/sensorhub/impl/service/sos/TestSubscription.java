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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * <p>
 * Subscription implementation used in JUnit tests
 * </p>
 * 
 * @param <T> The subscribed type
 *
 * @author Alex Robin
 * @date Apr 16, 2020
 */
class TestSubscription<T> implements Subscription
{
    Subscriber<T> subscriber;
    Queue<T> queue = new ConcurrentLinkedQueue<>();
    AtomicInteger numRequested = new AtomicInteger(0);
    boolean pushComplete;
    boolean done;
    
    TestSubscription(Subscriber<T> subscriber)
    {
        this.subscriber = subscriber;
    }        
    
    @Override
    public void request(long n)
    {
        //System.out.printf("Requesting %d records\n", n);
        numRequested.addAndGet((int)n);
        maybeSend();
    }

    @Override
    public void cancel()
    {            
        done = true;
    }
    
    void maybeSend()
    {
        if (done)
            return;
        
        while (numRequested.get() > 0 && queue != null && !queue.isEmpty())
        {
            subscriber.onNext(queue.poll());
            numRequested.decrementAndGet();
        }
        
        if (queue.isEmpty() && pushComplete && !done)
        {
            done = true;
            subscriber.onComplete();
        }
    }
    
    void push(T e, boolean pushComplete)
    {
        if (queue != null)
        {
            queue.offer(e);
            this.pushComplete = pushComplete;
            maybeSend();
        }
    }
}