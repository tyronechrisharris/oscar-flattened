/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.serialization.kryo.compat.v2;

import org.sensorhub.api.command.CommandStreamInfo;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.FieldSerializer;


/**
 * <p>
 * Custom serializer for backward compatibility with previous version of
 * CommandStreamInfo class that didn't have 'resultStruct' and 'resultEncoding'
 * fields.
 * </p>
 *
 * @author Alex Robin
 * @since Oct 5, 2022
 */
public class CommandStreamInfoSerializerV2 extends FieldSerializer<CommandStreamInfo>
{
    
    public CommandStreamInfoSerializerV2(Kryo kryo)
    {
        super(kryo, CommandStreamInfo.class);
    }
    
    
    @Override
    protected void initializeCachedFields()
    {
        // skip fields that were added in v3
        this.removeField("resultStruct");
        this.removeField("resultEncoding");
    }

}
