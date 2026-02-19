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
import java.util.Iterator;
import org.h2.mvstore.MVRadixTreeMap.SearchContext;
import org.h2.mvstore.MVRadixTreeMap.SearchNode;
import com.google.common.primitives.UnsignedBytes;


/**
 * <p>
 * Base implementation for byte array prefix keys.
 * </p>
 *
 * @author Alex Robin
 * @date Oct 8, 2018
 */
public abstract class RadixKeyBytesDataType implements RadixKeyDataType
{
    //Comparator<byte[]> comparator = UnsignedBytes.lexicographicalComparator();
    
    
    @Override
    public int compare(Object key1, Object key2)
    {
        byte[] k1 = (byte[])key1;
        byte[] k2 = (byte[])key2;
        
        int minLength = Math.min(k1.length, k2.length);
        for (int i = 0; i < minLength; i++)
        {
            int result = UnsignedBytes.compare(k1[i], k2[i]);
            if (result != 0)
                return result;
        }
        
        return k1.length - k2.length;
    }
    
    
    @Override
    public void comparePrefix(Object fullKey, Object nodeKey, SearchContext context)
    {
        byte[] k = (byte[])fullKey; // full key to test
        byte[] kp = (byte[])nodeKey; // key part contained in tree node
        
        // compare key with prefix lexicographically starting at offset
        int i = context.offset, j = 0;
        for (; j < kp.length && i < k.length && k[i] == kp[j]; i++, j++);
        
        if (k.length == 0)
            context.compare = -1;
        else if (kp.length == 0)
            context.compare = 1;
        else if (j == 0)
            context.compare = k[i] < kp[0] ? -1 : 1;
        else
            context.compare = 0;
        
        context.matchLength = j;
        context.keyRemaining = k.length - i;
        context.fullNodeMatch = (j == kp.length);
    }


    @Override
    public Object[] splitKey(Object nodeKey, int splitIndex)
    {
        byte[] key = (byte[])nodeKey;
        if (splitIndex >= key.length)
            throw new IllegalArgumentException("Split index out of bounds");
        
        byte[] begin = new byte[splitIndex];
        System.arraycopy(key, 0, begin, 0, begin.length);
        
        byte[] end = new byte[key.length - splitIndex];
        System.arraycopy(key, splitIndex, end, 0, end.length);
        
        return new Object[] {begin, end};   
    }
    
    
    @Override
    public Object tailKey(Object fullKey, int offset)
    {
        byte[] key = (byte[])fullKey;
        if (key.length <= offset)
            return null;
        byte[] keyPart = new byte[key.length-offset];
        System.arraycopy(key, offset, keyPart, 0, keyPart.length);
        return keyPart;
    }
    
    
    @Override
    public int getPrefixLength(Object keyPrefix)
    {
        return ((byte[])keyPrefix).length;
    }
    
    
    @Override
    public Object buildFullKey(SearchContext context)
    {
        int keySize = 0;
        for (SearchNode node: context.nodeStack)
        {
            if (node.childIndex >= 0) 
                keySize += ((byte[])node.page.getKey(node.childIndex)).length;
        }
        
        int destPos = 0;
        byte[] fullKey = new byte[keySize];
        Iterator<SearchNode> it = context.nodeStack.descendingIterator();
        while (it.hasNext())
        {
            SearchNode node = it.next();
            if (node.childIndex >= 0)
            {
                byte[] prefixB = (byte[])node.page.getKey(node.childIndex);
                System.arraycopy(prefixB, 0, fullKey, destPos, prefixB.length);
                destPos += prefixB.length;
            }
        }
        
        return fullKey;
    }
    

    @Override
    public int getMemory(Object obj)
    {
        return ((byte[])obj).length + 2;
    }
    

    @Override
    public void write(WriteBuffer wbuf, Object obj)
    {
        byte[] key = (byte[])obj;
        
        // write array length, then byte[]
        wbuf.putVarInt(key.length);
        wbuf.put(key);
    }
    

    @Override
    public void write(WriteBuffer wbuf, Object[] obj, int len, boolean key)
    {
        for (int i=0; i<len; i++)
            write(wbuf, obj[i]);
    }
    

    @Override
    public Object read(ByteBuffer buf)
    {
        // read array length, then byte[]
        int l = DataUtils.readVarInt(buf);
        byte[] key = new byte[l];
        buf.get(key);
        return key;
    }
    

    @Override
    public void read(ByteBuffer buf, Object[] obj, int len, boolean key)
    {
        for (int i=0; i<len; i++)
            obj[i] = read(buf);        
    }

}
