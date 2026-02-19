/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.serialization.kryo.compat.v1;

import org.locationtech.jts.geom.PrecisionModel;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.FieldSerializer;


/**
 * <p>
 * Custom serializer for backward compatibility with JTS < 1.19
 * PrecisionModel class that didn't have the 'gridSize' field.
 * </p>
 *
 * @author Alex Robin
 * @since Aug 20, 2022
 */
public class PrecisionModelSerializerV1 extends FieldSerializer<PrecisionModel>
{
    
    public PrecisionModelSerializerV1(Kryo kryo)
    {
        super(kryo, PrecisionModel.class);
    }
    
    
    @Override
    protected void initializeCachedFields()
    {
        // skip fields that were added in JTS 1.19
        this.removeField("gridSize");
    }

}
