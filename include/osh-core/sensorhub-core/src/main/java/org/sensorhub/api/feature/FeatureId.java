/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.

******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.feature;

import java.util.Objects;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.utils.OshAsserts;


/**
 * <p>
 * Immutable data class containing both internal ID and globally unique ID
 * (URI) of a feature.
 * </p>
 *
 * @author Alex Robin
 * @date Sep 17, 2019
 */
public class FeatureId
{
    public static FeatureId NULL_FEATURE = new FeatureId(BigId.NONE);

    protected BigId internalID;
    protected String uniqueID;


    protected FeatureId()
    {
    }


    private FeatureId(BigId internalID)
    {
        this.internalID = internalID;
    }


    public FeatureId(BigId internalID, String uid)
    {
        this(OshAsserts.checkValidInternalID(internalID));
        this.uniqueID = OshAsserts.checkValidUID(uid);
    }


    /**
     * @return The feature internal ID
     */
    public BigId getInternalID()
    {
        return internalID;
    }


    /**
     * @return The feature unique ID
     */
    public String getUniqueID()
    {
        return uniqueID;
    }


    @Override
    public int hashCode()
    {
        return java.util.Objects.hash(
                getInternalID(),
                getUniqueID());
    }


    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof FeatureId))
            return false;

        FeatureId other = (FeatureId)obj;
        return Objects.equals(getInternalID(), other.getInternalID()) &&
               Objects.equals(getUniqueID(), other.getUniqueID());
    }


    @Override
    public String toString()
    {
        return uniqueID + " (" + internalID + ")";
    }
}
