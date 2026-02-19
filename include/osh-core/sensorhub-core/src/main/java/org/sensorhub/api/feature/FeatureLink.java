/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2024 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.feature;

import java.util.Objects;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.utils.OshAsserts;
import org.vast.ogc.xlink.ExternalLink;
import org.vast.util.Asserts;


public class FeatureLink extends FeatureId
{
    protected final ExternalLink link;


    public FeatureLink(ExternalLink link)
    {
        this.link = Asserts.checkNotNull(link, ExternalLink.class);
        OshAsserts.checkValidURI(link.getHref());
    }
    
    
    public FeatureLink(BigId internalId, ExternalLink link)
    {
        this(link);
        this.internalID = Asserts.checkNotNull(internalId, BigId.class);
    }
    
    
    /**
     * @return The link to the external feature entity
     */
    public ExternalLink getLink()
    {
        return link;
    }
    
    
    @Override
    public String getUniqueID()
    {
        if (uniqueID != null)
            return uniqueID;
        else if (link != null)
            return link.getTargetUID();
        return null;
    }


    @Override
    public int hashCode()
    {
        return java.util.Objects.hash(
                getLink().getHref(),
                getUniqueID());
    }


    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof FeatureLink))
            return false;

        FeatureLink other = (FeatureLink)obj;
        return Objects.equals(getLink().getHref(), other.getLink().getHref()) &&
               Objects.equals(getUniqueID(), other.getUniqueID());
    }


    @Override
    public String toString()
    {
        return getUniqueID() + " (" + getLink().getHref() + ")";
    }
}
