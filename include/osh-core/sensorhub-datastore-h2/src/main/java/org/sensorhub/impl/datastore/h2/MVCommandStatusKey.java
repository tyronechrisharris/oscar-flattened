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
import java.util.Objects;
import org.sensorhub.api.common.BigId;
import org.sensorhub.utils.AtomicInitializer;
import org.sensorhub.utils.ObjectUtils;
import org.vast.util.Asserts;


/**
 * <p>
 * Key to index command status reports.
 * </p>
 *
 * @author Alex Robin
 * @date Jan 5, 2021
 */
class MVCommandStatusKey implements BigId
{
    BigId cmdID;
    Instant reportTime;
    AtomicInitializer<byte[]> cachedId = new AtomicInitializer<>();
    
    
    MVCommandStatusKey(BigId cmdID, Instant reportTime)
    {
        this.cmdID = Asserts.checkNotNull(cmdID, "cmdID");
        this.reportTime = reportTime;
    }


    @Override
    public int getScope()
    {
        return cmdID.getScope();
    }


    @Override
    public byte[] getIdAsBytes()
    {
        // compute byte[] representation lazily
        return cachedId.get(() -> {
            var wbuf = new FixedSizeWriteBuffer(MVCommandStatusKeyDataType.getEncodedLen(this));
            MVCommandStatusKeyDataType.encode(wbuf, this);
            return wbuf.getBuffer().array();
        });
    }


    @Override
    public int hashCode()
    {
        return Objects.hash(
            cmdID, reportTime
        );
    }


    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof MVCommandStatusKey))
            return false;
        
        var other = (MVCommandStatusKey)o;
        return Objects.equals(cmdID, other.cmdID) &&
               Objects.equals(reportTime, other.reportTime);
    }


    @Override
    public String toString()
    {
        return ObjectUtils.toString(this, true);
    }
}
