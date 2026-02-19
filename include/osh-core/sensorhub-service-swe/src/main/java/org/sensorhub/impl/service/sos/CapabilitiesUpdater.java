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

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.api.datastore.obs.DataStreamFilter;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.api.system.ISystemWithDesc;
import org.vast.data.DataIterator;
import org.vast.ogc.om.IObservation;
import org.vast.ows.sos.SOSOfferingCapabilities;
import org.vast.ows.swe.SWESOfferingCapabilities;
import org.vast.swe.SWEConstants;
import org.vast.util.TimeExtent;
import com.google.common.base.Strings;
import net.opengis.swe.v20.DataArray;
import net.opengis.swe.v20.DataRecord;
import net.opengis.swe.v20.Vector;


public class CapabilitiesUpdater
{
    public static final String OFFERING_NAME_PLACEHOLDER = "{%offering_name%}";
    public static final String PROC_UID_PLACEHOLDER = "{%procedure_uid%}";
    public static final String PROC_NAME_PLACEHOLDER = "{%procedure_name%}";
    public static final String PROC_DESC_PLACEHOLDER = "{%procedure_description%}";
    
    
    public void updateOfferings(final SOSServlet servlet)
    {
        Map<String, SOSOfferingCapabilities> offerings = new LinkedHashMap<>();
        
        var obsDb = servlet.getReadDatabase();
        var providerConfigs = servlet.providerConfigs;
        
        obsDb.getSystemDescStore().selectEntries(new SystemFilter.Builder().build())
            .forEach(entry -> {
                var sysID = entry.getKey().getInternalID();
                var proc = entry.getValue();
                
                String sysUID = proc.getUniqueIdentifier();
                var customConfig = providerConfigs.get(sysUID);
                
                // retrieve or create new offering
                SOSOfferingCapabilities offering = offerings.get(sysUID);
                if (offering == null)
                {
                    offering = new SOSOfferingCapabilities();
                    offering.setIdentifier(sysUID); // use system UID as offering ID
                    offering.getProcedures().add(sysUID);
                    
                    // use name and description from custom config if set
                    // otherwise default to name and description of system 
                    offering.setTitle(customConfig != null && !Strings.isNullOrEmpty(customConfig.name) ?
                        replaceVariables(customConfig.name, proc, customConfig) : proc.getName());
                    offering.setDescription(customConfig != null && !Strings.isNullOrEmpty(customConfig.description) ?
                        replaceVariables(customConfig.description, proc, customConfig) : proc.getDescription());
                    
                    // add supported formats
                    offering.getResponseFormats().add(SWESOfferingCapabilities.FORMAT_OM2);
                    offering.getResponseFormats().add(SWESOfferingCapabilities.FORMAT_OM2_JSON);
                    offering.getProcedureFormats().add(SWESOfferingCapabilities.FORMAT_SML2);
                    offering.getProcedureFormats().add(SWESOfferingCapabilities.FORMAT_SML2_JSON);
                    
                    offerings.put(sysUID, offering);
                }
                
                // process all system datastreams
                var finalOffering = offering;
                var dsFilter = new DataStreamFilter.Builder()
                    .withSystems(sysID)
                    .build();
                
                obsDb.getDataStreamStore().select(dsFilter)
                   .forEach(dsInfo -> {
                       
                       // if we have no catch all observed property URI, generate an output URI
                       if (!SOSProviderUtils.hasCatchAllObservedProperty(dsInfo.getRecordStructure()))
                       {
                           String defUri = SOSProviderUtils.getOutputURI(dsInfo.getOutputName());
                           finalOffering.getObservableProperties().add(defUri);
                       }
                       
                       // iterate through all SWE components and add all definition URIs as observables
                       // this way only composites with URI will get added
                       DataIterator it = new DataIterator(dsInfo.getRecordStructure());
                       while (it.hasNext())
                       {
                           var comp = it.next();
                           
                           // skip vector coordinates if parent vector has a def
                           if (comp.getParent() instanceof Vector && comp.getParent().getDefinition() != null)
                               continue;
                           
                           String defUri = comp.getDefinition();
                           if (defUri != null &&
                               !defUri.equals(SWEConstants.DEF_SAMPLING_TIME) &&
                               !defUri.equals(SWEConstants.DEF_PHENOMENON_TIME))
                               finalOffering.getObservableProperties().add(defUri);
                       }
                       
                       // add obs types
                       finalOffering.getObservationTypes().add(IObservation.OBS_TYPE_GENERIC);
                       finalOffering.getObservationTypes().add(IObservation.OBS_TYPE_SCALAR);
                       finalOffering.getObservationTypes().add(getObservationType(dsInfo));                       
                                              
                       // add time range                       
                       var timeRange = dsInfo.getPhenomenonTimeRange();
                       if (timeRange != null)
                       {
                           if (finalOffering.getPhenomenonTime() == null)
                               finalOffering.setPhenomenonTime(timeRange);
                           else
                               finalOffering.setPhenomenonTime(
                                   TimeExtent.span(finalOffering.getPhenomenonTime(), timeRange));
                       }
                   });
                                
                var phenTimeRange = offering.getPhenomenonTime();
                
                // set end to 'now' if timeout not reached yet
                var timeOut = servlet.config.defaultLiveTimeout;
                if (customConfig instanceof SystemDataProviderConfig)
                    timeOut = ((SystemDataProviderConfig)customConfig).liveDataTimeout;
                
                if (phenTimeRange != null &&
                    phenTimeRange.end().isAfter(Instant.now().minusMillis((long)(timeOut*1000.))))
                    offering.setPhenomenonTime(TimeExtent.endNow(phenTimeRange.begin()));
            });
        
        servlet.capabilities.getLayers().clear();
        servlet.capabilities.getLayers().addAll(offerings.values());
    }
    
    
    protected String getObservationType(IDataStreamInfo dsInfo)
    {
        // obs type depends on top-level component
        var recordStruct = dsInfo.getRecordStructure();
        if (recordStruct instanceof DataRecord)
            return IObservation.OBS_TYPE_RECORD;
        else if (recordStruct instanceof DataArray)
            return IObservation.OBS_TYPE_ARRAY;
        else
            return IObservation.OBS_TYPE_SCALAR;
    }
    
    
    protected String replaceVariables(String textField, ISystemWithDesc proc, SOSProviderConfig config)
    {
        textField = textField.replace(PROC_UID_PLACEHOLDER, proc.getUniqueIdentifier());
        
        if (config.name != null)
            textField = textField.replace(OFFERING_NAME_PLACEHOLDER, config.name);
        else if (proc.getName() != null)
            textField = textField.replace(OFFERING_NAME_PLACEHOLDER, proc.getName());
        
        if (proc.getName() != null)
            textField = textField.replace(PROC_NAME_PLACEHOLDER, proc.getName());
        
        if (proc.getDescription() != null)
            textField = textField.replace(PROC_DESC_PLACEHOLDER, proc.getDescription());
        
        return textField;
    }
}
