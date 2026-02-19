/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.obs;

import java.io.IOException;
import java.util.Collection;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.datastore.obs.ObsStats;
import org.sensorhub.api.feature.FeatureId;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.resource.ResourceBindingJson;
import org.sensorhub.impl.service.consys.resource.ResourceLink;
import org.vast.json.JsonInliningWriter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;


public class ObsStatsBindingJson extends ResourceBindingJson<BigId, ObsStats>
{
    
    public ObsStatsBindingJson(RequestContext ctx, IdEncoders idEncoders) throws IOException
    {
        super(ctx, idEncoders, false);
    }
    
    
    @Override
    public ObsStats deserialize(JsonReader reader) throws IOException
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public void serialize(BigId key, ObsStats stats, boolean showLinks, JsonWriter writer) throws IOException
    {
        var dsId = idEncoders.getDataStreamIdEncoder().encodeID(stats.getDataStreamID());
        
        writer.beginObject();
        writer.name("datastream@id").value(dsId);
        
        if (stats.getFoiID() != null && stats.getFoiID() != FeatureId.NULL_FEATURE)
        {
            var foiId = idEncoders.getFoiIdEncoder().encodeID(stats.getFoiID().getInternalID());
            writer.name("foi@id").value(foiId);
        }
        
        if (stats.getPhenomenonTimeRange() != null)
        {
            writer.name("phenomenonTime").beginArray()
                .value(stats.getPhenomenonTimeRange().begin().toString())
                .value(stats.getPhenomenonTimeRange().end().toString())
                .endArray();
        }

        if (stats.getResultTimeRange() != null)
        {
            writer.name("resultTime").beginArray()
                .value(stats.getResultTimeRange().begin().toString())
                .value(stats.getResultTimeRange().end().toString())
                .endArray();
        }
        
        writer.name("obsCount").value(stats.getTotalObsCount());
        
        if (stats.getObsCountsByTime() != null)
        {
            writer.name("histogram").beginArray();
            ((JsonInliningWriter)writer).writeInline(true);
            for (int val: stats.getObsCountsByTime())
                writer.value(val);
            writer.endArray();
            ((JsonInliningWriter)writer).writeInline(false);
        }
        
        writer.endObject();
        writer.flush();
    }


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
