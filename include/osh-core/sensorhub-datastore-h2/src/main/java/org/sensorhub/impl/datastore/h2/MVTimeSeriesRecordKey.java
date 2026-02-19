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


/**
 * <p>
 * Internal key used to index observations or commands by series ID and
 * timestamp. The full ObsKey/CommandKey is reconstructed when the series
 * info is known.
 * </p>
 *
 * @author Alex Robin
 * @date Sep 12, 2019
 */
class MVTimeSeriesRecordKey implements BigId
{
    int scope;
    final long seriesID;
    final Instant timeStamp;
    AtomicInitializer<byte[]> cachedId = new AtomicInitializer<>();
    
    
    /*
     * Constructor w/o scope, used for querying only!
     */
    MVTimeSeriesRecordKey(long seriesID, Instant timeStamp)
    {
        this.seriesID = seriesID;
        this.timeStamp = timeStamp;
    }
    
    
    MVTimeSeriesRecordKey(int scope, long seriesID, Instant timeStamp)
    {
        this(seriesID, timeStamp);
        this.scope = scope;
    }


    public long getSeriesID()
    {
        return seriesID;
    }


    public Instant getTimeStamp()
    {
        return timeStamp;
    }


    @Override
    public int getScope()
    {
        return scope;
    }


    @Override
    public byte[] getIdAsBytes()
    {
        // compute byte[] representation lazily
        return cachedId.get(() -> {
            var buf = new FixedSizeWriteBuffer(MVTimeSeriesRecordKeyDataType.getEncodedLen(this));
            MVTimeSeriesRecordKeyDataType.encode(buf, this);
            return buf.getBuffer().array();
        });
    }


    @Override
    public int hashCode()
    {
        return Objects.hash(
            scope, seriesID, timeStamp
        );
    }


    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof MVTimeSeriesRecordKey))
            return false;
        
        var other = (MVTimeSeriesRecordKey)o;
        return scope == other.scope &&
               seriesID == other.seriesID &&
               Objects.equals(timeStamp, other.timeStamp);
    }


    @Override
    public String toString()
    {
        return String.format("BigId {scope=%d, id='%s'}", scope, BASE32_ENCODING.encode(getIdAsBytes()));
        //return ObjectUtils.toString(this, true);
    }
}
