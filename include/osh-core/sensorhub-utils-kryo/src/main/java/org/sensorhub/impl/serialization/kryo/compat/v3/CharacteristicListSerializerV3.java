/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.serialization.kryo.compat.v3;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import net.opengis.sensorml.v20.impl.CharacteristicListImpl;


/**
 * <p>
 * Custom serializer for older version of CharacteristicListImpl class.
 * </p>
 *
 * @author Alex Robin
 * @since Jul 28, 2023
 */
public class CharacteristicListSerializerV3 extends FieldSerializer<CharacteristicListImpl>
{
    
    public CharacteristicListSerializerV3(Kryo kryo)
    {
        super(kryo, CharacteristicListImpl.class);
    }
    
    
    @Override
    protected void initializeCachedFields()
    {
        // remove class attributes missing from older version of the class
        this.removeField("conditionList");
    }

}
