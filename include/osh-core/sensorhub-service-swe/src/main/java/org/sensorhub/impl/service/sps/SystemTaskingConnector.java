/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sps;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.sensorhub.api.command.CommandData;
import org.sensorhub.api.command.CommandEvent;
import org.sensorhub.api.command.CommandStatus;
import org.sensorhub.api.command.ICommandStatus;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.command.CommandStreamFilter;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.api.event.IEventBus;
import org.sensorhub.impl.event.DelegatingSubscriberAdapter;
import org.sensorhub.impl.system.CommandStreamTransactionHandler;
import org.sensorhub.impl.system.SystemUtils;
import org.vast.data.DataBlockMixed;
import org.vast.data.SWEFactory;
import org.vast.util.Asserts;
import org.vast.util.TimeExtent;
import net.opengis.sensorml.v20.AbstractProcess;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataChoice;
import net.opengis.swe.v20.DataComponent;


public class SystemTaskingConnector implements ISPSConnector
{
    static final String WRAPPER_CHOICE_NAME = "root";
    static final long DEFAULT_SUBMIT_TIMEOUT = 2000;
    
    final SPSServlet servlet;
    final SystemTaskingConnectorConfig config;
    final IObsSystemDatabase db;
    final IEventBus eventBus;
    final String sysUID;
    Map<String, CommandStreamTransactionHandler> txnHandlers = new HashMap<>();
    List<String> possibleCommandNames;
    Random random = new SecureRandom();
    
    
    public SystemTaskingConnector(final SPSService service, final SystemTaskingConnectorConfig config)
    {
        this.servlet = Asserts.checkNotNull(service.getServlet(), SPSServlet.class);
        this.config = Asserts.checkNotNull(config, SPSConnectorConfig.class);
        this.sysUID = Asserts.checkNotNullOrEmpty(config.systemUID, "sysUID");
        this.db = Asserts.checkNotNull(service.getReadDatabase(), IObsSystemDatabase.class);
        this.eventBus = service.getParentHub().getEventBus();
    }
    
    
    @Override
    public Stream<AbstractProcess> getProcedureDescriptions(TimeExtent timeRange)
    {
        // build filter
        var procFilter = new SystemFilter.Builder()
            .withUniqueIDs(sysUID);
        if (timeRange != null)
            procFilter.withValidTimeDuring(timeRange);
        else
            procFilter.withCurrentVersion();
        
        return db.getSystemDescStore().selectEntries(procFilter.build())
            .map(entry -> {
                var internalID = entry.getKey().getInternalID();
                var smlProc = entry.getValue().getFullDescription();
                
                // add outputs and control inputs
                return SystemUtils.addIOsFromDataStore(internalID, smlProc, db);
            });
    }
    
    
    @Override
    public DataComponent getTaskingParams()
    {
        // process all procedure datastreams
        var csFilter = new CommandStreamFilter.Builder()
            .withSystems().withUniqueIDs(sysUID).done()
            .build();
        
        // always wrap with the choice so we have the control input name
        var commandChoice = new SWEFactory().newDataChoice();
        commandChoice.setName(WRAPPER_CHOICE_NAME);
        
        db.getCommandStreamStore().select(csFilter)
           .forEach(csInfo -> {
               commandChoice.addItem(csInfo.getControlInputName(), csInfo.getRecordStructure());
           });
        
        DataComponent taskingParams;
        if (commandChoice.getNumItems() == 1)
            taskingParams = commandChoice.getComponent(0);
        else
            taskingParams = commandChoice;
        
        computeCommandNames(taskingParams);
        return taskingParams;
    }
    
    
    @Override
    public void startDirectTasking(DataComponent taskingParams)
    {
        computeCommandNames(taskingParams);
    }
    
    
    protected void computeCommandNames(DataComponent taskingParams)
    {
        possibleCommandNames = new ArrayList<>();
        
        if (taskingParams instanceof DataChoice && WRAPPER_CHOICE_NAME.equals(taskingParams.getName()))
        {
            for (var item: ((DataChoice)taskingParams).getItemList())
                possibleCommandNames.add(item.getName());
        }
        else
            possibleCommandNames.add(taskingParams.getName());
    }
    
    
    @Override
    public CompletableFuture<ICommandStatus> sendCommand(DataBlock data, boolean waitForStatus)
    {
        String commandName;
        
        // if wrapped with a choice, extract actual command name and data
        if (possibleCommandNames.size() > 1)
        {
            int selectedIndex = data.getIntValue(0);
            commandName = possibleCommandNames.get(selectedIndex);
            data = ((DataBlockMixed)data).getUnderlyingObject()[1];
        }
        else
            commandName = possibleCommandNames.get(0);
        
        // reuse or create new transaction handler
        var txnHandler = txnHandlers.computeIfAbsent(commandName, key -> {
            return servlet.getSubmitTxnHandler().getCommandStreamHandler(sysUID, key);
        });
        
        // create the command
        var cmd = new CommandData.Builder()
            .withSender(servlet.getCurrentUser())
            .withCommandStream(txnHandler.getCommandStreamKey().getInternalID())
            .withParams(data)
            .build();
        
        if (waitForStatus)
        {
            return txnHandler.submitCommand(
                random.nextLong(), cmd, DEFAULT_SUBMIT_TIMEOUT, TimeUnit.MILLISECONDS);
        }
        else
        {
            txnHandler.submitCommandNoStatus(random.nextLong(), cmd);
            return null;
        }
    }
    
    
    @Override
    public void subscribeToCommands(DataComponent taskingParams, Subscriber<DataBlock> subscriber)
    {
        computeCommandNames(taskingParams);
        
        // subscribe to all command types for the associated procedure
        for (var commandName: possibleCommandNames)
        {
            // reuse or create new transaction handler
            var txnHandler = txnHandlers.computeIfAbsent(commandName, key -> {
                return servlet.getTransactionHandler().getCommandStreamHandler(sysUID, key);
            });
            
            txnHandler.connectCommandReceiver(new DelegatingSubscriberAdapter<CommandEvent, DataBlock>(subscriber) {
                @Override
                public void onNext(CommandEvent event)
                {
                    var cmd = event.getCommand();
                    subscriber.onNext(cmd.getParams());
                    
                    // for now, we cannot get feedback from the remote system
                    // so always report command has completed
                    txnHandler.sendStatus(
                        event.getCorrelationID(),
                        CommandStatus.completed(cmd.getID()));
                }
            });
        }
    }


    @Override
    public SPSConnectorConfig getConfig()
    {
        return config;
    }

}
