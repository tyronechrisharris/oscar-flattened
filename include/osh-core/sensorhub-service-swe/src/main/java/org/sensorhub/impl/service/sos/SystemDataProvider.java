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

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.common.IdEncoder;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.SpatialFilter;
import org.sensorhub.api.datastore.TemporalFilter;
import org.sensorhub.api.datastore.SpatialFilter.SpatialOp;
import org.sensorhub.api.datastore.feature.FoiFilter;
import org.sensorhub.api.datastore.obs.DataStreamFilter;
import org.sensorhub.api.datastore.obs.ObsFilter;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.api.feature.FeatureWrapper;
import org.sensorhub.impl.service.swe.RecordTemplate;
import org.sensorhub.impl.system.SystemUtils;
import org.vast.ogc.gml.IFeature;
import org.vast.ogc.gml.JTSUtils;
import org.vast.ogc.om.IProcedure;
import org.vast.ows.sos.GetFeatureOfInterestRequest;
import org.vast.ows.sos.GetObservationRequest;
import org.vast.ows.sos.GetResultTemplateRequest;
import org.vast.ows.sos.SOSException;
import org.vast.ows.swe.DescribeSensorRequest;
import org.vast.util.Asserts;
import org.locationtech.jts.geom.Point;
import net.opengis.fes.v20.BBOX;
import net.opengis.fes.v20.BinarySpatialOp;
import net.opengis.fes.v20.DWithin;
import net.opengis.fes.v20.DistanceBuffer;
import net.opengis.fes.v20.GMLExpression;
import net.opengis.fes.v20.SpatialOps;
import net.opengis.gml.v32.AbstractGeometry;
import net.opengis.swe.v20.DataComponent;


public abstract class SystemDataProvider implements ISOSAsyncDataProvider
{
    protected final SOSServlet servlet;
    protected final IObsSystemDatabase database;
    protected final SystemDataProviderConfig config;
    protected final ScheduledExecutorService threadPool;
    protected final IdEncoder foiIdEncoder;
    DataStreamInfoCache selectedDataStream;

    class DataStreamInfoCache
    {
        BigId internalId;
        String sysUID;
        DataComponent resultStruct;
        RecordTemplate resultTemplate;

        DataStreamInfoCache(BigId dsId, IDataStreamInfo dsInfo)
        {
            this.internalId = dsId;
            this.sysUID = dsInfo.getSystemID().getUniqueID();
            this.resultStruct = dsInfo.getRecordStructure().copy();
            this.resultTemplate = new RecordTemplate(dsInfo.getRecordStructure(), dsInfo.getRecordEncoding());
        }
    }
    
    protected class StreamSubscription<T> implements Subscription
    {
        Subscriber<T> subscriber;
        Spliterator<T> spliterator;
        Queue<T> itemQueue;
        int batchSize;
        volatile boolean streamDone = false;
        volatile boolean canceled = false;
        volatile boolean complete = false;
        AtomicBoolean reading = new AtomicBoolean(false);
        AtomicLong requested = new AtomicLong();
        
        StreamSubscription(Subscriber<T> subscriber, int batchSize, Stream<T> itemStream)
        {
            this.subscriber = Asserts.checkNotNull(subscriber, Subscriber.class);
            this.spliterator = Asserts.checkNotNull(itemStream, Stream.class).spliterator();
            this.itemQueue = new ArrayDeque<>(batchSize);
            this.batchSize = batchSize;
        }

        @Override
        public void request(long n)
        {
            if (complete || canceled)
                return;
            
            if (n <= 0L)
            {
                canceled = true;
                subscriber.onError(new IllegalArgumentException("bad request, n <= 0"));
            }
            
            /*boolean more = false;
            for (int i = 0; i < n && more; i++)
                more = spliterator.tryAdvance(e -> subscriber.onNext(e));            
            if (!more)
                subscriber.onComplete();*/
            
            long u = requested.addAndGet(n);
            //servlet.getLogger().debug("Requested {} items", n);
            maybeSendItems(u);            
        }

        void maybeFetchFromStorage()
        {
            if (complete || canceled)
                return;
            
            //servlet.getLogger().debug("maybeFetch");
            //long u = requested.get();
            var fetchNextBatch = itemQueue.size() < batchSize/3;//itemQueue.size() < Math.ceil(u/2.0);
            
            // fetch more from DB if needed
            if (fetchNextBatch && !streamDone && !canceled && reading.compareAndSet(false, true))
            {
                //servlet.getLogger().debug("Need {} items, queue={}", u, itemQueue.size());
                CompletableFuture.runAsync(() -> {
                    int count = 0;
                    while (spliterator.tryAdvance(e -> itemQueue.add(e)) && ++count < batchSize);
                    //servlet.getLogger().debug("Loaded new batch of {} items", count);
                    /*try { Thread.sleep(200); }
                    catch (InterruptedException e1) { }*/
                    streamDone = count < batchSize;
                    reading.compareAndSet(true, false);
                }, threadPool)
                .thenRun(() -> {
                    //servlet.getLogger().debug("Send After Fetch");
                    maybeSendItems(requested.get());
                })
                .exceptionally(e -> {
                    subscriber.onError(e);
                    return null;
                });
            }
        }
        
        void maybeSendItems(long n)
        {
            try
            {
                if (complete || canceled)
                    return;
                
                //servlet.getLogger().debug("maybeSend");
                
                long s = Math.min(n, itemQueue.size());
                //long numSent = s;
                if (requested.compareAndSet(n, n-s))
                {
                    //servlet.getLogger().debug("Before send: req={}, queue={}, canceled={}",
                    //    n, itemQueue.size(), canceled);
                    
                    while (s > 0 && !itemQueue.isEmpty() && !canceled)
                    {
                        subscriber.onNext(itemQueue.poll());
                        s--;
                    }
                    
                    if (streamDone && itemQueue.isEmpty() && !canceled)
                    {
                        subscriber.onComplete();
                        complete = true;
                    }
                    
                    //servlet.getLogger().debug("After send: req={}, sent={}, queue={}, canceled={}",
                    //    requested.get(), numSent, itemQueue.size(), canceled);
                }
                
                maybeFetchFromStorage();
            }
            catch (Exception e)
            {
                subscriber.onError(e);
            }
        }

        @Override
        public void cancel()
        {
            canceled = true;
        }
    }

    
    public SystemDataProvider(final SOSServlet servlet, final IObsSystemDatabase database, final ScheduledExecutorService threadPool, final SystemDataProviderConfig config)
    {
        this.servlet = Asserts.checkNotNull(servlet, SOSServlet.class);
        this.database = Asserts.checkNotNull(database, IObsSystemDatabase.class);
        this.threadPool = Asserts.checkNotNull(threadPool, ExecutorService.class);
        this.config = Asserts.checkNotNull(config, "config");
        this.foiIdEncoder = servlet.getParentHub().getIdEncoders().getFoiIdEncoder();
    }


    @Override
    public CompletableFuture<RecordTemplate> getResultTemplate(GetResultTemplateRequest req) throws SOSException
    {
        String sysUID = getProcedureUID(req.getOffering());
        
        var dsFilter = new DataStreamFilter.Builder()
            .withSystems().withUniqueIDs(sysUID).done()
            .withCurrentVersion();
        
        String outputName = SOSProviderUtils.getOutputNameFromObservableURIs(req.getObservables());
        if (outputName != null)
        {
            dsFilter.withOutputNames(outputName);
            req.getObservables().clear();
        }
        else
            dsFilter.withObservedProperties(req.getObservables());            

        return CompletableFuture.supplyAsync(() -> {
            // get datastream entry from obs store
            var dsEntry = database.getDataStreamStore()
                .selectEntries(dsFilter.build())
                .findFirst()
                .orElseThrow(() -> new CompletionException(
                    new SOSException(SOSException.invalid_param_code, "observedProperty", null, 
                        "No data found for observed properties " + req.getObservables())));

            // save datastream info for reuse in getResults()
            selectedDataStream = new DataStreamInfoCache(
                dsEntry.getKey().getInternalID(),
                dsEntry.getValue());
            return selectedDataStream.resultTemplate;

        }, threadPool);
    }


    @Override
    public void getProcedureDescriptions(DescribeSensorRequest req, Subscriber<IProcedure> consumer) throws SOSException, IOException
    {
        // build filter
        var procFilter = new SystemFilter.Builder()
            .withUniqueIDs(req.getProcedureID());
        if (req.getTime() != null)
            procFilter.withValidTimeDuring(req.getTime());
        else
            procFilter.withCurrentVersion();
        
        // notify consumer with subscription
        consumer.onSubscribe(
            new StreamSubscription<>(
                consumer,
                100,
                database.getSystemDescStore().selectEntries(procFilter.build())
                    .map(entry -> {
                        var internalID = entry.getKey().getInternalID();
                        var smlProc = entry.getValue().getFullDescription();
                        
                        // add outputs and control inputs
                        return SystemUtils.addIOsFromDataStore(internalID, smlProc, database);
                    })
            )
        );
    }


    @Override
    public void getFeaturesOfInterest(GetFeatureOfInterestRequest req, Subscriber<IFeature> consumer) throws SOSException, IOException
    {
        // build filter
        var foiFilter = new FoiFilter.Builder();
        
        if (req.getFoiIDs() != null && !req.getFoiIDs().isEmpty())
            foiFilter.withUniqueIDs(req.getFoiIDs());
        
        if (req.getProcedures() != null && !req.getProcedures().isEmpty())
        {
            foiFilter.withObservations(new ObsFilter.Builder()
                .withSystems(new SystemFilter.Builder()
                    .withUniqueIDs(req.getProcedures())
                    .includeMembers(true)
                    .build())
                .build());
            /*foiFilter.withParents()
                .withUniqueIDs(req.getProcedures())
                .done();*/
        }
        
        if (req.getSpatialFilter() != null)
            foiFilter.withLocation(toDbFilter(req.getSpatialFilter()));
        
        foiFilter.withCurrentVersion();
        
        // use mapper to set feature ID from key
        var foiStream = database.getFoiStore().selectEntries(foiFilter.build()).map(e -> {
            return (IFeature)new FeatureWrapper(e.getValue()) {
                @Override
                public String getId()
                {
                    return foiIdEncoder.encodeID(e.getKey().getInternalID());
                }
            };
        });
        
        // notify consumer with subscription
        consumer.onSubscribe(
            new StreamSubscription<>(
                consumer,
                100,
                foiStream
            )
        );
    }
    
    
    protected ObsFilter getObsFilter(GetObservationRequest req, BigId dataStreamId) throws SOSException
    {
        return getObsFilter(req, dataStreamId, Long.MAX_VALUE);
    }
    
    
    protected ObsFilter getObsFilter(GetObservationRequest req, BigId dataStreamId, long limit) throws SOSException
    {
        // build obs query filter
        var obsFilter = new ObsFilter.Builder()
            .withLimit(limit);
        
        // select datastream(s)
        var dsFilter = new DataStreamFilter.Builder();
        if (dataStreamId == null)
        {
            if (req.getProcedures() != null && !req.getProcedures().isEmpty())
            {
                dsFilter.withSystems()
                    .withUniqueIDs(req.getProcedures())
                    .done();
            }
        }
        else
            dsFilter.withInternalIDs(dataStreamId);
            
        // observables
        if (req.getObservables() != null && !req.getObservables().isEmpty())
            dsFilter.withObservedProperties(req.getObservables());
        obsFilter.withDataStreams(dsFilter.build());
        
        // FOIs by ID
        if (req.getFoiIDs() != null && !req.getFoiIDs().isEmpty())
        {
            obsFilter.withFois()
                .withUniqueIDs(req.getFoiIDs())
                .done();
        }
        
        // or FOI spatial filter
        else if (req.getSpatialFilter() != null)
        {
            obsFilter.withFois()
                .withLocation(toDbFilter(req.getSpatialFilter()))
                .done();
        }
        
        // phenomenon time filter
        TemporalFilter phenomenonTimeFilter;     
        if (req.getTime() == null)
        {
            // all records case
            phenomenonTimeFilter = new TemporalFilter.Builder()
                .withAllTimes()
                .build();
        }
        else if (req.getTime().isNow())
        {
            // latest record case
            phenomenonTimeFilter = new TemporalFilter.Builder()
                .withCurrentTime()
                .build();
        }
        else
        {
            // time range case
            phenomenonTimeFilter = new TemporalFilter.Builder()
                .withRange(req.getTime().begin(), req.getTime().end())
                .build();
        }            
        obsFilter.withPhenomenonTime(phenomenonTimeFilter);
        
        return obsFilter.build();
    }


    protected SpatialFilter toDbFilter(SpatialOps spatialFilter) throws SOSException
    {
        if (spatialFilter instanceof BBOX)
        {
            var geom = SOSProviderUtils.toRoiGeometry((BBOX) spatialFilter);
            return new SpatialFilter.Builder()
                .withRoi(geom)
                .build();
        }
        else if (spatialFilter instanceof DistanceBuffer)
        {
            var binaryOp2 = ((DistanceBuffer) spatialFilter).getOperand2();
            var gmlGeom = (AbstractGeometry) ((GMLExpression) binaryOp2).getGmlObject();
            var jtsGeom = JTSUtils.getAsJTSGeometry(gmlGeom);
            var builder = new SpatialFilter.Builder();

            if (spatialFilter instanceof DWithin)
            {
                double dist = ((DWithin) spatialFilter).getDistance().getValue();
                builder.withDistanceToPoint((Point) jtsGeom, dist);
                return builder.build();
            }
        }
        else if (spatialFilter instanceof BinarySpatialOp)
        {
            var binaryOp2 = ((BinarySpatialOp) spatialFilter).getOperand2();

            if (binaryOp2 instanceof GMLExpression &&
                ((GMLExpression) binaryOp2).getGmlObject() instanceof AbstractGeometry)
            {
                var gmlGeom = (AbstractGeometry) ((GMLExpression) binaryOp2).getGmlObject();
                var jtsGeom = JTSUtils.getAsJTSGeometry(gmlGeom);

                var builder = new SpatialFilter.Builder()
                    .withRoi(jtsGeom);

                if (spatialFilter instanceof net.opengis.fes.v20.Contains)
                    builder.withOperator(SpatialOp.CONTAINS);
                else if (spatialFilter instanceof net.opengis.fes.v20.Intersects)
                    builder.withOperator(SpatialOp.INTERSECTS);
                else if (spatialFilter instanceof net.opengis.fes.v20.Equals)
                    builder.withOperator(SpatialOp.EQUALS);
                else if (spatialFilter instanceof net.opengis.fes.v20.Disjoint)
                    builder.withOperator(SpatialOp.DISJOINT);
                else if (spatialFilter instanceof net.opengis.fes.v20.Within)
                    builder.withOperator(SpatialOp.WITHIN);

                return builder.build();
            }
        }

        throw new SOSException(SOSException.unsupported_op_code, null, null, "Unsupported spatial operator");
    }


    @Override
    public boolean hasMultipleProducers()
    {
        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public void close()
    {
        // TODO Auto-generated method stub

    }


    public String getProcedureUID(String offeringID)
    {
        return servlet.getProcedureUID(offeringID);
    }


    @Override
    public long getTimeout()
    {
        return (long)(config.liveDataTimeout * 1000);
    }

}