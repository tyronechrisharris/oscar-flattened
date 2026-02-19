/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.h2.mvstore;

import java.nio.ByteBuffer;
import org.h2.mvstore.type.DataType;


/**
 * <p>
 * H2 DataType implementation for keys and values of primitive type long
 * </p>
 *
 * @author Alex Robin
 * @date Oct 26, 2018
 */
public class MVVarLongDataType implements DataType
{
   
            
    @Override
    public int compare(Object objA, Object objB)
    {
        return ((Long)objA).compareTo((Long)objB);
    }
    

    @Override
    public int getMemory(Object obj)
    {
        return 8;
    }
    

    @Override
    public final void write(WriteBuffer wbuf, Object obj)
    {
        wbuf.putVarLong((long)obj);
    }
    

    @Override
    public void write(WriteBuffer wbuf, Object[] obj, int len, boolean key)
    {
        for (Object o: obj)
            write(wbuf, o);
    }
    

    @Override
    public final Object read(ByteBuffer buf)
    {
        return DataUtils.readVarLong(buf);
    }
    

    @Override
    public void read(ByteBuffer buf, Object[] obj, int len, boolean key)
    {
        for (int i=0; i<len; i++)
            obj[i] = read(buf);
    }
}
