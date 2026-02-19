/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2023 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.h2;

import org.h2.mvstore.MVBTreeMap;
import org.sensorhub.api.feature.FeatureWrapper;
import org.vast.ogc.gml.IFeature;
import org.vast.util.TimeExtent;


public class FeatureValidTimeAdapter<V extends IFeature> extends FeatureWrapper implements IFeature
{
    final TimeExtent validTime;

    
    public FeatureValidTimeAdapter(MVFeatureParentKey fk, V f, MVBTreeMap<MVFeatureParentKey, V> featuresIndex)
    {
        super(f);
        
        // compute valid time
        var nextKey = featuresIndex.higherKey(fk);
        if (nextKey != null && f.getValidTime() != null && f.getValidTime().endsNow() &&
            nextKey.getInternalID().getIdAsLong() == fk.getInternalID().getIdAsLong())
        {
            validTime = TimeExtent.period(f.getValidTime().begin(), nextKey.getValidStartTime());
        }
        else
            validTime = f.getValidTime();
    }
    

    public TimeExtent getValidTime()
    {
        return validTime;
    }
}
