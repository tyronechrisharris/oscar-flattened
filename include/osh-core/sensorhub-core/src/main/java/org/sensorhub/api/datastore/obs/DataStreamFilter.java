/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore.obs;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.SortedSet;
import java.util.stream.Stream;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.api.datastore.EmptyFilterIntersection;
import org.sensorhub.api.datastore.TemporalFilter;
import org.sensorhub.api.datastore.feature.FoiFilter;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.api.resource.ResourceFilter;
import org.sensorhub.utils.FilterUtils;
import org.vast.data.DataIterator;
import org.vast.swe.SWEConstants;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Streams;
import net.opengis.swe.v20.DataComponent;


/**
 * <p>
 * Immutable filter object for system data streams.<br/>
 * There is an implicit AND between all filter parameters.<br/>
 * If internal IDs are used, no other filter options are allowed.
 * </p>
 *
 * @author Alex Robin
 * @date Sep 17, 2019
 */
public class DataStreamFilter extends ResourceFilter<IDataStreamInfo>
{
    protected SystemFilter systemFilter;
    protected ObsFilter obsFilter;
    protected SortedSet<String> outputNames;
    protected SortedSet<String> observedProperties;
    protected TemporalFilter validTime;
    
    
    /*
     * this class can only be instantiated using builder
     */
    protected DataStreamFilter() {}


    public SystemFilter getSystemFilter()
    {
        return systemFilter;
    }
    
    
    public ObsFilter getObservationFilter()
    {
        return obsFilter;
    }


    public SortedSet<String> getOutputNames()
    {
        return outputNames;
    }


    public SortedSet<String> getObservedProperties()
    {
        return observedProperties;
    }


    public TemporalFilter getValidTimeFilter()
    {
        return validTime;
    }
    
    
    public boolean testOutputName(IDataStreamInfo ds)
    {
        return (outputNames == null ||
            outputNames.contains(ds.getOutputName()));
    }
    
    
    public boolean testValidTime(IDataStreamInfo ds)
    {
        return (validTime == null ||
            validTime.test(ds.getValidTime()));
    }
    
    
    public boolean testObservedProperty(IDataStreamInfo ds)
    {
        if (observedProperties == null)
            return true;
        
        return hasObservable(ds.getRecordStructure());
    }
    
    
    public boolean hasObservable(DataComponent comp)
    {
        String def = comp.getDefinition();
        if (def != null && observedProperties.contains(def))
            return true;
        
        for (int i = 0; i < comp.getComponentCount(); i++)
        {
            if (hasObservable(comp.getComponent(i)))
                return true;
        }
        
        return false;
    }
    
    
    @Override
    public boolean testFullText(IDataStreamInfo res)
    {
        if (super.testFullText(res))
            return true;
        
        return getTextContent(res).anyMatch(fullText::test);
    }


    @Override
    public boolean test(IDataStreamInfo ds)
    {
        return (super.test(ds) &&
            testOutputName(ds) &&
            testValidTime(ds) &&
            testObservedProperty(ds));
    }
    
    
    /**
     * Computes the intersection (logical AND) between this filter and another filter of the same kind
     * @param filter The other filter to AND with
     * @return The new composite filter
     * @throws EmptyFilterIntersection if the intersection doesn't exist
     */
    @Override
    public DataStreamFilter intersect(ResourceFilter<IDataStreamInfo> filter) throws EmptyFilterIntersection
    {
        if (filter == null)
            return this;
        
        return intersect((DataStreamFilter)filter, new Builder()).build();
    }
    
    
    protected <B extends DataStreamFilterBuilder<B, DataStreamFilter>> B intersect(DataStreamFilter otherFilter, B builder) throws EmptyFilterIntersection
    {
        super.intersect(otherFilter, builder);
        
        var procFilter = this.systemFilter != null ? this.systemFilter.intersect(otherFilter.systemFilter) : otherFilter.systemFilter;
        if (procFilter != null)
            builder.withSystems(procFilter);
        
        var obsFilter = this.obsFilter != null ? this.obsFilter.intersect(otherFilter.obsFilter) : otherFilter.obsFilter;
        if (obsFilter != null)
            builder.withObservations(obsFilter);
        
        var outputNames = FilterUtils.intersect(this.outputNames, otherFilter.outputNames);
        if (outputNames != null)
            builder.withOutputNames(outputNames);
        
        var observedProperties = FilterUtils.intersect(this.observedProperties, otherFilter.observedProperties);
        if (observedProperties != null)
            builder.withObservedProperties(observedProperties);
        
        return builder;
    }
    
    
    /**
     * Deep clone this filter
     */
    public DataStreamFilter clone()
    {
        return Builder.from(this).build();
    }
    
    
    /*
     * Builder
     */
    public static class Builder extends DataStreamFilterBuilder<Builder, DataStreamFilter>
    {
        public Builder()
        {
            super(new DataStreamFilter());
        }
        
        /**
         * Builds a new filter using the provided filter as a base
         * @param base Filter used as base
         * @return The new builder
         */
        public static Builder from(DataStreamFilter base)
        {
            return new Builder().copyFrom(base);
        }
    }
    
    
    /*
     * Nested builder for use within another builder
     */
    public static abstract class NestedBuilder<B> extends DataStreamFilterBuilder<NestedBuilder<B>, DataStreamFilter>
    {
        B parent;
        
        public NestedBuilder(B parent)
        {
            super(new DataStreamFilter());
            this.parent = parent;
        }
        
        public abstract B done();
    }


    @SuppressWarnings("unchecked")
    public static abstract class DataStreamFilterBuilder<
            B extends DataStreamFilterBuilder<B, F>,
            F extends DataStreamFilter>
        extends ResourceFilterBuilder<B, IDataStreamInfo, F>
    {
        
        protected DataStreamFilterBuilder(F instance)
        {
            super(instance);
        }
        
        
        @Override
        public B copyFrom(F other)
        {
            super.copyFrom(other);
            instance.systemFilter = other.systemFilter;
            instance.obsFilter = other.obsFilter;
            instance.outputNames = other.outputNames;
            instance.validTime = other.validTime;
            instance.observedProperties = other.observedProperties;
            return (B)this;
        }


        /**
         * Keep only datastreams generated by the systems matching the filters.
         * @param filter Filter to select desired systems
         * @return This builder for chaining
         */
        public B withSystems(SystemFilter filter)
        {
            instance.systemFilter = filter;
            return (B)this;
        }

        
        /**
         * Keep only datastreams generated by the systems matching the filters.<br/>
         * Call done() on the nested builder to go back to main builder.
         * @return The {@link SystemFilter} builder for chaining
         */
        public SystemFilter.NestedBuilder<B> withSystems()
        {
            return new SystemFilter.NestedBuilder<B>((B)this) {
                @Override
                public B done()
                {
                    DataStreamFilterBuilder.this.withSystems(build());
                    return (B)DataStreamFilterBuilder.this;
                }
            };
        }


        /**
         * Keep only datastreams from specific systems (including all outputs).
         * @param ids Internal IDs of one or more systems
         * @return This builder for chaining
         */
        public B withSystems(BigId... ids)
        {
            return withSystems(Arrays.asList(ids));
        }


        /**
         * Keep only datastreams from specific systems (including all outputs).
         * @param ids Internal IDs of one or more systems
         * @return This builder for chaining
         */
        public B withSystems(Collection<BigId> ids)
        {
            withSystems(new SystemFilter.Builder()
                .withInternalIDs(ids)
                .build());
            return (B)this;
        }
        

        /**
         * Keep only datastreams that have observations matching the filter
         * @param filter Filter to select desired observations
         * @return This builder for chaining
         */
        public B withObservations(ObsFilter filter)
        {
            instance.obsFilter = filter;
            return (B)this;
        }

        
        /**
         * Keep only datastreams that have observations matching the filter.<br/>
         * Call done() on the nested builder to go back to main builder.
         * @return The {@link ObsFilter} builder for chaining
         */
        public ObsFilter.NestedBuilder<B> withObservations()
        {
            return new ObsFilter.NestedBuilder<B>((B)this) {
                @Override
                public B done()
                {
                    DataStreamFilterBuilder.this.withObservations(build());
                    return (B)DataStreamFilterBuilder.this;
                }
            };
        }
        
        
        /**
         * Keep only datastreams associated to the specified outputs.
         * @param names One or more system output names
         * @return This builder for chaining
         */
        public B withOutputNames(String... names)
        {
            return withOutputNames(Arrays.asList(names));
        }
        
        
        /**
         * Keep only datastreams associated to the specified outputs.
         * @param names Collections of system output names
         * @return This builder for chaining
         */
        public B withOutputNames(Collection<String> names)
        {
            instance.outputNames = ImmutableSortedSet.copyOf(names);
            return (B)this;
        }
        
        
        /**
         * Keep only datastreams with the specified observed properties.
         * @param uris One or more observable property URIs
         * @return This builder for chaining
         */
        public B withObservedProperties(String... uris)
        {
            return withObservedProperties(Arrays.asList(uris));
        }
        
        
        /**
         * Keep only datastreams with the specified observed properties.
         * @param uris Collection of observable property URIs
         * @return This builder for chaining
         */
        public B withObservedProperties(Collection<String> uris)
        {
            instance.observedProperties = ImmutableSortedSet.copyOf(uris);
            return (B)this;
        }


        /**
         * Keep only datastreams whose temporal validity matches the filter.
         * @param timeFilter Temporal filter (see {@link TemporalFilter})
         * @return This builder for chaining
         */
        public B withValidTime(TemporalFilter timeFilter)
        {
            instance.validTime = timeFilter;
            return (B)this;
        }


        /**
         * Keep only datastreams whose temporal validity matches the filter.<br/>
         * Call done() on the nested builder to go back to main builder.
         * @return The {@link TemporalFilter} builder for chaining
         */
        public TemporalFilter.NestedBuilder<B> withValidTime()
        {
            return new TemporalFilter.NestedBuilder<B>((B)this) {
                @Override
                public B done()
                {
                    DataStreamFilterBuilder.this.withValidTime(build());
                    return (B)DataStreamFilterBuilder.this;
                }
            };
        }
        
        
        /**
         * Keep only datastreams that are valid at some point during the specified period.
         * @param begin Beginning of search period
         * @param end End of search period
         * @return This builder for chaining
         */
        public B withValidTimeDuring(Instant begin, Instant end)
        {
            instance.validTime = new TemporalFilter.Builder()
                    .withRange(begin, end)
                    .build();
            return (B)this;
        }


        /**
         * Keep only datastreams that are valid at the specified time.
         * @param time Time instant of interest (can be set to past or future)
         * @return This builder for chaining
         */
        public B validAtTime(Instant time)
        {
            instance.validTime = new TemporalFilter.Builder()
                    .withSingleValue(time)
                    .build();
            return (B)this;
        }


        /**
         * Keep only datastreams that are valid at the current time.
         * @return This builder for chaining
         */
        public B withCurrentVersion()
        {
            instance.validTime = new TemporalFilter.Builder()
                .withCurrentTime()
                .build();
            return (B)this;
        }
        
        
        /**
         * Keep all versions of selected datastreams.
         * @return This builder for chaining
         */
        public B withAllVersions()
        {
            instance.validTime = new TemporalFilter.Builder()
                .withAllTimes()
                .build();
            return (B)this;
        }
        

        /**
         * Select only datastreams containing observations of features of interest
         * matching the filter
         * @param filter Features of interest filter
         * @return This builder for chaining
         */
        public B withFois(FoiFilter filter)
        {
            return withObservations(new ObsFilter.Builder()
                .withFois(filter)
                .build());
        }

        
        /**
         * Select only datastreams containing observations of features of interest
         * matching the filter.<br/>
         * Call done() on the nested builder to go back to main builder.
         * @return The {@link FoiFilter} builder for chaining
         */
        public FoiFilter.NestedBuilder<B> withFois()
        {
            return new FoiFilter.NestedBuilder<B>((B)this) {
                @Override
                public B done()
                {
                    DataStreamFilterBuilder.this.withFois(build());
                    return (B)DataStreamFilterBuilder.this;
                }
            };
        }
    }
    
    
    public static Stream<String> getTextContent(IDataStreamInfo dsInfo)
    {
        return Streams.stream((Iterator<DataComponent>)new DataIterator(dsInfo.getRecordStructure()))
            .filter(comp -> {
                // skip well known fields
                var def = comp.getDefinition();
                return !(SWEConstants.DEF_SAMPLING_TIME.equals(def) ||
                         SWEConstants.DEF_PHENOMENON_TIME.equals(def) ||
                         SWEConstants.DEF_SYSTEM_ID.equals(def));
            })
            .flatMap(comp -> {
                var label = comp.getLabel();
                if (label == null)
                    label = comp.getName();
                var description = comp.getDescription();
                return Stream.of(label, description);
            })
            .filter(Objects::nonNull);
    }

}
