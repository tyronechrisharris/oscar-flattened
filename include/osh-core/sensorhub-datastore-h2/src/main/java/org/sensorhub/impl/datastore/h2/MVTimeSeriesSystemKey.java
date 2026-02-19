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
import org.vast.util.Asserts;


/**
 * <p>
 * Immutable key object used to index a datastream or command stream by its
 * parent system ID and output/control input name.
 * </p>
 *
 * @author Alex Robin
 * @since Sep 19, 2019
 */
class MVTimeSeriesSystemKey
{    
    long internalID;
    long systemID;
    String signalName;
    long validStartTime; // seconds past unix epoch
    
    
    MVTimeSeriesSystemKey(long sysID, String outputName, Instant validStartTime)
    {
        this(0, sysID, outputName, validStartTime.getEpochSecond());
    }
    
    
    MVTimeSeriesSystemKey(long internalID, long sysID, String signalName, long validStartTime)
    {
        this.internalID = internalID;
        this.systemID = sysID;
        this.signalName = Asserts.checkNotNull(signalName, "signalName");
        this.validStartTime = validStartTime;
    }


    @Override
    public String toString()
    {
        return ObjectUtils.toString(this, true);
    }
}
