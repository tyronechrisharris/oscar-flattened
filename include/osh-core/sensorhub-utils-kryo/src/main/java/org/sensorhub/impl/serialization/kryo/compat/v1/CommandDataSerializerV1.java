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

import org.sensorhub.api.command.CommandData;
import org.sensorhub.impl.serialization.kryo.compat.BackwardCompatFieldSerializer;
import com.esotericsoftware.kryo.Kryo;


/**
 * <p>
 * Custom serializer for backward compatibility with v1 format where
 * id was serialized as BigInt and commandStreamID/foiID as longs
 * </p>
 *
 * @author Alex Robin
 * @since May 3, 2022
 */
public class CommandDataSerializerV1 extends BackwardCompatFieldSerializer<CommandData>
{
    int idScope;
    
    
    public CommandDataSerializerV1(Kryo kryo, int idScope)
    {
        super(kryo, CommandData.class);
        this.idScope = idScope;
        customizeCacheFields();
    }
    
    
    protected void customizeCacheFields()
    {
        CachedField[] fields = getFields();
        
        // modify fields that have changed since v1
        int i = 0;
        compatFields = new CachedField[fields.length];
        for (var f: fields)
        {
            var name = f.getName();
            CachedField newField = f;
            
            if ("commandStreamID".equals(name))
            {
                // use transforming field to convert between BigId and long
                newField = new BigIdAsLongCachedField(f.getField(), idScope);
            }
            
            else if ("foiID".equals(name))
            {
                // use transforming field to convert between BigId and long
                newField = new BigIdAsLongCachedField(f.getField(), idScope);
            }
            
            else if ("id".equals(name))
            {
                // use transforming field to convert between BigId and BigInteger
                newField = new BigIdAsBigIntCachedField(f.getField(), getKryo(), idScope);
            }
            
            compatFields[i++] = newField;
        }
    }

}
