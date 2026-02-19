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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.stream.Collectors;
import org.sensorhub.api.event.EventUtils;
import org.sensorhub.api.event.IEventBus;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.data.IObsData;
import org.sensorhub.api.data.ObsEvent;
import org.sensorhub.api.datastore.obs.ObsFilter;
import org.sensorhub.impl.datastore.DataStoreUtils;
import org.sensorhub.impl.event.DelegatingSubscriber;
import org.sensorhub.impl.event.DelegatingSubscriberAdapter;
import org.sensorhub.impl.event.DelegatingSubscription;
import org.vast.ogc.om.IObservation;
import org.vast.ows.sos.GetObservationRequest;
import org.vast.ows.sos.GetResultRequest;
import org.vast.ows.sos.SOSException;
import org.vast.swe.SWEHelper;
import org.vast.swe.ScalarIndexer;
import org.vast.util.Asserts;
import org.vast.util.TimeExtent;
import com.google.common.collect.ImmutableMap;


/**
 * <p>
 * Implementation of SOS data provider used to stream real-time observations
 * from a system using the event-bus.
 * </p>
 *
 * @author Alex Robin
 * @since Apr 10, 2020
 */
public class StreamingDataProvider extends SystemDataProvider
{
    final IEventBus eventBus;
    final TimeOutMonitor timeOutMonitor;


    public StreamingDataProvider(final SOSService service, final SystemDataProviderConfig config)
    {
        super(service.getServlet(),
             service.getReadDatabase(),
             service.getThreadPool(),
             config);
        
        this.eventBus = service.getParentHub().getEventBus();
        this.timeOutMonitor = Asserts.checkNotNull(service.getTimeOutMonitor(), TimeOutMonitor.class);
    }


    @Override
    public void getObservations(GetObservationRequest req, Subscriber<IObservation> consumer) throws SOSException
    {
        var originalTimeFilter = req.getTime();
        
        // build obs filter from request but using 'now' for time
        req.setTime(TimeExtent.now());
        var obsFilter = getObsFilter(req, null);
        
        // cache of datastreams info
        var dataStreams = new HashMap<BigId, DataStreamInfoCache>();
        var dataStreamsByTopic = new HashMap<String, DataStreamInfoCache>();
        var foiIdCache = new HashMap<BigId, String>();
        
        // query selected datastreams
        var dsFilter = obsFilter.getDataStreamFilter();
        database.getDataStreamStore().selectEntries(dsFilter)
            .forEach(dsEntry -> {
                var dsKey = dsEntry.getKey();
                var dsInfo = dsEntry.getValue();
                var dsInfoCache = new DataStreamInfoCache(dsKey.getInternalID(), dsInfo);
                dataStreams.put(dsInfoCache.internalId, dsInfoCache);
                var topic = EventUtils.getDataStreamDataTopicID(
                    dsInfoCache.sysUID,
                    dsInfoCache.resultStruct.getName());
                dataStreamsByTopic.put(topic, dsInfoCache);
            });

        // call getResults and transform ObsEvents to Observation objects
        subscribeAndProcessDataEvents(dataStreams, originalTimeFilter, obsFilter, new DelegatingSubscriberAdapter<ObsEvent, IObservation>(consumer) {
            public void onNext(ObsEvent e)
            {
                var dsInfoCache = dataStreamsByTopic.get(e.getSourceID());
                
                for (var obs: e.getObservations())
                {
                    // can reuse same structure since we are running in a single thread
                    var result = dsInfoCache.resultStruct;
                    result.setData(obs.getResult());
                    
                    // get FOI UID from cache
                    String foiUID = null;
                    if (obs.hasFoi())
                    {
                        foiUID = foiIdCache.computeIfAbsent(obs.getFoiID(), k -> {
                            var f = database.getFoiStore().getCurrentVersion(k);
                            return f != null ? f.getUniqueIdentifier() : null;
                        });
                    }
                    
                    consumer.onNext(SOSProviderUtils.buildObservation(dsInfoCache.sysUID, foiUID, result));
                }
            }
        });
    }


    @Override
    public void getResults(GetResultRequest req, Subscriber<ObsEvent> consumer) throws SOSException
    {
        Asserts.checkState(selectedDataStream != null, "getResultTemplate hasn't been called");
        String procUID = getProcedureUID(req.getOffering());
                
        // build obs filter for current records (i.e. 'now')
        // Use equivalent GetObs request
        var getObsReq = new GetObservationRequest();
        getObsReq.getProcedures().add(procUID);
        getObsReq.getObservables().addAll(req.getObservables());
        getObsReq.getFoiIDs().addAll(req.getFoiIDs());
        getObsReq.setSpatialFilter(req.getSpatialFilter());
        getObsReq.setTemporalFilter(req.getTemporalFilter());
        getObsReq.setTime(TimeExtent.now());
        var obsFilter = getObsFilter(getObsReq, selectedDataStream.internalId);
        
        // select a single datastream
        var dataStreams = ImmutableMap.of(selectedDataStream.internalId, selectedDataStream);
        
        // subscribe to event bus and forward all matching events
        subscribeAndProcessDataEvents(dataStreams, req.getTime(), obsFilter, consumer);
    }
    
    
    @Override
    protected ObsFilter getObsFilter(GetObservationRequest req, BigId dataStreamId) throws SOSException
    {
        var obsFilter = super.getObsFilter(req, dataStreamId);
        
        // prefetch FOI internal IDs if filtering on FOIs
        // this is necessary because IObsData objects in events only contain the FOI ID
        if (obsFilter.getFoiFilter() != null)
        {
            var foiIDs = DataStoreUtils
                .selectFeatureIDs(database.getFoiStore(), obsFilter.getFoiFilter())
                .collect(Collectors.toSet());
            
            obsFilter = ObsFilter.Builder.from(obsFilter)
                .withFois(foiIDs)
                .build();
        }
        
        return obsFilter;
    }
    
    
    protected void subscribeAndProcessDataEvents(Map<BigId, DataStreamInfoCache> dataStreams, TimeExtent timeFilter, ObsFilter obsFilter, Subscriber<ObsEvent> consumer)
    {        
        // create set of event sources
        var topics = new HashSet<String>();
        for (var dsInfoCache: dataStreams.values())
        {
            var topic = EventUtils.getDataStreamDataTopicID(
                dsInfoCache.sysUID,
                dsInfoCache.resultStruct.getName());
            topics.add(topic);
        }        
        
        // subscribe for data events only if continuous live stream was requested
        if (timeFilter != null && !timeFilter.isNow())
        {
            long timeOut = (long)(config.liveDataTimeout * 1000.);

            // prepare time indexer so we can check against request stop time
            // this only works for GetResult for now
            ScalarIndexer timeIndexer;
            double stopTime;
            if (timeFilter.hasEnd() && selectedDataStream != null)
            {
                timeIndexer = SWEHelper.getTimeStampIndexer(selectedDataStream.resultStruct);
                stopTime = timeFilter.end().toEpochMilli() / 1000.0;
            }
            else
            {
                timeIndexer = null;
                stopTime = Double.POSITIVE_INFINITY;
            }

            // subscribe to event bus
            // wrap subscriber to handle timeout and end time
            eventBus.newSubscription(ObsEvent.class)
                .withTopicIDs(topics)
                .withEventType(ObsEvent.class)
                .subscribe(new DelegatingSubscriber<ObsEvent>(consumer) {
                    Subscription wrappedSub;
                    Subscription wrappingSub;
                    volatile boolean currentTimeRecordsSent = false;
                    volatile boolean canceled = false;
                    volatile long latestEventTimestamp;

                    @Override
                    public void onSubscribe(Subscription sub)
                    {
                        latestEventTimestamp = System.currentTimeMillis();
                        
                        // wrap subscription so we can send latest records before we actually
                        // start streaming real-time records from event bus
                        var delegatingSubscriber = this;
                        this.wrappedSub = sub;
                        this.wrappingSub = new DelegatingSubscription(sub) {
                            @Override
                            public void request(long n)
                            {
                                // always send current time record of each producer (if available)
                                // synchronously if available
                                if (!currentTimeRecordsSent)
                                {
                                    // TODO send only n records, not all of them
                                    sendLatestRecords(dataStreams, obsFilter, delegatingSubscriber);
                                    currentTimeRecordsSent = true;
                                    
                                    // stop here if there was nothing since timeout period
                                    if (System.currentTimeMillis() - latestEventTimestamp > timeOut)
                                    {
                                        onComplete();
                                        return;
                                    }
                                    
                                    timeOutMonitor.register(delegatingSubscriber::checkTimeOut);
                                }
                                
                                super.request(n);
                            }

                            @Override
                            public void cancel()
                            {
                                servlet.getLogger().debug("Canceling subscription: " + topics);
                                super.cancel();
                                canceled = true;
                            }
                        };
            
                        super.onSubscribe(wrappingSub);
                    }

                    protected boolean checkTimeOut()
                    {
                        if (canceled)
                            return true;
                        
                        if (System.currentTimeMillis() - latestEventTimestamp > timeOut)
                        {
                            servlet.getLogger().debug("Data provider timeout");
                            wrappingSub.cancel();
                            onComplete();
                            return true;
                        }
                        
                        return false;
                    }

                    @Override
                    public void onNext(ObsEvent event)
                    {
                        latestEventTimestamp = event.getTimeStamp();
                        
                        if (currentTimeRecordsSent)
                        {
                            //servlet.getLogger().debug("Event ts={}", latestRecordTimestamp);
                            var firstObs = event.getObservations()[0];
                            if (timeIndexer != null && timeIndexer.getDoubleValue(firstObs.getResult()) > stopTime)
                                onComplete();
                            else if (acceptEvent(obsFilter, firstObs))
                                super.onNext(event);
                            else
                                wrappingSub.request(1);
                        }
                        else
                            wrappedSub.request(1);
                    }
                });
        }
        else
            sendLatestRecordsOnly(dataStreams, obsFilter, consumer);
    }
    
    
    protected boolean acceptEvent(ObsFilter filter, IObsData obs)
    {
        var foiFilter = filter.getFoiFilter();
        if (foiFilter != null && foiFilter.getInternalIDs() != null)
        {
            if (!obs.hasFoi() || !foiFilter.getInternalIDs().contains(obs.getFoiID()))
                return false;
        }
        
        return true;
    }
    
    
    protected void sendLatestRecordsOnly(Map<BigId, DataStreamInfoCache> dataStreams, ObsFilter obsFilter, Subscriber<ObsEvent> consumer)
    {
        consumer.onSubscribe(new Subscription() {
            boolean currentTimeRecordsSent = false;
            
            @Override
            public void request(long n)
            {
                if (!currentTimeRecordsSent)
                {
                    // TODO send only n records, not all of them
                    currentTimeRecordsSent = true;
                    sendLatestRecords(dataStreams, obsFilter, consumer);
                    consumer.onComplete();
                }
            }

            @Override
            public void cancel()
            {
            }
        });
    }


    /*
     * Send the latest record of each data source to the consumer
     */
    protected void sendLatestRecords(Map<BigId, DataStreamInfoCache> dataStreams, ObsFilter obsFilter, Subscriber<ObsEvent> consumer)
    {
        var eventTime = System.currentTimeMillis();
        database.getObservationStore().select(obsFilter)
            .forEach(obs -> {
                var dsInfoCache = dataStreams.get(obs.getDataStreamID());
                consumer.onNext(new ObsEvent(
                    eventTime,
                    dsInfoCache.sysUID,
                    dsInfoCache.resultStruct.getName(),
                    obs));
            });
    }


    @Override
    public boolean hasMultipleProducers()
    {
        return false;
    }


    @Override
    public void close()
    {

    }
}
