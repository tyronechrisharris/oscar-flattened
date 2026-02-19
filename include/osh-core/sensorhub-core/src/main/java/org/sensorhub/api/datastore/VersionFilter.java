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

import org.vast.util.Asserts;
import com.google.common.collect.Range;


/**
 * <p>
 * Special case of range filter for version numbers.<br/>
 * Filtering is possible by version range, specific version number and
 * special cases for 'current version' and 'all versions'.
 * </p>
 *
 * @author Alex Robin
 * @date Oct 8, 2020
 */
public class VersionFilter extends RangeFilter<Integer>
{
    protected boolean currentVersion;
    
    
    public boolean isCurrentVersion()
    {
        return currentVersion;
    }
    
    
    public boolean isAllVersions()
    {
        return getMin() == 0 && getMax() == Integer.MAX_VALUE;
    }
    
    
    @Override
    public Range<Integer> getRange()
    {
        return range;
    }
    
    
    @Override
    public Integer getMin()
    {
        return getRange().lowerEndpoint();
    }
    
    
    @Override
    public Integer getMax()
    {
        return getRange().upperEndpoint();
    }
    
    
    @Override
    public boolean test(Integer val)
    {
        // we always return true if set to latest time since we cannot maintain
        // state of other possibly selected records here. It means that the caller
        // must enforce the latestTime filter contract via other means
        return currentVersion || getRange().contains(val);
    }
    
    
    /**
     * Computes the intersection (logical AND) between this filter and another filter of the same kind
     * @param filter The other filter to AND with
     * @return The new composite filter
     * @throws EmptyFilterIntersection if the intersection doesn't exist
     */
    public VersionFilter intersect(VersionFilter filter) throws EmptyFilterIntersection
    {
        if (filter == null)
            return this;
        return intersect(filter, new Builder()).build();
    }
    
    
    protected <B extends VersionFilterBuilder<B, VersionFilter>> B intersect(VersionFilter otherFilter, B builder) throws EmptyFilterIntersection
    {
        // handle latest time special case
        if ((otherFilter.isCurrentVersion() && isCurrentVersion()) ||
            (otherFilter.isCurrentVersion() && isAllVersions()) ||
            (otherFilter.isAllVersions() && isCurrentVersion()))
            return builder.withCurrentVersion();
        
        // need to call getRange() to compute current time dynamically in case
        // we need to AND it with a fixed time period
        var range = getRange();
        var otherRange = otherFilter.getRange();
        if (!range.isConnected(otherRange))
            throw new EmptyFilterIntersection();
        return builder.withRange(range.intersection(otherRange));
    }
    
    
    @Override
    public String toString()
    {
        if (isCurrentVersion())
            return "current";
        else
            return super.toString();
    }
    
    
    /*
     * Builder
     */
    public static class Builder extends VersionFilterBuilder<Builder, VersionFilter>
    {
        public Builder()
        {
            super(new VersionFilter());
        }
        
        /**
         * Builds a new filter using the provided filter as a base
         * @param base Filter used as base
         * @return The new builder
         */
        public static Builder from(VersionFilter base)
        {
            return new Builder().copyFrom(base);
        }
    }
    
    
    /*
     * Nested builder for use within another builder
     */
    public static abstract class NestedBuilder<B> extends VersionFilterBuilder<NestedBuilder<B>, VersionFilter>
    {
        B parent;
        
        public NestedBuilder(B parent)
        {
            super(new VersionFilter());
            this.parent = parent;            
        }
                
        public abstract B done();
    }
    
    
    @SuppressWarnings("unchecked")
    public static abstract class VersionFilterBuilder<
            B extends VersionFilterBuilder<B, F>,
            F extends VersionFilter>
        extends RangeFilterBuilder<B, F, Integer>
    {
        
        protected VersionFilterBuilder(F instance)
        {
            super(instance);
        }
        
        
        protected B copyFrom(F base)
        {
            Asserts.checkNotNull(base, VersionFilter.class);
            super.copyFrom(base);
            instance.currentVersion = base.currentVersion;
            return (B)this;
        }
        
        
        /**
         * Match only the latest version
         * @return This builder for chaining
         */
        public B withCurrentVersion()
        {
            instance.currentVersion = true;
            instance.range = Range.singleton(0);
            return (B)this;
        }
        
        
        /**
         * Match any timestamp
         * @return This builder for chaining
         */
        public B withAllVersions()
        {
            withRange(0, Integer.MAX_VALUE);
            return (B)this;
        }
    }
}
