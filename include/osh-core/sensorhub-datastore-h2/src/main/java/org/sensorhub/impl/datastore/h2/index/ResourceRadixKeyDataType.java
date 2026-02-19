/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.h2.index;

import org.h2.mvstore.RadixKeyBytesDataType;
import org.sensorhub.api.resource.ResourceKey;


/**
 * <p>
 * Implementation with prefix key as byte array and value key as long.
 * </p>
 *
 * @author Alex Robin
 * @date Nov 1, 2018
 */
public class ResourceRadixKeyDataType extends RadixKeyBytesDataType
{

    @Override
    public Object getValueKey(Object value)
    {
        // extract long id from value object
        long id;
        if (value instanceof ResourceKey<?>)
            id = ((ResourceKey<?>)value).getInternalID().getIdAsLong();
        else if (value instanceof Long)
            id = (Long)value;
        else
            throw new IllegalArgumentException("Unsupported value type");
        
        // convert to byte[]
        byte[] bytes = new byte[8];
        bytes[0] = (byte) (id >> 56);
        bytes[1] = (byte) (id >> 48);
        bytes[2] = (byte) (id >> 40);
        bytes[3] = (byte) (id >> 32);
        bytes[4] = (byte) (id >> 24);
        bytes[5] = (byte) (id >> 16);
        bytes[6] = (byte) (id >> 8);
        bytes[7] = (byte) (id /*>> 0*/);
        return bytes;
    }
    
    
    /*@Override
    public void write(WriteBuffer wbuf, Object obj)
    {
        if (obj instanceof Long)
        {
            wbuf.put((byte)0);
            wbuf.putVarLong((Long)obj);
        }
        else
            super.write(wbuf, obj);
    }
    
    
    @Override
    public Object read(ByteBuffer buf)
    {
        // read array length, then byte[]
        int l = DataUtils.readVarInt(buf);
        
        // case of value key as long
        if (l == 0)
            return DataUtils.readVarLong(buf);
        
        byte[] key = new byte[l];
        buf.get(key);
        return key;
    }*/

}
