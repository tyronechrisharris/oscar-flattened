/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.sensorml;

import java.io.IOException;
import java.util.Collection;
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.api.feature.ISmlFeature;
import org.sensorhub.api.procedure.IProcedureWithDesc;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.impl.service.consys.ResourceParseException;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.resource.ResourceBindingJson;
import org.sensorhub.impl.service.consys.resource.ResourceLink;
import org.sensorhub.impl.system.wrapper.ProcessWrapper;
import org.sensorhub.impl.system.wrapper.SmlFeatureWrapper;
import org.vast.sensorML.SMLJsonBindings;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.opengis.sensorml.v20.AbstractProcess;


/**
 * <p>
 * SensorML JSON formatter for system resources
 * </p>
 * 
 * @param <V> Type of SML feature resource
 *
 * @author Alex Robin
 * @since Jan 26, 2021
 */
public abstract class SmlProcessBindingSmlJson<V extends ISmlFeature<?>> extends ResourceBindingJson<FeatureKey, V>
{
    SMLJsonBindings smlBindings;
    SMLConverter converter;
    
    
    public SmlProcessBindingSmlJson(RequestContext ctx, IdEncoders idEncoders, boolean forReading) throws IOException
    {
        super(ctx, idEncoders, forReading);
        this.smlBindings = new SMLJsonBindings();
        this.converter = new SMLConverter();
    }


    @Override
    public V deserialize(JsonReader reader) throws IOException
    {
        // if array, prepare to parse first element
        if (reader.peek() == JsonToken.BEGIN_ARRAY)
            reader.beginArray();
        
        if (reader.peek() == JsonToken.END_DOCUMENT || !reader.hasNext())
            return null;
        
        try
        {
            var sml = smlBindings.readAbstractProcess(reader);
            
            @SuppressWarnings("unchecked")
            var wrapper = (V)new SmlFeatureWrapper(sml);
            return wrapper;
        }
        catch (JsonParseException e)
        {
            throw new ResourceParseException(INVALID_JSON_ERROR_MSG + e.getMessage(), e);
        }
    }


    @Override
    public void serialize(FeatureKey key, V res, boolean showLinks, JsonWriter writer) throws IOException
    {
        try
        {
            var sml = res.getFullDescription();
            if (sml == null)
            {
                if (res instanceof ISystemWithDesc)
                    sml = new SMLConverter().genericFeatureToSystem(res);
                else if (res instanceof IProcedureWithDesc)
                    sml = new SMLConverter().genericFeatureToProcedure(res);
                else
                    throw new IOException("Cannot convert feature to SensorML");
            }
            
            if (key != null)
            {
                var idStr = encodeID(key);
                sml = ProcessWrapper.getWrapper((AbstractProcess)sml).withId(idStr);
            }
            
            smlBindings.writeDescribedObject(writer, sml);
            writer.flush();
        }
        catch (IllegalStateException e)
        {
            if (e.getCause() instanceof IOException)
                throw (IOException)e.getCause();
            else
                throw e;
        }
        catch (Exception e)
        {
            throw new IOException("Error writing SensorML JSON", e);
        }
    }
    
    
    protected abstract String encodeID(FeatureKey key);


    @Override
    public void startCollection() throws IOException
    {
        startJsonCollection(writer);
    }


    @Override
    public void endCollection(Collection<ResourceLink> links) throws IOException
    {
        endJsonCollection(writer, links);
    }
}
