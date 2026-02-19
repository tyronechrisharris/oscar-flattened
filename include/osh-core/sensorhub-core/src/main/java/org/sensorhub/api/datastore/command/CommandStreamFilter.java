/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore.command;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;
import org.sensorhub.api.command.ICommandStreamInfo;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.EmptyFilterIntersection;
import org.sensorhub.api.datastore.TemporalFilter;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.api.resource.ResourceFilter;
import org.sensorhub.utils.FilterUtils;
import com.google.common.collect.ImmutableSortedSet;
import net.opengis.swe.v20.DataComponent;


/**
 * <p>
 * Immutable filter object for system command streams.<br/>
 * There is an implicit AND between all filter parameters.<br/>
 * If internal IDs are used, no other filter options are allowed.
 * </p>
 *
 * @author Alex Robin
 * @date Mar 11, 2021
 */
public class CommandStreamFilter extends ResourceFilter<ICommandStreamInfo>
{
    protected SystemFilter systemFilter;
    protected CommandFilter commandFilter;
    protected SortedSet<String> controlInputNames;
    protected SortedSet<String> taskableProperties;
    protected TemporalFilter validTime;
    
    
    /*
     * this class can only be instantiated using builder
     */
    protected CommandStreamFilter() {}


    public SystemFilter getSystemFilter()
    {
        return systemFilter;
    }


    public CommandFilter getCommandFilter()
    {
        return commandFilter;
    }


    public SortedSet<String> getControlInputNames()
    {
        return controlInputNames;
    }


    public SortedSet<String> getTaskableProperties()
    {
        return taskableProperties;
    }


    public TemporalFilter getValidTimeFilter()
    {
        return validTime;
    }
    
    
    public boolean testCommandName(ICommandStreamInfo cs)
    {
        return (controlInputNames == null ||
            controlInputNames.contains(cs.getControlInputName()));
    }
    
    
    public boolean testValidTime(ICommandStreamInfo cs)
    {
        return (validTime == null ||
            validTime.test(cs.getValidTime()));
    }
    
    
    public boolean testTaskableProperty(ICommandStreamInfo cs)
    {
        return (taskableProperties == null ||
            hasObservable(cs.getRecordStructure()));
    }
    
    
    public boolean hasObservable(DataComponent comp)
    {
        String def = comp.getDefinition();
        if (def != null && taskableProperties.contains(def))
            return true;
        
        for (int i = 0; i < comp.getComponentCount(); i++)
        {
            if (hasObservable(comp.getComponent(i)))
                return true;
        }
        
        return false;
    }


    @Override
    public boolean test(ICommandStreamInfo cs)
    {
        return (super.test(cs) &&
            testCommandName(cs) &&
            testValidTime(cs) &&
            testTaskableProperty(cs));
    }
    
    
    /**
     * Computes the intersection (logical AND) between this filter and another filter of the same kind
     * @param filter The other filter to AND with
     * @return The new composite filter
     * @throws EmptyFilterIntersection if the intersection doesn't exist
     */
    @Override
    public CommandStreamFilter intersect(ResourceFilter<ICommandStreamInfo> filter) throws EmptyFilterIntersection
    {
        if (filter == null)
            return this;
        
        return intersect((CommandStreamFilter)filter, new Builder()).build();
    }
    
    
    protected <B extends CommandStreamFilterBuilder<B, CommandStreamFilter>> B intersect(CommandStreamFilter otherFilter, B builder) throws EmptyFilterIntersection
    {
        super.intersect(otherFilter, builder);
        
        var procFilter = this.systemFilter != null ? this.systemFilter.intersect(otherFilter.systemFilter) : otherFilter.systemFilter;
        if (procFilter != null)
            builder.withSystems(procFilter);
        
        var cmdFilter = this.commandFilter != null ? this.commandFilter.intersect(otherFilter.commandFilter) : otherFilter.commandFilter;
        if (cmdFilter != null)
            builder.withCommands(cmdFilter);
        
        var outputNames = FilterUtils.intersect(this.controlInputNames, otherFilter.controlInputNames);
        if (outputNames != null)
            builder.withControlInputNames(outputNames);
        
        var taskableProperties = FilterUtils.intersect(this.taskableProperties, otherFilter.taskableProperties);
        if (taskableProperties != null)
            builder.withTaskableProperties(taskableProperties);
        
        return builder;
    }
    
    
    /**
     * Deep clone this filter
     */
    public CommandStreamFilter clone()
    {
        return Builder.from(this).build();
    }
    
    
    /*
     * Builder
     */
    public static class Builder extends CommandStreamFilterBuilder<Builder, CommandStreamFilter>
    {
        public Builder()
        {
            super(new CommandStreamFilter());
        }
        
        /**
         * Builds a new filter using the provided filter as a base
         * @param base Filter used as base
         * @return The new builder
         */
        public static Builder from(CommandStreamFilter base)
        {
            return new Builder().copyFrom(base);
        }
    }
    
    
    /*
     * Nested builder for use within another builder
     */
    public static abstract class NestedBuilder<B> extends CommandStreamFilterBuilder<NestedBuilder<B>, CommandStreamFilter>
    {
        B parent;
        
        public NestedBuilder(B parent)
        {
            super(new CommandStreamFilter());
            this.parent = parent;
        }
        
        public abstract B done();
    }


    @SuppressWarnings("unchecked")
    public static abstract class CommandStreamFilterBuilder<
            B extends CommandStreamFilterBuilder<B, F>,
            F extends CommandStreamFilter>
        extends ResourceFilterBuilder<B, ICommandStreamInfo, F>
    {
        
        protected CommandStreamFilterBuilder(F instance)
        {
            super(instance);
        }
        
        
        @Override
        public B copyFrom(F other)
        {
            super.copyFrom(other);
            instance.systemFilter = other.systemFilter;
            instance.commandFilter = other.commandFilter;
            instance.controlInputNames = other.controlInputNames;
            instance.validTime = other.validTime;
            instance.taskableProperties = other.taskableProperties;
            return (B)this;
        }


        /**
         * Keep only command streams exposed by the systems matching the filters.
         * @param filter Filter to select desired systems
         * @return This builder for chaining
         */
        public B withSystems(SystemFilter filter)
        {
            instance.systemFilter = filter;
            return (B)this;
        }

        
        /**
         * Keep only command streams exposed by the systems matching the filters.<br/>
         * Call done() on the nested builder to go back to main builder.
         * @return The {@link SystemFilter} builder for chaining
         */
        public SystemFilter.NestedBuilder<B> withSystems()
        {
            return new SystemFilter.NestedBuilder<B>((B)this) {
                @Override
                public B done()
                {
                    CommandStreamFilterBuilder.this.withSystems(build());
                    return (B)CommandStreamFilterBuilder.this;
                }
            };
        }


        /**
         * Keep only command streams exposed by specific systems (including all outputs).
         * @param ids Internal IDs of one or more systems
         * @return This builder for chaining
         */
        public B withSystems(BigId... ids)
        {
            return withSystems(Arrays.asList(ids));
        }


        /**
         * Keep only command streams exposed by specific systems (including all outputs).
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
         * Keep only command streams that have commands matching the filter
         * @param filter Filter to select desired commands
         * @return This builder for chaining
         */
        public B withCommands(CommandFilter filter)
        {
            instance.commandFilter = filter;
            return (B)this;
        }

        
        /**
         * Keep only command streams that have commands matching the filter.<br/>
         * Call done() on the nested builder to go back to main builder.
         * @return The {@link CommandFilter} builder for chaining
         */
        public CommandFilter.NestedBuilder<B> withCommands()
        {
            return new CommandFilter.NestedBuilder<B>((B)this) {
                @Override
                public B done()
                {
                    CommandStreamFilterBuilder.this.withCommands(build());
                    return (B)CommandStreamFilterBuilder.this;
                }
            };
        }
        
        
        /**
         * Keep only command streams associated to the specified command inputs.
         * @param names One or more system control input names
         * @return This builder for chaining
         */
        public B withControlInputNames(String... names)
        {
            return withControlInputNames(Arrays.asList(names));
        }
        
        
        /**
         * Keep only command streams associated to the specified command inputs.
         * @param names Collections of system control input names
         * @return This builder for chaining
         */
        public B withControlInputNames(Collection<String> names)
        {
            instance.controlInputNames = ImmutableSortedSet.copyOf(names);
            return (B)this;
        }
        
        
        /**
         * Keep only command streams with the specified taskable properties.
         * @param uris One or more taskable property URIs
         * @return This builder for chaining
         */
        public B withTaskableProperties(String... uris)
        {
            return withTaskableProperties(Arrays.asList(uris));
        }
        
        
        /**
         * Keep only command streams with the specified taskable properties.
         * @param uris Collection of taskable property URIs
         * @return This builder for chaining
         */
        public B withTaskableProperties(Collection<String> uris)
        {
            instance.taskableProperties = ImmutableSortedSet.copyOf(uris);
            return (B)this;
        }


        /**
         * Keep only command streams whose temporal validity matches the filter.
         * @param timeFilter Temporal filter (see {@link TemporalFilter})
         * @return This builder for chaining
         */
        public B withValidTime(TemporalFilter timeFilter)
        {
            instance.validTime = timeFilter;
            return (B)this;
        }


        /**
         * Keep only command streams whose temporal validity matches the filter.<br/>
         * Call done() on the nested builder to go back to main builder.
         * @return The {@link TemporalFilter} builder for chaining
         */
        public TemporalFilter.NestedBuilder<B> withValidTime()
        {
            return new TemporalFilter.NestedBuilder<B>((B)this) {
                @Override
                public B done()
                {
                    CommandStreamFilterBuilder.this.withValidTime(build());
                    return (B)CommandStreamFilterBuilder.this;
                }                
            };
        }
        
        
        /**
         * Keep only command streams that are valid at some point during the specified period.
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
         * Keep only command streams that are valid at the specified time.
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
         * Keep only command streams that are valid at the current time.
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
         * Keep all versions of selected command streams.
         * @return This builder for chaining
         */
        public B withAllVersions()
        {
            instance.validTime = new TemporalFilter.Builder()
                .withAllTimes()
                .build();
            return (B)this;
        }
    }

}
