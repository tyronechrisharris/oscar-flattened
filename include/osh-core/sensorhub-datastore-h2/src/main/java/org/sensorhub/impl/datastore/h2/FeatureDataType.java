/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.h2;

import javax.xml.namespace.QName;
import org.h2.mvstore.MVMap;
import org.locationtech.jts.geom.PrecisionModel;
import org.sensorhub.impl.datastore.h2.kryo.FeatureClassResolver;
import org.sensorhub.impl.datastore.h2.kryo.KryoDataType;
import org.sensorhub.impl.serialization.kryo.QNameSerializer;
import org.sensorhub.impl.serialization.kryo.VersionedSerializer;
import org.sensorhub.impl.serialization.kryo.compat.v1.FeatureSerializerV1;
import org.sensorhub.impl.serialization.kryo.compat.v1.PrecisionModelSerializerV1;
import org.vast.ogc.gml.FeatureRef;
import org.vast.ogc.gml.IFeature;
import org.vast.ogc.om.ProcedureRef;
import org.vast.ogc.om.SamplingFeature;


/**
 * <p>
 * H2 DataType implementation for feature objects
 * </p>
 *
 * @author Alex Robin
 * @date Apr 7, 2018
 */
class FeatureDataType extends KryoDataType
{
    
    FeatureDataType(MVMap<String, Integer> kryoClassMap)
    {
        this.classResolver = () -> new FeatureClassResolver(kryoClassMap);
        this.configurator = kryo -> {
            kryo.addDefaultSerializer(QName.class, QNameSerializer.class);
            
            // register custom serializer w/ backward compatibility
            kryo.addDefaultSerializer(IFeature.class,
                VersionedSerializer.<IFeature>factory(H2Utils.CURRENT_VERSION)
                    .put(H2Utils.CURRENT_VERSION, new FeatureSerializerV1())
                    .build());
            
            // but use default serializer for the following well-known feature types
            kryo.addDefaultSerializer(FeatureRef.class, defaultObjectSerializer);
            kryo.addDefaultSerializer(ProcedureRef.class, defaultObjectSerializer);
            kryo.addDefaultSerializer(SamplingFeature.class, defaultObjectSerializer);
            
            // register backward compatible serializer for JTS < 1.19 classes
            kryo.addDefaultSerializer(PrecisionModel.class,
                VersionedSerializer.<PrecisionModel>factory(H2Utils.CURRENT_VERSION)
                    .put(1, new PrecisionModelSerializerV1(kryo))
                    .build());
        };
    }
}