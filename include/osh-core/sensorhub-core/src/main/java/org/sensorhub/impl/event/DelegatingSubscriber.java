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


/**
 * <p>
 * Base class for all delegating subscribers, useful to simplify implementation
 * of various types of subscribers.
 * </p>
 * 
 * @param <T> The subscribed item type
 *
 * @author Alex Robin
 * @date Apr 5, 2020
 */
public class DelegatingSubscriber<T> extends DelegatingSubscriberAdapter<T, T>
{

    public DelegatingSubscriber(Subscriber<? super T> subscriber)
    {
        super(subscriber);
    }
    
    
    public void onNext(T item)
    {
        subscriber.onNext(item);
    }
}
