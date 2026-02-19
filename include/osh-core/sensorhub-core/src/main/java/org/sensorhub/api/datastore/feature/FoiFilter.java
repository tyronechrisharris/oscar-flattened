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
import org.sensorhub.api.datastore.obs.ObsFilter;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.api.resource.ResourceFilter;
import org.vast.ogc.gml.IFeature;


/**
 * <p>
 * Immutable filter object for features of interest associated to observations.<br/>
 * There is an implicit AND between all filter parameters
 * </p>
 *
 * @author Alex Robin
 * @date Apr 5, 2018
 */
public class FoiFilter extends FeatureFilterBase<IFeature>
{
    protected SystemFilter parentFilter;
    protected FeatureFilter sampledFeatureFilter;
    protected ObsFilter obsFilter;
    
    
    /*
     * this class can only be instantiated using builder
     */
    protected FoiFilter() {}
    
    
    public SystemFilter getParentFilter()
    {
        return parentFilter;
    }


    public FeatureFilter getSampledFeatureFilter()
    {
        return sampledFeatureFilter;
    }
    
    
    public ObsFilter getObservationFilter()
    {
        return obsFilter;
    }
    
    
    /**
     * Computes the intersection (logical AND) between this filter and another filter of the same kind
     * @param filter The other filter to AND with
     * @return The new composite filter
     * @throws EmptyFilterIntersection if the intersection doesn't exist
     */
    @Override
    public FoiFilter intersect(ResourceFilter<IFeature> filter) throws EmptyFilterIntersection
    {
        if (filter == null)
            return this;
        
        return intersect((FoiFilter)filter, new Builder()).build();
    }
    
    
    protected <B extends FoiFilterBuilder<B, FoiFilter>> B intersect(FoiFilter otherFilter, B builder) throws EmptyFilterIntersection
    {
        super.intersect(otherFilter, builder);
        
        var parentFilter = this.parentFilter != null ? this.parentFilter.intersect(otherFilter.parentFilter) : otherFilter.parentFilter;
        if (parentFilter != null)
            builder.withParents(parentFilter);
        
        var sfFilter = this.sampledFeatureFilter != null ? this.sampledFeatureFilter.intersect(otherFilter.sampledFeatureFilter) : otherFilter.sampledFeatureFilter;
        if (sfFilter != null)
            builder.withSampledFeatures(sfFilter);
        
        var obsFilter = this.obsFilter != null ? this.obsFilter.intersect(otherFilter.obsFilter) : otherFilter.obsFilter;
        if (obsFilter != null)
            builder.withObservations(obsFilter);
        
        return builder;
    }
    
    
    /**
     * Deep clone this filter
     */
    public FoiFilter clone()
    {
        return Builder.from(this).build();
    }
    
    
    /*
     * Builder
     */
    public static class Builder extends FoiFilterBuilder<Builder, FoiFilter>
    {
        public Builder()
        {
            super(new FoiFilter());
        }
        
        
        /**
         * Builds a new filter using the provided filter as a base
         * @param base Filter used as base
         * @return The new builder
         */
        public static Builder from(FoiFilter base)
        {
            return new Builder().copyFrom(base);
        }
    }
    
    
    /*
     * Nested builder for use within another builder
     */
    public static abstract class NestedBuilder<B> extends FoiFilterBuilder<NestedBuilder<B>, FoiFilter>
    {
        B parent;
        
        public NestedBuilder(B parent)
        {
            super(new FoiFilter());
            this.parent = parent;
        }
                
        public abstract B done();
    }
    
    
    @SuppressWarnings("unchecked")
    public static abstract class FoiFilterBuilder<
            B extends FoiFilterBuilder<B, F>,
            F extends FoiFilter>
        extends FeatureFilterBaseBuilder<B, IFeature, FoiFilter>
    {    
        
        protected FoiFilterBuilder(F instance)
        {
            super(instance);
        }
        
        
        @Override
        public B copyFrom(FoiFilter base)
        {
            super.copyFrom(base);
            instance.parentFilter = base.parentFilter;
            instance.sampledFeatureFilter = base.sampledFeatureFilter;
            instance.obsFilter = base.obsFilter;
            return (B)this;
        }
        
        
        /**
         * Select only sampling features attached to the selected systems
         * @param filter Parent system filter
         * @return This builder for chaining
         */
        public B withParents(SystemFilter filter)
        {
            instance.parentFilter = filter;
            return (B)this;
        }

        
        /**
         * Select only sampling features attached to the selected systems.<br/>
         * Call done() on the nested builder to go back to main builder.
         * @return The {@link SystemFilter} builder for chaining
         */
        public SystemFilter.NestedBuilder<B> withParents()
        {
            return new SystemFilter.NestedBuilder<B>((B)this) {
                @Override
                public B done()
                {
                    FoiFilterBuilder.this.withParents(build());
                    return (B)FoiFilterBuilder.this;
                }
            };
        }
        
        
        /**
         * Select only sampling features attached to systems with
         * specific internal IDs
         * @param ids List of IDs of parent systems
         * @return This builder for chaining
         */
        public B withParents(BigId... ids)
        {
            return withParents()
                .withInternalIDs(ids)
                .done();
        }
        
        
        /**
         * Select only sampling features attached to systems with
         * specific internal IDs
         * @param ids Collection of internal IDs
         * @return This builder for chaining
         */
        public B withParents(Collection<BigId> ids)
        {
            return withParents()
                .withInternalIDs(ids)
                .done();
        }
        
        
        /**
         * Select only FOIs that are sampling the features matching the filter
         * @param filter Sampled features filter
         * @return This builder for chaining
         */
        public B withSampledFeatures(FeatureFilter filter)
        {
            instance.sampledFeatureFilter = filter;
            return (B)this;
        }
        
        
        /**
         * Select only FOIs that are sampling the features matching the filter.<br/>
         * Call done() on the nested builder to go back to main builder.
         * @return The {@link FeatureFilter} builder for chaining
         */
        public FeatureFilter.NestedBuilder<B> withSampledFeatures()
        {
            return new FeatureFilter.NestedBuilder<B>((B)this) {
                @Override
                public B done()
                {
                    FoiFilterBuilder.this.withSampledFeatures(build());
                    return (B)FoiFilterBuilder.this;
                }
            };
        }
        
        
        /**
         * Select only FOIs with observations matching the filter
         * @param filter Observation filter
         * @return This builder for chaining
         */
        public B withObservations(ObsFilter filter)
        {
            instance.obsFilter = filter;
            return (B)this;
        }
        
        
        /**
         * Select only FOIs with observations matching the filter.<br/>
         * Call done() on the nested builder to go back to main builder.
         * @return The {@link ObsFilter} builder for chaining
         */
        public ObsFilter.NestedBuilder<B> withObservations()
        {
            return new ObsFilter.NestedBuilder<B>((B)this) {
                @Override
                public B done()
                {
                    FoiFilterBuilder.this.withObservations(build());
                    return (B)FoiFilterBuilder.this;
                }
            };
        }
    }
}
