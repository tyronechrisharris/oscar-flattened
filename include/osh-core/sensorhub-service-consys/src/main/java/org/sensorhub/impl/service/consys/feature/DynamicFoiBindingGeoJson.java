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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.api.datastore.obs.DataStreamKey;
import org.sensorhub.api.datastore.obs.ObsFilter;
import org.sensorhub.api.feature.FeatureWrapper;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.utils.Lambdas;
import org.vast.ogc.gml.GeoJsonBindings;
import org.vast.ogc.gml.IFeature;
import org.vast.util.Asserts;
import com.google.gson.stream.JsonWriter;
import net.opengis.swe.v20.DataBlock;


/**
 * <p>
 * GeoJSON formatter for feature resources
 * </p>
 *
 * @author Alex Robin
 * @since Jan 26, 2021
 */
public class DynamicFoiBindingGeoJson extends AbstractFeatureBindingGeoJson<IFeature, IObsSystemDatabase>
{
    
    public DynamicFoiBindingGeoJson(RequestContext ctx, IdEncoders idEncoders, IObsSystemDatabase db, boolean forReading) throws IOException
    {
        super(ctx, idEncoders, db, forReading);
    }
    
    
    protected GeoJsonBindings getJsonBindings()
    {
        class VariableProps
        {
            DynamicPropsWriter writer;
            DynamicGeomScanner geomScanner;
            DataBlock result;
        }
        
        return new GeoJsonBindings() {
            Map<BigId, VariableProps> variablePropsHelpers = new HashMap<>();
            Collection<VariableProps> variableProps = new ArrayList<>();
            VariableProps geomProps;
            
            protected void writeStandardGeoJsonProperties(JsonWriter writer, IFeature bean) throws IOException
            {
                variableProps.clear();
                geomProps = null;
                
                // try to load associated observations
                var obsStream = db.getObservationStore().select(new ObsFilter.Builder()
                    .withLatestResult()
                    .withFois(idEncoders.getFoiIdEncoder().decodeID(bean.getId()))
                    .build());
                
                obsStream.findFirst().ifPresent(Lambdas.checked(obs -> {
                    var varPropsHelper = variablePropsHelpers.computeIfAbsent(obs.getDataStreamID(), k -> {
                        var dsKey = new DataStreamKey(k);
                        var dsInfo = db.getDataStreamStore().get(dsKey);
                        
                        var propsWrt = new DynamicPropsWriter(writer);
                        propsWrt.setDataComponents(dsInfo.getRecordStructure());
                        propsWrt.initProcessTree();
                        
                        var geomScanner = new DynamicGeomScanner();
                        geomScanner.setDataComponents(dsInfo.getRecordStructure());
                        geomScanner.initProcessTree();
                        
                        var helper = new VariableProps();
                        helper.writer = propsWrt;
                        if (geomScanner.hasGeom())
                            helper.geomScanner = geomScanner;
                        return helper;
                    });
                    
                    // store result and associated writer
                    // so we can write values later in feature properties object
                    varPropsHelper.result = obs.getResult();
                    variableProps.add(varPropsHelper);
                    if (varPropsHelper.geomScanner != null)
                        geomProps = varPropsHelper;
                }));
                
                super.writeStandardGeoJsonProperties(writer, bean);
                
                if (geomProps != null)
                {
                    var geom = geomProps.geomScanner.getGeom(geomProps.result);
                    if (geom != null)
                    {
                        writer.name("geometry");
                        writeGeometry(writer, geom);
                    }
                }
            }
            
            protected void writeCustomFeatureProperties(JsonWriter writer, IFeature bean) throws IOException
            {
                super.writeCustomFeatureProperties(writer, bean);
                for (var props: variableProps)
                    props.writer.write(props.result);
            }
            
            protected void writeDateTimeValue(JsonWriter writer, Instant dateTime) throws IOException
            {
                // truncate timestamps to whole seconds
                super.writeDateTimeValue(writer, dateTime.truncatedTo(ChronoUnit.SECONDS));
            }
        };
    }
    
    
    @Override
    protected IFeature getFeatureWithId(FeatureKey key, IFeature f)
    {
        Asserts.checkNotNull(key, FeatureKey.class);
        Asserts.checkNotNull(f, IFeature.class);
        
        return new FeatureWrapper(f) {
            @Override
            public String getId()
            {
                return idEncoders.getFoiIdEncoder().encodeID(key.getInternalID());
            }
        };
    }
}
