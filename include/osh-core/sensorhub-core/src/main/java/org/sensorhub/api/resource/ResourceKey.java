/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.resource;

import java.util.Objects;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.utils.OshAsserts;
import org.sensorhub.utils.ObjectUtils;


/**
 * <p>
 * Key used for indexing and referencing resources by their internal ID
 * </p>
 *
 * @author Alex Robin
 * @date Oct 29, 2018
 */
@SuppressWarnings("javadoc")
public class ResourceKey<T extends ResourceKey<T>> implements Comparable<T>
{
    protected BigId internalID = BigId.NONE;
    
    
    /* for deserialization only */
    protected ResourceKey()
    {
    }
    
    
    public ResourceKey(BigId internalID)
    {
        this.internalID = OshAsserts.checkValidInternalID(internalID, "internalID");
    }
    
    
    /**
     * @return Resource internal numeric ID
     */
    public BigId getInternalID()
    {
        return internalID;
    }


    @Override
    public String toString()
    {
        return ObjectUtils.toString(this, true);
    }
    

    @Override
    public int hashCode()
    {
        return internalID.hashCode();
    }
    
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        
        if (!(obj instanceof ResourceKey))
            return false;
        
        ResourceKey<?> other = (ResourceKey<?>)obj;
        return Objects.equals(getInternalID(), other.getInternalID());
    }


    @Override
    public int compareTo(T o)
    {
        return BigId.compareLongs(internalID, o.getInternalID());
    }
}
