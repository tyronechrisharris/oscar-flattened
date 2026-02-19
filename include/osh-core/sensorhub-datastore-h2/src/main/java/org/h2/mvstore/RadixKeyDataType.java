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

import org.h2.mvstore.MVRadixTreeMap.SearchContext;
import org.h2.mvstore.type.DataType;


/**
 * <p>
 * Base interface for Radix-tree Key DataTypes
 * </p>
 *
 * @author Alex Robin
 * @date Nov 2, 2018
 */
public interface RadixKeyDataType extends DataType
{
    
    /**
     * Compare the part of the key starting at offset with a tree node prefix 
     * @param fullKey
     * @param prefix
     * @param context search context provides the offset and receives comparison
     * results (compare flag and match length)
     */
    void comparePrefix(Object fullKey, Object prefix, SearchContext context);
    
    
    /**
     * @param prefix Prefix stored in tree node
     * @return Length of key prefix object
     */
    int getPrefixLength(Object prefix);
    
    
    /**
     * Split prefix at given index
     * @param prefix
     * @param splitIndex
     * @return array of length 2 containing begin and end of key/prefix
     */
    Object[] splitKey(Object prefix, int splitIndex);
    
    
    /**
     * @param fullKey
     * @param offset
     * @return return end of key starting at offset
     */
    Object tailKey(Object fullKey, int offset);


    /**
     * Generates the complete key from individual prefixes obtained from the
     * search context (matching node stack)
     * @param context
     * @return The complete key
     */
    Object buildFullKey(SearchContext context);
    
    
    /**
     * @param value value object being inserted or removed from the map
     * @return the key to use to store this value in B-Tree page
     */
    Object getValueKey(Object value);
    
}
