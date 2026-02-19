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

import java.time.Instant;
import java.util.Objects;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.resource.ResourceKey;
import org.sensorhub.utils.ObjectUtils;
import org.vast.util.Asserts;


/**
 * <p>
 * Immutable key object used to index features in data stores.<br/>
 * The key includes the feature internal ID and its validity time stamp.
 * </p>
 *
 * @author Alex Robin
 * @since Mar 19, 2018
 */
public class FeatureKey extends ResourceKey<FeatureKey>
{    
    public final static Instant TIMELESS = Instant.MIN;
    
    protected Instant validStartTime = TIMELESS;
    
    
    /**
     * Creates a key for a timeless feature
     * @param internalID Internal ID of desired feature
     */
    public FeatureKey(BigId internalID)
    {
        super(internalID);
    }
    
    
    /**
     * Creates a key for the version of a feature valid at a specific time
     * @param internalID Internal ID of desired feature
     * @param validStartTime Start of feature version validity period
     */
    public FeatureKey(BigId internalID, Instant validStartTime)
    {
        super(internalID);
        this.validStartTime = Asserts.checkNotNull(validStartTime, Instant.class);
    }
    
    
    /**
     * Creates a key for the version of a feature valid at a specific time
     * @param scope Internal ID scope
     * @param internalID Internal ID of desired feature
     * @param validStartTime Start of feature version validity period
     */
    public FeatureKey(int scope, long internalID, Instant validStartTime)
    {
        this(BigId.fromLong(scope, internalID), validStartTime);
    }


    /**
     * @return The start of validity of the feature description.<br/>
     * If null or equal to {@link TIMELESS}, the feature will always be returned
     * by queries containing a temporal filter 
     */    
    public Instant getValidStartTime()
    {
        return validStartTime;
    }


    @Override
    public String toString()
    {
        return ObjectUtils.toString(this, true);
    }
    

    @Override
    public int hashCode()
    {
        return Objects.hash(
            getInternalID(),
            getValidStartTime());
    }
    
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        
        if (!(obj instanceof FeatureKey))
            return false;
        
        FeatureKey other = (FeatureKey)obj;
        return Objects.equals(getInternalID(), other.getInternalID()) &&
               Objects.equals(getValidStartTime(), other.getValidStartTime());
    }


    @Override
    public int compareTo(FeatureKey other)
    {
        int res = super.compareTo(other);
        if (res == 0)
            res = getValidStartTime().compareTo(other.getValidStartTime());
        return res;
    }
}
