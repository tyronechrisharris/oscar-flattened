/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore.deployment;

import java.util.Collection;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.EmptyFilterIntersection;
import org.sensorhub.api.datastore.feature.FeatureFilterBase;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.api.resource.ResourceFilter;
import org.sensorhub.api.system.IDeploymentWithDesc;


/**
 * <p>
 * Immutable filter object for deployment resources<br/>
 * There is an implicit AND between all filter parameters
 * </p>
 *
 * @author Alex Robin
 * @since April 4, 2023
 */
public class DeploymentFilter extends FeatureFilterBase<IDeploymentWithDesc>
{
    protected DeploymentFilter parentFilter;
    protected SystemFilter systemFilter;

    
    /*
     * this class can only be instantiated using builder
     */
    protected DeploymentFilter() {}
    
    
    public DeploymentFilter getParentFilter()
    {
        return parentFilter;
    }
    
    
    public SystemFilter getSystemFilter()
    {
        return systemFilter;
    }
    
    
    /**
     * Computes the intersection (logical AND) between this filter and another filter of the same kind
     * @param filter The other filter to AND with
     * @return The new composite filter
     * @throws EmptyFilterIntersection if the intersection doesn't exist
     */
    @Override
    public DeploymentFilter intersect(ResourceFilter<IDeploymentWithDesc> filter) throws EmptyFilterIntersection
    {
        if (filter == null)
            return this;
        
        return intersect((DeploymentFilter)filter, new Builder()).build();
    }
    
    
    protected <B extends DeploymentFilterBuilder<B, DeploymentFilter>> B intersect(DeploymentFilter otherFilter, B builder) throws EmptyFilterIntersection
    {
        super.intersect(otherFilter, builder);
        
        var parentFilter = this.parentFilter != null ? this.parentFilter.intersect(otherFilter.parentFilter) : otherFilter.parentFilter;
        if (parentFilter != null)
            builder.withParents(parentFilter);
        
        var systemFilter = this.systemFilter != null ? this.systemFilter.intersect(otherFilter.systemFilter) : otherFilter.systemFilter;
        if (systemFilter != null)
            builder.withSystems(systemFilter);
        
        return builder;
    }
    
    
    /**
     * Deep clone this filter
     */
    public DeploymentFilter clone()
    {
        return Builder.from(this).build();
    }
    
    
    /*
     * Builder
     */
    public static class Builder extends DeploymentFilterBuilder<Builder, DeploymentFilter>
    {
        public Builder()
        {
            super(new DeploymentFilter());
        }
        
        /**
         * Builds a new filter using the provided filter as a base
         * @param base Filter used as base
         * @return The new builder
         */
        public static Builder from(DeploymentFilter base)
        {
            return new Builder().copyFrom(base);
        }
    }
    
    
    /*
     * Nested builder for use within another builder
     */
    public abstract static class NestedBuilder<B> extends DeploymentFilterBuilder<NestedBuilder<B>, DeploymentFilter>
    {
        B parent;
        
        protected NestedBuilder(B parent)
        {
            super(new DeploymentFilter());
            this.parent = parent;
        }
        
        public abstract B done();
    }
    
    
    @SuppressWarnings("unchecked")
    public abstract static class DeploymentFilterBuilder<
            B extends DeploymentFilterBuilder<B, F>,
            F extends DeploymentFilter>
        extends FeatureFilterBaseBuilder<B, IDeploymentWithDesc, F>
    {
        
        protected DeploymentFilterBuilder(F instance)
        {
            super(instance);
        }
        
        
        @Override
        public B copyFrom(F base)
        {
            super.copyFrom(base);
            instance.systemFilter = base.systemFilter;
            return (B)this;
        }
        
        
        /**
         * Select only subdeployments that are part of the matching parent deployments
         * @param filter Parent deployment filter
         * @return This builder for chaining
         */
        public B withParents(DeploymentFilter filter)
        {
            instance.parentFilter = filter;
            return (B)this;
        }

        
        /**
         * Keep only subdeployments that are part of the parent deployments.<br/>
         * Call done() on the nested builder to go back to main builder.
         * @return The {@link DeploymentFilter} builder for chaining
         */
        public DeploymentFilter.NestedBuilder<B> withParents()
        {
            return new DeploymentFilter.NestedBuilder<B>((B)this) {
                @Override
                public B done()
                {
                    DeploymentFilterBuilder.this.withParents(build());
                    return (B)DeploymentFilterBuilder.this;
                }
            };
        }
        
        
        /**
         * Select only subdeployments that are part of the parent deployments
         * with specific internal IDs
         * @param ids List of IDs of parent deployments
         * @return This builder for chaining
         */
        public B withParents(BigId... ids)
        {
            return withParents()
                .withInternalIDs(ids)
                .done();
        }
        
        
        /**
         * Select only subdeployments that are part of the parent deployments
         * with specific internal IDs
         * @param ids Collection of IDs of parent deployments
         * @return This builder for chaining
         */
        public B withParents(Collection<BigId> ids)
        {
            return withParents()
                .withInternalIDs(ids)
                .done();
        }
        
        
        /**
         * Select only deployments that have no parent
         * @return This builder for chaining
         */
        public B withNoParent()
        {
            return withParents()
                .withInternalIDs(BigId.NONE)
                .done();
        }
        
        
        /**
         * Keep only deployments that involve systems matching the filter.
         * @param filter System filter
         * @return This builder for chaining
         */
        public B withSystems(SystemFilter filter)
        {
            instance.systemFilter = filter;
            return (B)this;
        }

        
        /**
         * Keep only deployments that involve systems matching the filter.<br/>
         * Call done() on the nested builder to go back to main builder.
         * @return The {@link DeploymentFilter} builder for chaining
         */
        public SystemFilter.NestedBuilder<B> withSystems()
        {
            return new SystemFilter.NestedBuilder<B>((B)this) {
                @Override
                public B done()
                {
                    DeploymentFilterBuilder.this.withSystems(build());
                    return (B)DeploymentFilterBuilder.this;
                }
            };
        }
        
        
        /**
         * Keep only deployments that involve systems with the specified internal IDs
         * @param ids List of system IDs
         * @return This builder for chaining
         */
        public B withSystems(BigId... ids)
        {
            return withSystems()
                .withInternalIDs(ids)
                .done();
        }
        
        
        /**
         * Keep only deployments that involve systems with the specified internal IDs
         * @param ids Collection of of system IDs
         * @return This builder for chaining
         */
        public B withSystems(Collection<BigId> ids)
        {
            return withSystems()
                .withInternalIDs(ids)
                .done();
        }
    }
}
