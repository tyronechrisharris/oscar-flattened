/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2017 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.processing;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.function.Consumer;
import org.sensorhub.api.ISensorHub;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.data.IObsData;
import org.sensorhub.api.data.ObsEvent;
import org.sensorhub.api.datastore.obs.DataStreamFilter;
import org.sensorhub.api.datastore.obs.ObsFilter;
import org.sensorhub.api.event.EventUtils;
import org.sensorhub.api.processing.OSHProcessInfo;
import org.sensorhub.utils.Async;
import org.vast.process.ExecutableProcessImpl;
import org.vast.process.ProcessException;
import org.vast.process.ProcessInfo;
import org.vast.swe.SWEHelper;
import com.google.common.base.Objects;
import net.opengis.swe.v20.Text;


/**
 * <p>
 * Process implementation used to feed data from a single data stream
 * (received from event bus) into a SensorML processing chain.
 * </p>
 *
 * @author Alex Robin
 * @since May 10, 2023
 */
public class DataStreamSource extends ExecutableProcessImpl implements ISensorHubProcess
{
    public static final OSHProcessInfo INFO = new OSHProcessInfo("datasource:datastream", "DataStream Source", null, DataStreamSource.class);
    public static final String SYSTEM_UID_PARAM = "systemUID";
    public static final String OUTPUT_NAME_PARAM = "outputName";
    
    final Text systemUidParam;
    final Text outputNameParam;
    ISensorHub hub;
    String systemUid;
    String outputName;
    BigId datastreamId;
    boolean paused;
    Subscription sub;
    
    
    public DataStreamSource()
    {
        super(INFO);
        SWEHelper fac = new SWEHelper();
        
        // params
        systemUidParam = fac.createText()
            .definition(SWEHelper.getPropertyUri("SystemUID"))
            .label("Producer Unique ID")
            .build();
        paramData.add(SYSTEM_UID_PARAM, systemUidParam);
        
        outputNameParam = fac.createText()
            .definition(SWEHelper.getPropertyUri("OutputName"))
            .label("Output Name")
            .build();
        paramData.add(OUTPUT_NAME_PARAM, outputNameParam);
        
        // output cannot be created until source URI param is set
    }


    @Override
    public void notifyParamChange()
    {
        systemUid = systemUidParam.getData().getStringValue();
        outputName = outputNameParam.getData().getStringValue();
        
        if (systemUid != null && outputName != null)
        {
            try {
                // wait here to make sure datasource and its datastreams have been registered.
                // needed to handle case where datasource is being registered concurrently.
                Async.waitForCondition(this::checkForDataSource, 500, 10000);
            } catch (TimeoutException e) {
                if (processInfo == null)
                    throw new IllegalStateException("System " + systemUid + " not found", e);
                else
                    throw new IllegalStateException("System " + systemUid + " is missing output " + outputName, e);
            }
        }
    }
    
    
    protected boolean checkForDataSource()
    {
        var db = hub.getDatabaseRegistry().getFederatedDatabase();
        var sysEntry = db.getSystemDescStore().getCurrentVersionEntry(systemUid);
        if (sysEntry == null)
            return false;
        
        // set process info
        ProcessInfo instanceInfo = new ProcessInfo(
                processInfo.getUri(),
                sysEntry.getValue().getName(),
                processInfo.getDescription(),
                processInfo.getImplementationClass());
        this.processInfo = instanceInfo;
        
        // get datastream corresponding to outputName
        db.getDataStreamStore().selectEntries(new DataStreamFilter.Builder()
                .withSystems(sysEntry.getKey().getInternalID())
                .withOutputNames(outputName)
                .withCurrentVersion()
                .build())
            .forEach(entry -> {
                datastreamId = entry.getKey().getInternalID();
                var ds = entry.getValue();
                outputData.add(ds.getOutputName(), ds.getRecordStructure().copy());
            });
        
        return !outputData.isEmpty();
    }
    
    
    @Override
    public void start(Consumer<Throwable> onError) throws ProcessException
    {
        start(null, onError);
    }
    
    
    @Override
    public synchronized void start(ExecutorService threadPool, Consumer<Throwable> onError) throws ProcessException
    {
        if (!started)
        {
            started = true;
            
            var output = outputData.getComponent(outputName);
            var topic = EventUtils.getDataStreamDataTopicID(systemUid, output.getName());
            
            hub.getEventBus().newSubscription(ObsEvent.class)
                .withTopicID(topic)
                .subscribe(new Subscriber<ObsEvent>() {
                    volatile Instant latestObsTime;
                    volatile boolean needDedup;
                   
                    @Override
                    public void onSubscribe(Subscription subscription)
                    {
                        // first publish latest obs in case streaming is not continuous
                        var db = hub.getDatabaseRegistry().getFederatedDatabase();
                        db.getObservationStore().select(new ObsFilter.Builder()
                            .withDataStreams(datastreamId)
                            .withLatestResult()
                            .build()).findFirst().ifPresent(latestObs -> {
                                latestObsTime = latestObs.getResultTime();
                                publishObs(latestObs);
                                needDedup = true;
                            });
                        
                        sub = subscription;
                        subscription.request(Long.MAX_VALUE);
                    }
    
                    @Override
                    public void onNext(ObsEvent e)
                    {
                        if (paused)
                            return;
                        
                        // publish each obs to process chain
                        for (var obs: e.getObservations())
                            publishObs(obs);
                    }
                    
                    protected void publishObs(IObsData obs)
                    {
                        try
                        {
                            if (needDedup)
                            {
                                needDedup = false;
                                if (Objects.equal(latestObsTime, obs.getResultTime()))
                                    return;
                            }
                            
                            output.setData(obs.getResult());
                            publishData(outputName);
                        }
                        catch (InterruptedException ex)
                        {
                            Thread.currentThread().interrupt();
                        }
                    }
    
                    @Override
                    public void onError(Throwable throwable)
                    {
                        getLogger().error("Error receiving data from event bus", throwable);
                        
                        if (onError != null)
                            onError.accept(throwable);
                    }
    
                    @Override
                    public void onComplete()
                    {
                        getLogger().info("No more data expected from data source");
                    }
                });
            
            getLogger().debug("Connected to data source '{}'", systemUid);
        }
    }


    @Override
    public synchronized void stop()
    {
        if (started)
        {
            started = false;
            if (sub != null)
                sub.cancel();
            sub = null;
            getLogger().debug("Disconnected from data source '{}'", systemUid);
        }
    }


    public void pause()
    {
        this.paused = true;
    }


    public void resume()
    {
        this.paused = false;
    }


    @Override
    public void execute() throws ProcessException
    {
        // nothing to do, data is just pushed to outputs
        // when events are received
    }


    @Override
    public boolean needSync()
    {
        return false;
    }


    @Override
    public void setParentHub(ISensorHub hub)
    {
        this.hub = hub;
    }

}
