/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.system;

import java.util.concurrent.Flow.Subscriber;
import java.util.stream.Collectors;
import org.sensorhub.api.command.CommandStreamEvent;
import org.sensorhub.api.data.DataStreamEvent;
import org.sensorhub.api.data.ObsEvent;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.command.CommandStreamFilter;
import org.sensorhub.api.datastore.obs.DataStreamFilter;
import org.sensorhub.api.datastore.obs.ObsFilter;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.api.event.EventUtils;
import org.sensorhub.api.event.IEventBus;
import org.sensorhub.api.system.SystemEvent;


/**
 * <p>
 * Helper for subscribing to system events, including observation events
 * and system change events (i.e. system added, updated, deleted).
 * </p><p>
 * The filter is evaluated at the time of subscription and not re-evaluated
 * afterwards, even if wildcards are used. This is to avoid unexpected data
 * to start flowing through an existing subscription. This means that if the
 * consumer wants to subscribe to events associated to systems that joined
 * the hub after the subscription was created, the consumer must also
 * subscribe to the desired "resource added" events and update the main
 * subscription to include the new data sources.
 * </p>
 *
 * @author Alex Robin
 * @since May 26, 2021
 */
public class SystemSubscriptionHandler
{
    IObsSystemDatabase db;
    IEventBus eventBus;
    
    
    public SystemSubscriptionHandler(IObsSystemDatabase db, IEventBus eventBus)
    {
        this.db = db;
        this.eventBus = eventBus;
    }
    
    
    public <T extends SystemEvent> boolean subscribe(SystemFilter filter, Class<T> eventClass, Subscriber<T> subscriber)
    {
        // query all matching systems from DB
        // and collect corresponding topics
        var topics = db.getSystemDescStore().select(filter)
            .map(proc -> EventUtils.getSystemStatusTopicID(proc.getUniqueIdentifier()))
            .collect(Collectors.toSet());
        
        if (topics.isEmpty())
            return false;
        
        // subscribe to all selected topics
        eventBus.newSubscription(eventClass)
            .withTopicIDs(topics)
            .subscribe(subscriber);
        
        return true;
    }
    
    
    public boolean subscribe(SystemFilter filter, Subscriber<SystemEvent> subscriber)
    {
        return subscribe(filter, SystemEvent.class, subscriber);
    }
    
    
    public boolean subscribe(DataStreamFilter filter, Subscriber<DataStreamEvent> subscriber)
    {        
        // query all matching datastreams from DB
        // and collect corresponding topics
        var topics = db.getDataStreamStore().select(filter)
            .map(dsInfo -> {
                var sysUID = dsInfo.getSystemID().getUniqueID();
                var outputName = dsInfo.getOutputName();
                return EventUtils.getDataStreamStatusTopicID(sysUID, outputName);
            })
            .collect(Collectors.toSet());
        
        if (topics.isEmpty())
            return false;
        
        // subscribe to all selected topics
        eventBus.newSubscription(DataStreamEvent.class)
            .withTopicIDs(topics)
            .subscribe(subscriber);
        
        return true;
    }
    
    
    public boolean subscribe(CommandStreamFilter filter, Subscriber<CommandStreamEvent> subscriber)
    {
        // query all matching command streams from DB
        // and collect corresponding topics
        var topics = db.getCommandStreamStore().select(filter)
            .map(csInfo -> {
                var sysUID = csInfo.getSystemID().getUniqueID();
                var inputName = csInfo.getControlInputName();
                return EventUtils.getCommandStreamStatusTopicID(sysUID, inputName);
            })
            .collect(Collectors.toSet());
        
        if (topics.isEmpty())
            return false;
        
        // subscribe to all selected topics
        eventBus.newSubscription(CommandStreamEvent.class)
            .withTopicIDs(topics)
            .subscribe(subscriber);
        
        return true;
    }
    
    
    public boolean subscribe(ObsFilter filter, Subscriber<ObsEvent> subscriber)
    {
        var dsFilter = filter.getDataStreamFilter();
        if (dsFilter == null)
            dsFilter = new DataStreamFilter.Builder().build();
        
        // query all matching datastreams from DB
        var topics = db.getDataStreamStore().select(dsFilter)
            .map(dsInfo -> {
                var sysUID = dsInfo.getSystemID().getUniqueID();
                var outputName = dsInfo.getOutputName();
                return EventUtils.getDataStreamDataTopicID(sysUID, outputName);
            })
            .collect(Collectors.toSet());
        
        if (topics.isEmpty())
            return false;
        
        // subscribe to all selected topics
        eventBus.newSubscription(ObsEvent.class)
            .withTopicIDs(topics)
            //.withFilter(e -> filter.test(e.getObservations().iterator().next()))
            .subscribe(subscriber);
        
        return true;
    }
}
