/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.common;

import java.util.Arrays;
import org.sensorhub.utils.VarInt;
import org.vast.util.Asserts;


/**
 * <p>
 * BigId implementation backed by a single long value
 * </p>
 *
 * @author Alex Robin
 * @since Apr 14, 2022
 */
public class BigIdLong implements BigId
{
    protected int scope;
    protected long id;
    
    
    public BigIdLong(int scope, long id)
    {
        Asserts.checkArgument(id > 0, "id must be > 0");
        this.scope = scope;
        this.id = id;
    }


    @Override
    public int getScope()
    {
        return scope;
    }


    @Override
    public byte[] getIdAsBytes()
    {
        var buf = new byte[VarInt.varLongSize(id)];
        VarInt.putVarLong(id, buf, 0);
        return buf;
    }


    @Override
    public long getIdAsLong()
    {
        return id;
    }


    @Override
    public int size()
    {
        return VarInt.varLongSize(id);
    }


    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof BigId))
            return false;
        
        if (obj instanceof BigIdLong)
        {
            var other = (BigIdLong)obj;
            return other.scope == scope && other.id == id;
        }
        
        return BigId.equals(this, (BigId)obj);
    }


    @Override
    public int compareTo(BigId other)
    {
        if (other instanceof BigIdLong)
            return BigId.compareLongs(this, other);
        
        return BigId.compare(this, other);
    }


    @Override
    public int hashCode()
    {
        return 31*Integer.hashCode(scope) + Arrays.hashCode(getIdAsBytes());
    }


    @Override
    public String toString()
    {
        return String.format("BigId {scope=%d, id='%s'(%dL)}", scope, BASE32_ENCODING.encode(getIdAsBytes()), id);
    }
}