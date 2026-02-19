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
import java.util.concurrent.Flow.Subscription;
import java.util.function.Predicate;


/**
 * <p>
 * Wraps an existing subscriber and further filters items before they are
 * delivered to its {@link Subscriber#onNext(Object) onNext()} method
 * </p>
 * 
 * @param <T> the subscribed item type
 *
 * @author Alex Robin
 * @date Feb 22, 2019
 */
public class FilteredSubscriber<T> extends DelegatingSubscriber<T>
{
    Predicate<? super T> filter;
    Subscription subscription;
    
    
    public FilteredSubscriber(Subscriber<? super T> subscriber, Predicate<? super T> filter)
    {
        super(subscriber);
        this.filter = filter;
    }


    @Override
    public void onNext(T item)
    {
        if (filter.test(item))
            subscriber.onNext(item);
        else
            subscription.request(1);
    }


    @Override
    public void onSubscribe(Subscription subscription)
    {
        this.subscription = subscription;
        subscriber.onSubscribe(subscription);
    }
}
