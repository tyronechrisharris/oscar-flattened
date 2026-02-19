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
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.vast.util.Asserts;


/**
 * <p>
 * Key used for associating features with their parent.
 * Note: we don't override hashcode and equals so we can still compare with
 * plain FeatureKey objects.
 * </p>
 *
 * @author Alex Robin
 * @date Oct 28, 2020
 */
public class MVFeatureParentKey extends FeatureKey
{
    protected long parentID; // 0 indicates no parent
    
    
    public MVFeatureParentKey(long parentID, BigId internalID, Instant validStartTime)
    {
        super(internalID, validStartTime);
        
        Asserts.checkArgument(parentID >= 0, "Invalid parentID");
        this.parentID = parentID;
    }
    
    
    public MVFeatureParentKey(int idScope, long parentID, long internalID, Instant validStartTime)
    {
        super(idScope, internalID, validStartTime);
        
        Asserts.checkArgument(parentID >= 0, "Invalid parentID");
        this.parentID = parentID;
    }
    

    public long getParentID()
    {
        return parentID;
    }
}
