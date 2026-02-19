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


/**
 * <p>
 * H2 DataType implementation to index observation series by FOI ID,
 * then datastream ID, then result time.
 * </p>
 *
 * @author Alex Robin
 * @date Sep 12, 2019
 */
class MVObsSeriesKeyByFoiDataType extends MVObsSeriesKeyByDataStreamDataType
{
        
    @Override
    public int compare(Object objA, Object objB)
    {
        MVTimeSeriesKey a = (MVTimeSeriesKey)objA;
        MVTimeSeriesKey b = (MVTimeSeriesKey)objB;
        
        // first compare FOI IDs
        int comp = Long.compare(a.foiID, b.foiID);
        if (comp != 0)
            return comp;
        
        // if FOI IDs are the same, compare datastream IDs
        comp = Long.compare(a.dataStreamID, b.dataStreamID);
        if (comp != 0)
            return comp;
        
        // if datastream IDs are the same, compare result time stamps
        return a.resultTime.compareTo(b.resultTime);
    }
}
