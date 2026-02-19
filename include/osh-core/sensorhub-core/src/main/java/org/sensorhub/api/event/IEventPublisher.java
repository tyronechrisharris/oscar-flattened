/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.event;

import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.function.BiPredicate;


/**
 * <p>
 * Interface for event publishers accepting subscriptions of simple listeners
 * as well as {@link Subscriber subscribers} with flow control support.
 * </p>
 *
 * @author Alex Robin
 * @date Feb 21, 2019
 */
public interface IEventPublisher extends Publisher<Event>
{
    
    /**
     * Dispatch event to all subscribers
     * @param e event to dispatch
     */
    public void publish(Event e);
    
    
    /**
     * Dispatch event to all subscribers and provide onDrop callback.
     * <p>An event can be dropped by a subscriber when it cannot keep up with
     * the publisher rate and its delivery buffer has reached maximum capacity.</p>
     * @param e event to dispatch
     * @param onDrop called when event was dropped by a subscriber
     */
    public void publish(Event e, BiPredicate<Subscriber<? super Event>, ? super Event> onDrop);
    
    
    /**
     * @return the number of current subscribers
     */
    public int getNumberOfSubscribers();
}
