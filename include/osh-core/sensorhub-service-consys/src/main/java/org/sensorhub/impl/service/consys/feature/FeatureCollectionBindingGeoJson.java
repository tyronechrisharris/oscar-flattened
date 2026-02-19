/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.feature;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import javax.xml.namespace.QName;
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.database.IFeatureDatabase;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.vast.ogc.gml.GenericFeature;
import org.vast.ogc.gml.GenericFeatureImpl;
import org.vast.ogc.gml.GeoJsonBindings;
import org.vast.ogc.gml.IFeature;
import org.vast.ogc.gml.IFeatureCollection;
import org.vast.util.Asserts;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.opengis.gml.v32.AbstractGeometry;


public class FeatureCollectionBindingGeoJson extends AbstractFeatureBindingGeoJson<IFeature, IFeatureDatabase>
{
    public static final int EXTERNAL_ID_SEED = 7352181;
    
    
    FeatureCollectionBindingGeoJson(RequestContext ctx, IdEncoders idEncoders, IFeatureDatabase db, boolean forReading) throws IOException
    {
        super(ctx, idEncoders, db, forReading);
    }
    
    
    @Override
    protected GeoJsonBindings getJsonBindings()
    {
        return new GeoJsonBindings(true) {
            @Override
            protected GenericFeature createFeatureObject(String type)
            {
                if (!"FeatureCollection".equals(type))
                    throw new JsonParseException("The type of a GeoJSON feature must be 'FeatureCollection'");
                
                return new GenericFeatureImpl(new QName(type));
            }

            @Override
            protected void writeStandardGeoJsonProperties(JsonWriter writer, IFeature bean) throws IOException
            {
                super.writeStandardGeoJsonProperties(writer, bean);
                
                if (bean instanceof IFeatureCollection)
                {
                    IFeatureCollection fcol = (IFeatureCollection)bean;
                    writer.name("numElements").value(fcol.getMembers().size());
                    /*if (fcol.getSchemaUrl() != null)
                        writer.name("schemaUrl").value(fcol.getSchemaUrl());
                    if (fcol.getCrsUri() != null)
                        writer.name("crsUri").value(fcol..getCrsUri());*/
                }
            }            
            
            /*@Override
            protected boolean readObjectProperty(JsonReader reader, AbstractFeature f, String name) throws IOException
            {
                if ("crsUri".equals(name))
                    featureBuilder.withCrsUri(reader.nextString());
                else if ("schemaUrl".equals(name))
                    featureBuilder.withSchemaUrl(reader.nextString());
                else
                    return super.readObjectProperty(reader, f, name);
                
                return true;
            }*/
            
            
            @Override
            public GenericFeature readFeature(JsonReader reader) throws IOException
            {
                /*featureBuilder = new FeatureCollection.Builder();
                GenericFeature f = super.readFeature(reader);
                return featureBuilder.withExistingFeature(f).build();*/
                return null;
            }
        };
    }
    
    
    @Override
    protected IFeatureCollection getFeatureWithId(FeatureKey key, IFeature f)
    {
        Asserts.checkNotNull(key, FeatureKey.class);
        Asserts.checkNotNull(f, IFeatureCollection.class);
        
        return new IFeatureCollection() {
            
            @Override
            public String getId()
            {
                return idEncoders.getFeatureIdEncoder().encodeID(key.getInternalID());
            }

            @Override
            public String getUniqueIdentifier()
            {
                return f.getUniqueIdentifier();
            }

            @Override
            public String getName()
            {
                return f.getName();
            }

            @Override
            public String getDescription()
            {
                return f.getDescription();
            }

            @Override
            public AbstractGeometry getGeometry()
            {
                return f.getGeometry();
            }

            @Override
            public Collection<IFeature> getMembers()
            {
                return Collections.emptyList();
            }
        };
    }
}
