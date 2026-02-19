/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.event;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import org.vast.util.Asserts;


/**
 * <p>
 * Delegating subscriber allowing conversion of the subscribed object type.
 * This is used to simplify implementation of various types of subscribers.
 * </p>
 * 
 * @param <IN> The incoming subscribed item type
 * @param <OUT> The outgoing subscribed item type
 *
 * @author Alex Robin
 * @date Apr 5, 2020
 */
public abstract class DelegatingSubscriberAdapter<IN, OUT> implements Subscriber<IN>
{
    protected Subscriber<? super OUT> subscriber;

    
    public abstract void onNext(IN item);
    
    
    public DelegatingSubscriberAdapter(Subscriber<? super OUT> subscriber)
    {
        this.subscriber = Asserts.checkNotNull(subscriber, Subscriber.class);
    }
    
    
    public void onSubscribe(Subscription subscription)
    {
        subscriber.onSubscribe(subscription);
    }


    public void onError(Throwable throwable)
    {
        subscriber.onError(throwable);
    }


    public void onComplete()
    {
        subscriber.onComplete();
    }
}
