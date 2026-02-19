/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2023 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore;

import java.util.Set;
import java.util.stream.Stream;
import org.sensorhub.api.datastore.IQueryFilter;
import org.sensorhub.api.datastore.ValueField;


public abstract class EmptyDataStore<K, V, VF extends ValueField, Q extends IQueryFilter> extends ReadOnlyDataStore<K, V, VF, Q>
{

    @Override
    public String getDatastoreName()
    {
        return "Empty Datastore";
    }
    

    @Override
    public Stream<Entry<K, V>> selectEntries(Q query, Set<VF> fields)
    {
        return Stream.empty();
    }
    

    @Override
    public V get(Object arg0)
    {
        return null;
    }

}
