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

import java.util.concurrent.TimeoutException;
import org.sensorhub.api.ISensorHub;
import org.sensorhub.api.command.CommandData;
import org.sensorhub.api.datastore.command.CommandStreamFilter;
import org.sensorhub.api.processing.OSHProcessInfo;
import org.sensorhub.impl.system.CommandStreamTransactionHandler;
import org.sensorhub.impl.system.SystemDatabaseTransactionHandler;
import org.sensorhub.utils.Async;
import org.vast.process.ExecutableProcessImpl;
import org.vast.process.ProcessException;
import org.vast.process.ProcessInfo;
import org.vast.swe.SWEHelper;
import net.opengis.swe.v20.Text;


/**
 * <p>
 * Process implementation used to send data to a system taskable input.
 * </p>
 *
 * @author Alex Robin
 * @since May 14, 2023
 */
public class CommandStreamSink extends ExecutableProcessImpl implements ISensorHubProcess
{
    public static final OSHProcessInfo INFO = new OSHProcessInfo("datasink:commandstream", "Command Sink", null, CommandStreamSink.class);
    public static final String SYSTEM_UID_PARAM = "systemUID";
    public static final String OUTPUT_NAME_PARAM = "inputName";
    
    final Text systemUidParam;
    final Text inputNameParam;
    String systemUid;
    String inputName;
    ISensorHub hub;
    CommandStreamTransactionHandler commandHandler;
    
    
    public CommandStreamSink()
    {
        super(INFO);
        SWEHelper fac = new SWEHelper();
        
        // params
        systemUidParam = fac.createText()
            .definition(SWEHelper.getPropertyUri("SystemUID"))
            .label("Producer Unique ID")
            .build();
        paramData.add(SYSTEM_UID_PARAM, systemUidParam);
        
        inputNameParam = fac.createText()
            .definition(SWEHelper.getPropertyUri("OutputName"))
            .label("Output Name")
            .build();
        paramData.add(OUTPUT_NAME_PARAM, inputNameParam);
        
        // output cannot be created until source URI param is set
    }


    @Override
    public void notifyParamChange()
    {
        systemUid = systemUidParam.getData().getStringValue();
        inputName = inputNameParam.getData().getStringValue();
        
        if (systemUid != null && inputName != null)
        {
            try {
                // wait here to make sure parent system and control stream have been registered.
                // needed to handle case where system is being registered concurrently.
                Async.waitForCondition(this::checkForControlStream, 500, 10000);
            } catch (TimeoutException e) {
                if (processInfo == null)
                    throw new IllegalStateException("System " + systemUid + " not found", e);
                else
                    throw new IllegalStateException("System " + systemUid + " is missing input " + inputName, e);
            }
        }
    }
    
    
    protected boolean checkForControlStream()
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
        
        // get control stream corresponding to inputName
        db.getCommandStreamStore().selectEntries(new CommandStreamFilter.Builder()
                .withSystems(sysEntry.getKey().getInternalID())
                .withControlInputNames(inputName)
                .withCurrentVersion()
                .build())
            .forEach(entry -> {
                // add input with same schema as control stream
                inputData.add(
                    entry.getValue().getControlInputName(),
                    entry.getValue().getRecordStructure().copy());
                
                // also get handle to handler used to send commands
                var systemHandler = new SystemDatabaseTransactionHandler(hub.getEventBus(), hub.getDatabaseRegistry().getFederatedDatabase());
                commandHandler = systemHandler.getCommandStreamHandler(entry.getKey().getInternalID());
            });
        
        return !inputData.isEmpty();
    }


    @Override
    public synchronized void stop()
    {
        if (started)
        {
            started = false;
            getLogger().debug("Disconnected from system '{}'", systemUid);
        }
    }


    @Override
    public void execute() throws ProcessException
    {
        // read from input
        var cmdData = inputData.getComponent(0).getData();
        
        // send to control stream
        var cmd = new CommandData.Builder()
            .withCommandStream(commandHandler.getCommandStreamKey().getInternalID())
            .withParams(cmdData)
            .withSender(getProcessInfo().getUri() + "#" + getInstanceName())
            .build();
        
        commandHandler.submitCommand(0, cmd).thenAccept(s -> {
            
        });
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
