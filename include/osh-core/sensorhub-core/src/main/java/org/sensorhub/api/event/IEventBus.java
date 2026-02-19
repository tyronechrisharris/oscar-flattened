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


/**
 * <p>
 * Event bud interface for dispatching and subscribing to events.<br/>
 * This class allows subscribing to one or more event sources at once as well
 * as create publisher groups to share a publication channel (and resources)
 * among several publishers.
 * </p>
 *
 * @author Alex Robin
 * @date Mar 2, 2019
 */
public interface IEventBus
{
    
    /**
     * Create a new publisher or return the existing one for the given source ID
     * @param sourceID ID of event source to obtain a publisher for
     * @return the publisher instance that can be used to publish events and
     * directly register subscribers
     */
    IEventPublisher getPublisher(String sourceID);


    /**
     * Get a publisher that shares a channel with all other sources in the same group.
     * <p>All events published in the same group are delivered sequentially and their
     * order is guaranteed globally among all publishers in the group. Events are
     * dispatched in the same order they were received, even if they come from different
     * sources</p>
     * @param groupID ID of publisher group
     * @param sourceID ID of event source to obtain a publisher for
     * @return the publisher instance that can be used to publish events and
     * directly register subscribers
     */
    IEventPublisher getPublisher(String groupID, String sourceID);
    
    
    /**
     * Create a new subscription for events handled by this event bus
     * @return the builder for the new subscription
     */
    ISubscriptionBuilder<Event> newSubscription();
    
    
    /**
     * Create a new subscription for events of a specific class handled by
     * this event bus
     * @param eventClass The event class to filter on
     * @return the builder for the new subscription
     */
    <E extends Event> ISubscriptionBuilder<E> newSubscription(Class<E> eventClass);
    
    
    /**
     * Check how many subscribers are listening to events from a given source.
     * <p><i>Note: For instance, this can be used to pause the source when nothing
     * is expecting data from it</i></p>
     * @param sourceID
     * @return number of subscribers
     */
    int getNumberOfSubscribers(String sourceID);


    void shutdown();

}