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

import org.sensorhub.api.command.ICommandStreamInfo;
import org.sensorhub.api.command.IStreamingControlInterface;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.api.data.IStreamingDataInterface;
import org.sensorhub.api.system.ISystemDriver;
import org.sensorhub.api.utils.OshAsserts;
import org.vast.util.Asserts;


public class EventUtils
{
    public static final String MODULE_TOPIC_PREFIX = "modules/";
    public static final String SYSTEM_TOPIC_PREFIX = "systems/";
    public static final String SYSTEM_OUTPUT_CHANNELS = "/outputs/";
    public static final String SYSTEM_COMMAND_CHANNELS = "/commands/";
    public static final String STATUS_CHANNEL = "/status";
    public static final String DATA_CHANNEL = "/data";
    public static final String ACK_CHANNEL = "/ack";
    
    
    private EventUtils() {}
    
    
    public static final String getModuleRegistryTopicID()
    {
        return MODULE_TOPIC_PREFIX;
    }
    
    
    public static final String getModuleTopicID(String moduleID)
    {
        OshAsserts.checkValidUID(moduleID, "ModuleID");
        return MODULE_TOPIC_PREFIX + moduleID;
    }
    
    
    public static final String getSystemRegistryTopicID()
    {
        return SYSTEM_TOPIC_PREFIX;
    }
    
    
    public static final String getSystemPublisherGroupID(String systemUID)
    {
        OshAsserts.checkValidUID(systemUID);
        return SYSTEM_TOPIC_PREFIX + systemUID;
    }
    
    
    public static final String getSystemStatusTopicID(String systemUID)
    {
        OshAsserts.checkValidUID(systemUID);
        return SYSTEM_TOPIC_PREFIX + systemUID + STATUS_CHANNEL;
    }
    
    
    public static final String getSystemStatusTopicID(ISystemDriver driver)
    {
        return getSystemStatusTopicID(driver.getUniqueIdentifier());
    }
    
    
    static final String getDataStreamTopicID(String systemUID, String outputName)
    {
        OshAsserts.checkValidUID(systemUID);
        Asserts.checkNotNullOrEmpty(outputName, "outputName");
        return SYSTEM_TOPIC_PREFIX + systemUID + SYSTEM_OUTPUT_CHANNELS + outputName;
    }
    
    
    public static final String getDataStreamStatusTopicID(String systemUID, String outputName)
    {
        return getDataStreamTopicID(systemUID, outputName) + STATUS_CHANNEL;
    }
    
    
    public static final String getDataStreamStatusTopicID(IStreamingDataInterface outputInterface)
    {
        return getDataStreamStatusTopicID(
            outputInterface.getParentProducer().getUniqueIdentifier(),
            outputInterface.getName());
    }
    
    
    public static final String getDataStreamStatusTopicID(IDataStreamInfo dsInfo)
    {
        return getDataStreamStatusTopicID(
            dsInfo.getSystemID().getUniqueID(),
            dsInfo.getOutputName());
    }
    
    
    public static final String getDataStreamDataTopicID(String systemUID, String outputName)
    {
        return getDataStreamTopicID(systemUID, outputName) + DATA_CHANNEL;
    }
    
    
    public static final String getDataStreamDataTopicID(IStreamingDataInterface outputInterface)
    {
        return getDataStreamDataTopicID(
            outputInterface.getParentProducer().getUniqueIdentifier(),
            outputInterface.getName());
    }
    
    
    public static final String getDataStreamDataTopicID(IDataStreamInfo dsInfo)
    {
        return getDataStreamDataTopicID(
            dsInfo.getSystemID().getUniqueID(),
            dsInfo.getOutputName());
    }
    
    
    static final String getCommandStreamTopicID(String systemUID, String controlInputName)
    {
        OshAsserts.checkValidUID(systemUID);
        Asserts.checkNotNullOrEmpty(controlInputName, "controlInputName");
        return SYSTEM_TOPIC_PREFIX + systemUID + SYSTEM_COMMAND_CHANNELS + controlInputName;
    }
    
    
    public static final String getCommandStreamStatusTopicID(String systemUID, String controlInputName)
    {
        return getCommandStreamTopicID(systemUID, controlInputName) + STATUS_CHANNEL;
    }
    
    
    public static final String getCommandStreamStatusTopicID(IStreamingControlInterface controlInterface)
    {
        return getCommandStreamStatusTopicID(
            controlInterface.getParentProducer().getUniqueIdentifier(),
            controlInterface.getName());
    }
    
    
    public static final String getCommandStreamStatusTopicID(ICommandStreamInfo csInfo)
    {
        return getCommandStreamStatusTopicID(
            csInfo.getSystemID().getUniqueID(),
            csInfo.getControlInputName());
    }
    
    
    public static final String getCommandDataTopicID(String systemUID, String controlInputName)
    {
        return getCommandStreamTopicID(systemUID, controlInputName) + DATA_CHANNEL;
    }
    
    
    public static final String getCommandDataTopicID(IStreamingControlInterface controlInterface)
    {
        return getCommandDataTopicID(
            controlInterface.getParentProducer().getUniqueIdentifier(),
            controlInterface.getName());
    }
    
    
    public static final String getCommandDataTopicID(ICommandStreamInfo csInfo)
    {
        return getCommandDataTopicID(
            csInfo.getSystemID().getUniqueID(),
            csInfo.getControlInputName());
    }
    
    
    public static final String getCommandStatusTopicID(String systemUID, String controlInputName)
    {
        return getCommandStreamTopicID(systemUID, controlInputName) + ACK_CHANNEL;
    }
    
    
    public static final String getCommandStatusTopicID(IStreamingControlInterface controlInterface)
    {
        return getCommandStatusTopicID(
            controlInterface.getParentProducer().getUniqueIdentifier(),
            controlInterface.getName());
    }
    
    
    public static final String getCommandStatusTopicID(ICommandStreamInfo csInfo)
    {
        return getCommandStatusTopicID(
            csInfo.getSystemID().getUniqueID(),
            csInfo.getControlInputName());
    }
    
}
