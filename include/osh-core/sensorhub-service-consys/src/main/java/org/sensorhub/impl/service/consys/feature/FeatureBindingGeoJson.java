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
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.database.IFeatureDatabase;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.api.feature.FeatureWrapper;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.vast.ogc.gml.GeoJsonBindings;
import org.vast.ogc.gml.IFeature;
import org.vast.util.Asserts;
import com.google.gson.stream.JsonWriter;


/**
 * <p>
 * GeoJSON formatter for feature resources
 * </p>
 *
 * @author Alex Robin
 * @since Jan 26, 2021
 */
public class FeatureBindingGeoJson extends AbstractFeatureBindingGeoJson<IFeature, IFeatureDatabase>
{
    
    public FeatureBindingGeoJson(RequestContext ctx, IdEncoders idEncoders, IFeatureDatabase db, boolean forReading) throws IOException
    {
        super(ctx, idEncoders, db, forReading);
    }
    
    
    protected GeoJsonBindings getJsonBindings()
    {
        return new GeoJsonBindings() {
            protected void writeDateTimeValue(JsonWriter writer, Instant dateTime) throws IOException
            {
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
                return idEncoders.getFeatureIdEncoder().encodeID(key.getInternalID());
            }
        };
    }
}
