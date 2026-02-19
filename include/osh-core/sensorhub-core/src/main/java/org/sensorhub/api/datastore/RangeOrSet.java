/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.function.Predicate;
import org.sensorhub.utils.FilterUtils;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Range;


/**
 * <p>
 * Immutable union type that can be either a continuous range or discrete
 * sorted set of values.
 * </p>
 *
 * @author Alex Robin
 * @param <T> Type of range or set values 
 * @date Oct 16, 2019
 */
public class RangeOrSet<T extends Comparable<T>> implements Predicate<T>
{
    Range<T> range;
    SortedSet<T> set;
    
    
    public static <T extends Comparable<T>> RangeOrSet<T> from(Range<T> range)
    {
        RangeOrSet<T> union = new RangeOrSet<>();
        union.range = range;
        return union;
    }
    
    
    public static <T extends Comparable<T>> RangeOrSet<T> from(T lower, T upper)
    {
        return RangeOrSet.from(Range.closed(lower, upper));
    }
    
    
    public static <T extends Comparable<T>> RangeOrSet<T> from(Collection<T> col)
    {
        RangeOrSet<T> union = new RangeOrSet<>();
        union.set = ImmutableSortedSet.copyOf(col);
        return union;
    }
    
    
    @SafeVarargs
    public static <T extends Comparable<T>> RangeOrSet<T> from(T... items)
    {
        return RangeOrSet.from(Arrays.asList(items));
    }
    
    
    public boolean isRange()
    {
        return range != null;
    }
    
    
    public Range<T> getRange()
    {
        return range;
    }
    
    
    public boolean isSet()
    {
        return set != null;
    }
    
    
    public SortedSet<T> getSet()
    {
        return Collections.unmodifiableSortedSet(set);
    }


    @Override
    public boolean test(T t)
    {
        if (isRange())
            return range.test(t);
        else if (isSet())
            return set.contains(t);
        return false;
    }
    
    
    public RangeOrSet<T> intersect(RangeOrSet<T> other) throws EmptyFilterIntersection
    {
        if (other == null)
            return this;
        
        if (isSet() && other.isSet())
            return RangeOrSet.from(FilterUtils.intersect(set, other.set));
        
        if (isRange() && other.isRange())
        {
            if (!range.isConnected(other.range))
                throw new EmptyFilterIntersection();
            return RangeOrSet.from(range.intersection(other.range));
        }
        
        if (isSet())
        {
            ArrayList<T> newSet = new ArrayList<>();
            for (T e: set)
            {
                if (other.test(e))
                    newSet.add(e);
            }
            if (newSet.isEmpty())
                throw new EmptyFilterIntersection();
            return RangeOrSet.from(newSet);
        }
        
        if (other.isSet())
        {
            ArrayList<T> newSet = new ArrayList<>();
            for (T e: other.set)
            {
                if (test(e))
                    newSet.add(e);
            }
            if (newSet.isEmpty())
                throw new EmptyFilterIntersection();
            return RangeOrSet.from(newSet);
        }
        
        return null;
    }
    
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append(isSet() ? getSet() : getRange());
        return buf.toString();
    }
    
}
