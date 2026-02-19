/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.h2.kryo;

import java.util.Set;
import org.h2.mvstore.MVMap;
import org.vast.ogc.gml.FeatureRef;
import org.vast.ogc.gml.IFeature;
import org.vast.ogc.om.MovingFeature;
import org.vast.ogc.om.ProcedureRef;
import org.vast.ogc.om.SamplingCurve;
import org.vast.ogc.om.SamplingPoint;
import org.vast.ogc.om.SamplingSurface;
import org.vast.sensorML.sampling.SamplingSphere;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Output;


/**
 * <p>
 * Special class resolver to prevent custom feature classes that may not be 
 * available at deserialization time to be serialized directly in storage.
 * </p>
 *
 * @author Alex Robin
 * @since Oct 1, 2021
 */
@SuppressWarnings("rawtypes")
public class FeatureClassResolver extends PersistentClassResolver
{
    // list of feature classes that are allowed for direct serialization
    // any other feature class will be serialized using a generic serializer
    // and a GenericFeature will be created when deserializing
    static Set<Class<?>> allowedFeatureClasses = Set.of(
        SamplingPoint.class,
        SamplingCurve.class,
        SamplingSurface.class,
        SamplingSphere.class,
        MovingFeature.class,
        FeatureRef.class,
        ProcedureRef.class);
    
    
    public FeatureClassResolver(MVMap<String, Integer> classNameToIdMap)
    {
        super(classNameToIdMap);
    }
    
    
    @Override
    public Registration writeClass(Output output, Class type)
    {
        // if class shouldn't be serialized directly, tag with
        // super interface to force use of generic feature serializer
        if (type != null && IFeature.class.isAssignableFrom(type))
        {
            if (!allowedFeatureClasses.contains(type))
                type = IFeature.class;
        }
        
        return super.writeClass(output, type);
    }
}
