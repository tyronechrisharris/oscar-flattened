/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore.feature;

import java.util.Collection;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.EmptyFilterIntersection;
import org.sensorhub.api.resource.ResourceFilter;
import org.vast.ogc.gml.IFeature;


/**
 * <p>
 * Immutable filter object for generic features.<br/>
 * There is an implicit AND between all filter parameters.
 * </p>
 *
 * @author Alex Robin
 * @date Apr 3, 2018
 */
public class FeatureFilter extends FeatureFilterBase<IFeature>
{        
    protected FeatureFilter parentFilter;
    
    
    /*
     * this class can only be instantiated using builder
     */
    protected FeatureFilter()
    {
    }
    
    
    public FeatureFilter getParentFilter()
    {
        return parentFilter;
    }


    /**
     * Computes the intersection (logical AND) between this filter and another filter of the same kind
     * @param filter The other filter to AND with
     * @return The new composite filter
     * @throws EmptyFilterIntersection if the intersection doesn't exist
     */
    @Override
    public FeatureFilter intersect(ResourceFilter<IFeature> filter) throws EmptyFilterIntersection
    {
        if (filter == null)
            return this;
        
        return intersect((FeatureFilter)filter, new Builder()).build();
    }
    
    
    protected <B extends Builder> B intersect(FeatureFilter otherFilter, B builder) throws EmptyFilterIntersection
    {
        super.intersect(otherFilter, builder);
        
        var parentFilter = this.parentFilter != null ? this.parentFilter.intersect(otherFilter.parentFilter) : otherFilter.parentFilter;
        if (parentFilter != null)
            builder.withParents(parentFilter);
        
        return builder;
    }
    
    
    /*
     * Builder
     */
    public static class Builder extends FeatureFilterBuilder<Builder, FeatureFilter>
    {
        public Builder()
        {
            super(new FeatureFilter());
        }
        
        public static Builder from(FeatureFilter base)
        {
            return new Builder().copyFrom(base);
        }
    }
    
    
    /*
     * Nested builder for use within another builder
     */
    public static abstract class NestedBuilder<B> extends FeatureFilterBaseBuilder<NestedBuilder<B>, IFeature, FeatureFilter>
    {
        B parent;
        
        public NestedBuilder(B parent)
        {
            super(new FeatureFilter());
            this.parent = parent;
        }
                
        public abstract B done();
    }
    
    
    @SuppressWarnings("unchecked")
    public static abstract class FeatureFilterBuilder<
            B extends FeatureFilterBuilder<B, F>,
            F extends FeatureFilter>
        extends FeatureFilterBaseBuilder<B, IFeature, F>
    {        
        
        protected FeatureFilterBuilder(F instance)
        {
            super(instance);
        }
                
        
        @Override
        public B copyFrom(F base)
        {
            super.copyFrom(base);
            instance.parentFilter = base.parentFilter;
            return (B)this;
        }
        
        
        /**
         * Select only features belonging to the matching groups
         * @param filter Parent feature filter
         * @return This builder for chaining
         */
        public B withParents(FeatureFilter filter)
        {
            instance.parentFilter = filter;
            return (B)this;
        }

        
        /**
         * Keep only features belonging to the matching collections.<br/>
         * Call done() on the nested builder to go back to main builder.
         * @return The {@link FeatureFilter} builder for chaining
         */
        public FeatureFilter.NestedBuilder<B> withParents()
        {
            return new FeatureFilter.NestedBuilder<B>((B)this) {
                @Override
                public B done()
                {
                    FeatureFilterBuilder.this.withParents(build());
                    return (B)FeatureFilterBuilder.this;
                }
            };
        }
        
        
        /**
         * Select only features belonging to the collections with specific internal IDs
         * @param ids List of IDs of parent feature collections
         * @return This builder for chaining
         */
        public B withParents(BigId... ids)
        {
            return withParents()
                .withInternalIDs(ids)
                .done();
        }
        
        
        /**
         * Select only features belonging to the collections with specific internal IDs
         * @param ids Collection of IDs of parent feature collections
         * @return This builder for chaining
         */
        public B withParents(Collection<BigId> ids)
        {
            return withParents()
                .withInternalIDs(ids)
                .done();
        }
        
        
        /**
         * Select only features that have no parent
         * @return This builder for chaining
         */
        public B withNoParent()
        {
            return withParents()
                .withInternalIDs(BigId.NONE)
                .done();
        }
    }
}
