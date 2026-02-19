/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.

******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.feature;

import java.io.IOException;
import java.util.Set;
import org.vast.swe.SWEConstants;
import org.vast.swe.fast.JsonDataWriterGson;
import org.vast.util.WriterException;
import com.google.common.collect.ImmutableSet;
import com.google.gson.stream.JsonWriter;
import net.opengis.swe.v20.DataArray;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataChoice;
import net.opengis.swe.v20.DataRecord;


/**
 * <p>
 * Custom JSON writer for serializing variable feature property values
 * within a feature of interest object
 * </p>
 *
 * @author Alex Robin
 * @since Jun 24, 2022
 */
public class DynamicPropsWriter extends JsonDataWriterGson
{
    static final Set<String> SKIPPED_DEFS = ImmutableSet.of(
        SWEConstants.DEF_PHENOMENON_TIME,
        SWEConstants.DEF_SAMPLING_TIME,
        SWEConstants.DEF_FORECAST_TIME,
        SWEConstants.DEF_RUN_TIME
        );
    
    static final Set<String> SKIPPED_ROLES = ImmutableSet.of(
        SWEConstants.DEF_PHENOMENON_TIME,
        SWEConstants.DEF_SYSTEM_ID
    );
    
    
    protected class RootWriter extends RecordProcessor implements JsonAtomWriter
    {
        @Override
        public int process(DataBlock data, int index) throws IOException
        {
            try
            {
                for (AtomProcessor p: fieldProcessors)
                {
                    if (p.isEnabled())
                        writer.name(((JsonAtomWriter)p).getEltName());
                    index = p.process(data, index);
                }
                
                return index;
            }
            catch (IOException e)
            {
                throw new WriterException("Error writing variable feature properties", e);
            }
        }

        @Override
        public String getEltName()
        {
            return null;
        }
    }
    
    
    public DynamicPropsWriter(JsonWriter writer)
    {
        this.writer = writer;
        setDataComponentFilter(comp -> {
            if (comp.getParent() == null)
                return false;
            if (comp instanceof DataArray)
                return false;
            if (comp instanceof DataChoice)
                return false;
            if (SKIPPED_DEFS.contains(comp.getDefinition()))
                return false;
            if (DynamicGeomScanner.isGeom(comp) || DynamicGeomScanner.isGeom(comp.getParent()))
                return false;
            
            if (comp.getParent() instanceof DataRecord)
            {
                var prop = ((DataRecord)comp.getParent()).getFieldList().getProperty(comp.getName());
                var role = prop.getRole();
                if (SKIPPED_ROLES.contains(role))
                    return false;
            }
            
            return true;
        });
    }
    
    
    public void initProcessTree()
    {
        checkEnabled(dataComponents);
        dataComponents.accept(this);
        processorTreeReady = true;
    }


    @Override
    protected void init()
    {
    }


    @Override
    protected RecordProcessor getRecordProcessor(DataRecord record)
    {
        if (record.getParent() == null)
            return new RootWriter();
        else
            return super.getRecordProcessor(record);
    }
}
