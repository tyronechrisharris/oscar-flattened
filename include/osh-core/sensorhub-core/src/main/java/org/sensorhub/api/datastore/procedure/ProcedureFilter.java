/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore.procedure;

import java.util.Collection;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.EmptyFilterIntersection;
import org.sensorhub.api.datastore.feature.FeatureFilterBase;
import org.sensorhub.api.procedure.IProcedureWithDesc;
import org.sensorhub.api.resource.ResourceFilter;


/**
 * <p>
 * Immutable filter object for procedure resources<br/>
 * There is an implicit AND between all filter parameters
 * </p>
 *
 * @author Alex Robin
 * @date Oct 4, 2021
 */
public class ProcedureFilter extends FeatureFilterBase<IProcedureWithDesc>
{
    protected ProcedureFilter parentFilter;

    
    /*
     * this class can only be instantiated using builder
     */
    protected ProcedureFilter() {}
    
    
    public ProcedureFilter getParentFilter()
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
    public ProcedureFilter intersect(ResourceFilter<IProcedureWithDesc> filter) throws EmptyFilterIntersection
    {
        if (filter == null)
            return this;
        
        return intersect((ProcedureFilter)filter, new Builder()).build();
    }
    
    
    protected <B extends ProcedureFilterBuilder<B, ProcedureFilter>> B intersect(ProcedureFilter otherFilter, B builder) throws EmptyFilterIntersection
    {
        super.intersect(otherFilter, builder);
        
        var parentFilter = this.parentFilter != null ? this.parentFilter.intersect(otherFilter.parentFilter) : otherFilter.parentFilter;
        if (parentFilter != null)
            builder.withParents(parentFilter);
        
        return builder;
    }
    
    
    /**
     * Deep clone this filter
     */
    public ProcedureFilter clone()
    {
        return Builder.from(this).build();
    }
    
    
    /*
     * Builder
     */
    public static class Builder extends ProcedureFilterBuilder<Builder, ProcedureFilter>
    {
        public Builder()
        {
            super(new ProcedureFilter());
        }
        
        /**
         * Builds a new filter using the provided filter as a base
         * @param base Filter used as base
         * @return The new builder
         */
        public static Builder from(ProcedureFilter base)
        {
            return new Builder().copyFrom(base);
        }
    }
    
    
    /*
     * Nested builder for use within another builder
     */
    public abstract static class NestedBuilder<B> extends ProcedureFilterBuilder<NestedBuilder<B>, ProcedureFilter>
    {
        B parent;
        
        protected NestedBuilder(B parent)
        {
            super(new ProcedureFilter());
            this.parent = parent;
        }
        
        public abstract B done();
    }
    
    
    @SuppressWarnings("unchecked")
    public abstract static class ProcedureFilterBuilder<
            B extends ProcedureFilterBuilder<B, F>,
            F extends ProcedureFilter>
        extends FeatureFilterBaseBuilder<B, IProcedureWithDesc, F>
    {
        
        protected ProcedureFilterBuilder(F instance)
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
         * Keep only procedures that are sub-types of the the procedures matching
         * the filter.
         * @param filter Parent procedure filter
         * @return This builder for chaining
         */
        public B withParents(ProcedureFilter filter)
        {
            instance.parentFilter = filter;
            return (B)this;
        }

        
        /**
         * Keep only procedures that are sub-types of the procedures matching
         * the filter.<br/>
         * Call done() on the nested builder to go back to main builder.
         * @return The {@link ProcedureFilter} builder for chaining
         */
        public ProcedureFilter.NestedBuilder<B> withParents()
        {
            return new ProcedureFilter.NestedBuilder<B>((B)this) {
                @Override
                public B done()
                {
                    ProcedureFilterBuilder.this.withParents(build());
                    return (B)ProcedureFilterBuilder.this;
                }
            };
        }
        
        
        /**
         * Keep only procedures that are sub-types of the procedures with the
         * specified internal IDs
         * @param ids List of IDs of parent procedures
         * @return This builder for chaining
         */
        public B withParents(BigId... ids)
        {
            return withParents()
                .withInternalIDs(ids)
                .done();
        }
        
        
        /**
         * Keep only procedures that are sub-types of the procedures with the
         * specified internal IDs
         * @param ids Collection of IDs of parent procedures
         * @return This builder for chaining
         */
        public B withParents(Collection<BigId> ids)
        {
            return withParents()
                .withInternalIDs(ids)
                .done();
        }
        
        
        /**
         * Keep only procedures that have no parent
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
