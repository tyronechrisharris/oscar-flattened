/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore.obs;

import java.util.stream.Stream;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.data.IObsData;
import org.sensorhub.api.datastore.IDataStore;
import org.sensorhub.api.datastore.ValueField;
import org.sensorhub.api.datastore.feature.IFoiStore;
import org.sensorhub.api.datastore.obs.IObsStore.ObsField;
import net.opengis.swe.v20.DataBlock;
import com.google.common.collect.Sets;


/**
 * <p>
 * Generic interface for data stores containing observations.
 * </p><p>
 * Observations are organized into data streams. Each data stream contains
 * observations sharing the same result type (i.e. record structure).
 * </p><p>
 * Observations retrieved by select methods are sorted by phenomenon time and
 * grouped by result time when several result times are requested.
 * </p>
 *
 * @author Alex Robin
 * @date Mar 19, 2018
 */
public interface IObsStore extends IDataStore<BigId, IObsData, ObsField, ObsFilter>
{
    public static class ObsField extends ValueField
    {
        public static final ObsField DATASTREAM_ID = new ObsField("dataStreamID");
        public static final ObsField FOI_ID = new ObsField("foiID");
        public static final ObsField PHENOMENON_TIME = new ObsField("phenomenonTime");
        public static final ObsField RESULT_TIME  = new ObsField("resultTime");
        public static final ObsField PHENOMENON_LOCATION = new ObsField("phenomenonLocation");
        public static final ObsField PARAMETERS = new ObsField("parameters");
        public static final ObsField RESULT = new ObsField("result");
        
        public ObsField(String name)
        {
            super(name);
        }
    }
    
    
    /**
     * @return Interface to manage data streams
     */
    IDataStreamStore getDataStreams();
    
    
    /**
     * Add an observation to the datastore
     * @param obs
     * @return The auto-generated ID
     */
    public BigId add(IObsData obs);
    
    
    /**
     * Select all observations matching the query and return result datablocks only
     * @param filter Observation filter
     * @return Stream of result data blocks
     */
    public default Stream<DataBlock> selectResults(ObsFilter filter)
    {
        return select(filter, Sets.newHashSet(ObsField.RESULT))
            .map(o -> o.getResult());
    }
    
    
    /**
     * Select all FOIs for which observation matching the filter are available
     * @param filter
     * @return Stream of FOI internal IDs
     */
    public default Stream<BigId> selectObservedFois(ObsFilter filter)
    {
        return select(filter, Sets.newHashSet(ObsField.FOI_ID))
            .filter(o -> o.hasFoi())
            .map(o -> o.getFoiID())
            .distinct();
    }
    
    
    /**
     * Select statistics for systems and FOI matching the query
     * @param query filter to select desired systems and FOIs
     * @return stream of statistics buckets. Each item represents statistics for
     * observations collected for a combination of system, feature of
     * interest, and result time. 
     */
    public Stream<ObsStats> getStatistics(ObsStatsQuery query);


    /**
     * @return A builder for a filter compatible with this datastore
     */
    public default ObsFilter.Builder filterBuilder()
    {
        return new ObsFilter.Builder();
    }
    
    
    @Override
    public default ObsFilter selectAllFilter()
    {
        return filterBuilder().build();
    }
    
    
    /**
     * Link this store to a feature of interest store to enable JOIN queries
     * @param foiStore
     */
    public void linkTo(IFoiStore foiStore);
    
}
