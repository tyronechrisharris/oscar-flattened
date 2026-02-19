/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.utils;

import java.util.LinkedHashSet;
import java.util.Set;
import org.sensorhub.api.datastore.EmptyFilterIntersection;
import com.google.common.collect.Sets;


public class FilterUtils
{
    public static final String WILDCARD_CHAR = "*";
    
    
    private FilterUtils() {}
    
    
    public static <T> Set<T> intersect(Set<T> set1, Set<T> set2) throws EmptyFilterIntersection
    {
        // handle null cases
        if (set1 == null)
            return set2;        
        if (set2 == null)
            return set1;
        
        Set<T> setView = Sets.intersection(set1, set2);
        if (setView.isEmpty())
            throw new EmptyFilterIntersection();
        return setView;
    }
    
    
    public static Set<String> intersectWithWildcards(Set<String> set1, Set<String> set2) throws EmptyFilterIntersection
    {
        // handle null cases
        if (set1 == null)
            return set2;        
        if (set2 == null)
            return set1;
        
        var intersection = new LinkedHashSet<String>();        
        for (String val1: set1)
        {
            String prefixVal1 = val1.endsWith(WILDCARD_CHAR) ?
                val1.substring(0, val1.length()-1) : null;
                
            for (String val2: set2)
            {
                String prefixVal2 = val2.endsWith(WILDCARD_CHAR) ?
                    val2.substring(0, val2.length()-1) : null;
                
                if (prefixVal1 != null)
                {
                    if (prefixVal2 != null)
                    {
                        if (prefixVal1.startsWith(prefixVal2))
                            intersection.add(val1);
                        else if (prefixVal2.startsWith(prefixVal1))
                            intersection.add(val2);
                    }
                    else if (val2.startsWith(prefixVal1))
                        intersection.add(val2);
                }
                else
                {
                    if (prefixVal2 != null)
                    {
                        if (val1.startsWith(prefixVal2))
                            intersection.add(val1);
                    }
                    else if (val2.equals(val1))
                        intersection.add(val2);
                }
            }             
        }
        
        if (intersection.isEmpty())
            throw new EmptyFilterIntersection();
        return intersection;
    }
    
}
