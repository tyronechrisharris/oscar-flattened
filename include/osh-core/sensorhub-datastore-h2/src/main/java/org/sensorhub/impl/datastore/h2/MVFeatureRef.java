/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.h2;

import java.time.Instant;
import org.sensorhub.utils.ObjectUtils;
import com.google.common.collect.Range;


/**
 * <p>
 * Internal object stored in secondary indexes to reference a feature
 * </p>
 *
 * @author Alex Robin
 * @date Apr 12, 2018
 */
public class MVFeatureRef
{
    private long internalID;
    private long parentID;
    private Range<Instant> validityPeriod;
    //private Geometry geom;
    
    
    public MVFeatureRef(long parentID, long internalID, Range<Instant> validityPeriod)
    {
        this.parentID = parentID;
        this.internalID = internalID;
        this.validityPeriod = validityPeriod;
    }
    

    public long getInternalID()
    {
        return internalID;
    }


    public long getParentID()
    {
        return parentID;
    }


    /*public Geometry getGeom()
    {
        return geom;
    }*/


    public Range<Instant> getValidityPeriod()
    {
        return validityPeriod;
    }


    @Override
    public String toString()
    {
        return ObjectUtils.toString(this, true);
    }
}
