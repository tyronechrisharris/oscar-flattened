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

import org.sensorhub.api.common.BigId;
import org.sensorhub.utils.ObjectUtils;
import org.vast.util.Asserts;
import org.vast.util.BaseBuilder;
import org.vast.util.TimeExtent;


/**
 * <p>
 * Immutable object representing statistics for commands of a specific
 * command stream.
 * </p>
 *
 * @author Alex Robin
 * @since Mar 12, 2021
 */
public class CommandStats
{
    protected BigId commandStreamID;
    protected TimeExtent issueTimeRange;
    protected TimeExtent actuationTimeRange;
    protected long totalCommandCount = 0;
    protected int[] commandCountsByTime = null;
    
    
    /*
     * this class can only be instantiated using builder
     */
    CommandStats()
    {
    }


    /**
     * @return The ID of the command stream that these statistics apply to
     */
    public BigId getCommandStreamID()
    {
        return commandStreamID;
    }


    /**
     * @return The time range spanned by issue times of all selected commands
     */
    public TimeExtent getIssueTimeRange()
    {
        return issueTimeRange;
    }


    /**
     * @return The time range spanned by actuation times of all selected commands
     */
    public TimeExtent getActuationTimeRange()
    {
        if (actuationTimeRange == null)
            return issueTimeRange;
        return actuationTimeRange;
    }


    /**
     * @return The total number of selected commands
     */
    public long getTotalCommandCount()
    {
        return totalCommandCount;
    }
    

    /**
     * @return The histogram of command counts vs. time
     */
    public int[] getCommandCountsByTime()
    {
        return commandCountsByTime;
    }


    @Override
    public String toString()
    {
        return ObjectUtils.toString(this, true);
    }
    
    
    /*
     * Builder
     */
    public static class Builder extends CommandStatsBuilder<Builder, CommandStats>
    {
        public Builder()
        {
            this.instance = new CommandStats();
        }
        
        public static Builder from(CommandStats base)
        {
            return new Builder().copyFrom(base);
        }
    }
    
    
    @SuppressWarnings("unchecked")
    public static abstract class CommandStatsBuilder<
            B extends CommandStatsBuilder<B, T>,
            T extends CommandStats>
        extends BaseBuilder<T>
    {       
        protected CommandStatsBuilder()
        {
        }
        
        
        protected B copyFrom(CommandStats base)
        {
            instance.commandStreamID = base.commandStreamID;
            instance.totalCommandCount = base.totalCommandCount;
            instance.issueTimeRange = base.issueTimeRange;
            instance.actuationTimeRange = base.actuationTimeRange;
            instance.commandCountsByTime = base.commandCountsByTime;
            return (B)this;
        }

        
        public B withCommandStreamID(BigId commandStreamID)
        {
            instance.commandStreamID = commandStreamID;
            return (B)this;
        }


        public B withIssueTimeRange(TimeExtent timeRange)
        {
            instance.issueTimeRange = timeRange;
            return (B)this;
        }


        public B withActuationTimeRange(TimeExtent timeRange)
        {
            instance.actuationTimeRange = timeRange;
            return (B)this;
        }


        public B withTotalCommandCount(long count)
        {
            instance.totalCommandCount = count;
            return (B)this;
        }


        public B withCommandCountByTime(int[] counts)
        {
            instance.commandCountsByTime = counts;
            return (B)this;
        }
        
        
        public T build()
        {
            Asserts.checkNotNull(instance.commandStreamID, "commandStreamID");
            Asserts.checkState(instance.issueTimeRange != null || instance.actuationTimeRange != null, "At least one time range must be set");
            return super.build();
        }
    }
}
