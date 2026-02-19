/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2023 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore.obs;

import java.util.stream.Stream;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.data.IObsData;
import org.sensorhub.api.datastore.feature.IFoiStore;
import org.sensorhub.api.datastore.obs.IObsStore.ObsField;
import org.sensorhub.impl.datastore.EmptyDataStore;


/**
 * <p>
 * Helper class to implement databases that don't support all datastores
 * </p>
 * 
 * @author Alex Robin
 * @since Jun 22, 2023
 */
public class EmptyObsStore
    extends EmptyDataStore<BigId, IObsData, ObsField, ObsFilter> implements IObsStore
{
    final IDataStreamStore dataStreamStore;
    
    
    public EmptyObsStore()
    {
        this.dataStreamStore = new EmptyDataStreamStore();
    }
    
    
    @Override
    public IDataStreamStore getDataStreams()
    {
        return dataStreamStore;
    }


    @Override
    public BigId add(IObsData obs)
    {
        throw new UnsupportedOperationException(READ_ONLY_ERROR_MSG);
    }


    @Override
    public Stream<ObsStats> getStatistics(ObsStatsQuery query)
    {
        return Stream.empty();
    }


    @Override
    public void linkTo(IFoiStore foiStore)
    {
    }
}
