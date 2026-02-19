/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore.property;

import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.EmptyFilterIntersection;
import org.sensorhub.api.resource.ResourceFilter;
import org.sensorhub.api.semantic.IDerivedProperty;
import org.sensorhub.utils.FilterUtils;
import com.google.common.collect.ImmutableSortedSet;


/**
 * <p>
 * Immutable filter object for property resources<br/>
 * There is an implicit AND between all filter parameters
 * </p>
 *
 * @author Alex Robin
 * @since June 22, 2023
 */
public class PropertyFilter extends ResourceFilter<IDerivedProperty>
{
    protected SortedSet<String> uniqueIDs;
    protected SortedSet<String> objectTypes;
    protected PropertyFilter basePropFilter;
    
    
    /*
     * this class can only be instantiated using builder
     */
    protected PropertyFilter() {}
    
    
    public SortedSet<String> getUniqueIDs()
    {
        return uniqueIDs;
    }
    
    
    public SortedSet<String> getObjectTypes()
    {
        return objectTypes;
    }
    
    
    public PropertyFilter getBasePropertyFilter()
    {
        return basePropFilter;
    }


    @Override
    public boolean test(IDerivedProperty p)
    {
        return (super.test(p) &&
                testUniqueIDs(p) &&
                testObjectTypes(p));
    }
    
    
    public boolean testUniqueIDs(IDerivedProperty p)
    {
        var uid = p.getURI().toString();
        
        if (uniqueIDs == null)
            return true;
        
        return uniqueIDs.contains(uid);
    }
    
    
    public boolean testObjectTypes(IDerivedProperty p)
    {
        var uri = p.getObjectType();
        
        if (objectTypes == null)
            return true;
        
        return objectTypes.contains(uri);
    }
    
    
    /**
     * Computes the intersection (logical AND) between this filter and another filter of the same kind
     * @param filter The other filter to AND with
     * @return The new composite filter
     * @throws EmptyFilterIntersection if the intersection doesn't exist
     */
    @Override
    public PropertyFilter intersect(ResourceFilter<IDerivedProperty> filter) throws EmptyFilterIntersection
    {
        if (filter == null)
            return this;
        
        return intersect((PropertyFilter)filter, new Builder()).build();
    }
    
    
    protected <B extends PropertyFilterBuilder<B, PropertyFilter>> B intersect(PropertyFilter otherFilter, B builder) throws EmptyFilterIntersection
    {
        super.intersect(otherFilter, builder);
        
        var uniqueIDs = FilterUtils.intersectWithWildcards(this.uniqueIDs, otherFilter.uniqueIDs);
        if (uniqueIDs != null)
            builder.withUniqueIDs(uniqueIDs);
        
        var objectTypes = FilterUtils.intersectWithWildcards(this.objectTypes, otherFilter.objectTypes);
        if (objectTypes != null)
            builder.withObjectTypes(objectTypes);
        
        var basePropFilter = this.basePropFilter != null ? this.basePropFilter.intersect(otherFilter.basePropFilter) : otherFilter.basePropFilter;
        if (basePropFilter != null)
            builder.withBaseProperties(basePropFilter);
        
        return builder;
    }
    
    
    /**
     * Deep clone this filter
     */
    public PropertyFilter clone()
    {
        return Builder.from(this).build();
    }
    
    
    /*
     * Builder
     */
    public static class Builder extends PropertyFilterBuilder<Builder, PropertyFilter>
    {
        public Builder()
        {
            super(new PropertyFilter());
        }
        
        /**
         * Builds a new filter using the provided filter as a base
         * @param base Filter used as base
         * @return The new builder
         */
        public static Builder from(PropertyFilter base)
        {
            return new Builder().copyFrom(base);
        }
    }
    
    
    /*
     * Nested builder for use within another builder
     */
    public abstract static class NestedBuilder<B> extends PropertyFilterBuilder<NestedBuilder<B>, PropertyFilter>
    {
        B parent;
        
        protected NestedBuilder(B parent)
        {
            super(new PropertyFilter());
            this.parent = parent;
        }
        
        public abstract B done();
    }
    
    
    @SuppressWarnings("unchecked")
    public abstract static class PropertyFilterBuilder<
            B extends PropertyFilterBuilder<B, F>,
            F extends PropertyFilter>
        extends ResourceFilterBuilder<B, IDerivedProperty, F>
    {
        
        protected PropertyFilterBuilder(F instance)
        {
            super(instance);
        }
        
        
        @Override
        public B copyFrom(F base)
        {
            super.copyFrom(base);
            instance.basePropFilter = base.basePropFilter;
            return (B)this;
        }
        
        
        /**
         * Keep only properties with specific URIs.
         * @param uris One or more URIs of properties to select.
         * @return This builder for chaining
         */
        public B withUniqueIDs(String... uris)
        {
            return withUniqueIDs(Arrays.asList(uris));
        }
        
        
        /**
         * Keep only properties with specific URIs.
         * @param uris Collection of URIs.
         * @return This builder for chaining
         */
        public B withUniqueIDs(Collection<String> uris)
        {
            this.instance.uniqueIDs = ImmutableSortedSet.copyOf(uris);
            return (B)this;
        }
        
        
        /**
         * Keep only properties applicable to specific object types.
         * @param uris One or more URIs of object types to select.
         * @return This builder for chaining
         */
        public B withObjectTypes(String... uris)
        {
            return withUniqueIDs(Arrays.asList(uris));
        }
        
        
        /**
         * Keep only properties applicable to specific  object types.
         * @param uris Collection of URIs.
         * @return This builder for chaining
         */
        public B withObjectTypes(Collection<String> uris)
        {
            this.instance.objectTypes = ImmutableSortedSet.copyOf(uris);
            return (B)this;
        }
        
        
        /**
         * Keep only properties that are sub-types of the the properties matching
         * the filter.
         * @param filter base property filter
         * @return This builder for chaining
         */
        public B withBaseProperties(PropertyFilter filter)
        {
            instance.basePropFilter = filter;
            return (B)this;
        }

        
        /**
         * Keep only properties that are sub-types of the properties matching
         * the filter.<br/>
         * Call done() on the nested builder to go back to main builder.
         * @return The {@link PropertyFilter} builder for chaining
         */
        public PropertyFilter.NestedBuilder<B> withBaseProperties()
        {
            return new PropertyFilter.NestedBuilder<B>((B)this) {
                @Override
                public B done()
                {
                    PropertyFilterBuilder.this.withBaseProperties(build());
                    return (B)PropertyFilterBuilder.this;
                }
            };
        }
        
        
        /**
         * Keep only properties that are sub-types of the properties with the
         * specified internal IDs
         * @param ids List of IDs of base properties
         * @return This builder for chaining
         */
        public B withBaseProperties(BigId... ids)
        {
            return withBaseProperties()
                .withInternalIDs(ids)
                .done();
        }
        
        
        /**
         * Keep only properties that are sub-types of the properties with the
         * specified internal IDs
         * @param ids Collection of IDs of base properties
         * @return This builder for chaining
         */
        public B withBaseProperties(Collection<BigId> ids)
        {
            return withBaseProperties()
                .withInternalIDs(ids)
                .done();
        }
        
        
        /**
         * Keep only properties that have no base properties
         * @return This builder for chaining
         */
        public B withNoBaseProperty()
        {
            return withBaseProperties()
                .withInternalIDs(BigId.NONE)
                .done();
        }
    }
}
