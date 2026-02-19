/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.obs;

import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.api.datastore.obs.DataStreamFilter;
import org.sensorhub.api.datastore.obs.DataStreamKey;
import org.sensorhub.api.datastore.obs.IDataStreamStore;
import org.sensorhub.api.datastore.obs.IDataStreamStore.DataStreamInfoField;
import org.sensorhub.api.datastore.system.ISystemDescStore;
import org.sensorhub.impl.service.consys.resource.AbstractResourceStoreWrapper;


public class DataStreamStoreWrapper extends AbstractResourceStoreWrapper<DataStreamKey, IDataStreamInfo, DataStreamInfoField, DataStreamFilter, IDataStreamStore> implements IDataStreamStore
{
    
    public DataStreamStoreWrapper(IDataStreamStore readStore, IDataStreamStore writeStore)
    {
        super(readStore, writeStore);
    }


    @Override
    public DataStreamFilter.Builder filterBuilder()
    {
        return (DataStreamFilter.Builder)super.filterBuilder();
    }


    @Override
    public void linkTo(ISystemDescStore systemStore)
    {
        throw new UnsupportedOperationException();
    }

}
