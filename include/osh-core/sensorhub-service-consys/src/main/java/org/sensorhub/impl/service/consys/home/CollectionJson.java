/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.home;

import java.io.IOException;
import java.util.Collection;
import org.sensorhub.impl.service.consys.home.CollectionHandler.CollectionInfo;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.resource.ResourceBindingJson;
import org.sensorhub.impl.service.consys.resource.ResourceLink;
import com.google.common.base.Strings;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;


public class CollectionJson extends ResourceBindingJson<String, CollectionInfo>
{
    
    public CollectionJson(RequestContext ctx) throws IOException
    {
        super(ctx, null, false);
    }
    
    
    @Override
    public void serialize(String key, CollectionInfo col, boolean showLinks, JsonWriter writer) throws IOException
    {
        writer.beginObject();
        writer.name("id").value(col.id);
        writer.name("title").value(col.title);
        if (!Strings.isNullOrEmpty(col.description))
            writer.name("description").value(col.description);
        if (!Strings.isNullOrEmpty(col.attribution))
            writer.name("attribution").value(col.attribution);
        writer.name("itemType").value(col.itemType);
        if (!Strings.isNullOrEmpty(col.featureType))
            writer.name("featureType").value(col.featureType);
        writeLinksAsJson(writer, col.links);
        writer.endObject();
        writer.flush();
    }


    @Override
    public CollectionInfo deserialize(JsonReader reader) throws IOException
    {
        // this should never be called since home page is read-only
        throw new UnsupportedOperationException();
    }
    
    
    @Override
    public void startCollection() throws IOException
    {
        writer.beginObject();
        writer.name("collections");
        writer.beginArray();
    }


    @Override
    public void endCollection(Collection<ResourceLink> links) throws IOException
    {
        writer.endArray(); // end items list
        writeLinksAsJson(writer, links);
        writer.endObject();
        writer.flush();
    }
}
