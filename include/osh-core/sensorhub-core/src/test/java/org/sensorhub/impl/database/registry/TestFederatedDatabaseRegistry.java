/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.database.registry;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sensorhub.api.database.IDatabase;
import org.sensorhub.api.database.IDatabaseRegistry;
import org.sensorhub.api.database.IFederatedDatabase;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.api.datastore.feature.FoiFilter;
import org.sensorhub.api.datastore.obs.DataStreamFilter;
import org.sensorhub.api.datastore.obs.ObsFilter;
import org.sensorhub.api.datastore.procedure.ProcedureFilter;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.impl.SensorHub;
import org.sensorhub.impl.datastore.mem.InMemoryProcedureDatabase;
import org.sensorhub.impl.datastore.mem.InMemorySystemStateDatabase;
import org.sensorhub.impl.system.wrapper.SystemWrapper;
import org.vast.sensorML.SimpleProcessImpl;
import net.opengis.sensorml.v20.AbstractProcess;


public class TestFederatedDatabaseRegistry
{
    IDatabaseRegistry registry;
    IFederatedDatabase federatedDatabase;
    
    
    @Rule
    @SuppressWarnings("deprecation")
    public ExpectedException errors = ExpectedException.none();
    
    
    @Before
    public void setup() throws Exception
    {
        SensorHub hub = new SensorHub();
        hub.start();
        this.registry = hub.getDatabaseRegistry();
        this.federatedDatabase = registry.getFederatedDatabase();
    }
    
    
    @Test
    public void testRegister()
    {
        IDatabase db1, db2;
        
        registry.register(db1 = new InMemorySystemStateDatabase((byte)1));
        registry.register(db2 = new InMemorySystemStateDatabase((byte)22));
        
        assertEquals(registry.getObsDatabaseByNum(1), db1);
        assertEquals(registry.getObsDatabaseByNum(22), db2);
    }


    @Test
    public void testRegisterDuplicateIDs()
    {
        IDatabase db1;
        registry.register(db1 = new InMemorySystemStateDatabase((byte)1));
        
        errors.expect(IllegalStateException.class);
        registry.register(new InMemorySystemStateDatabase((byte)1));
        
        assertEquals(registry.getObsDatabaseByNum(1), db1);
    }
    
    
    @Test
    public void testEmptyDatabases()
    {
        registry.register(new InMemorySystemStateDatabase((byte)3));
        registry.register(new InMemorySystemStateDatabase((byte)4));
        registry.register(new InMemoryProcedureDatabase((byte)55));
        
        assertEquals(0, federatedDatabase.getSystemDescStore().getNumFeatures());
        assertEquals(0, federatedDatabase.getSystemDescStore().getNumRecords());
        assertEquals(0, federatedDatabase.getFoiStore().getNumFeatures());
        assertEquals(0, federatedDatabase.getFoiStore().getNumRecords());
        assertEquals(0, federatedDatabase.getObservationStore().getNumRecords());
        assertEquals(0, federatedDatabase.getObservationStore().getDataStreams().getNumRecords());
        assertEquals(0, federatedDatabase.getProcedureStore().getNumFeatures());
        
        assertTrue(federatedDatabase.getSystemDescStore().isEmpty());
        assertTrue(federatedDatabase.getFoiStore().isEmpty());
        assertTrue(federatedDatabase.getObservationStore().isEmpty());
        assertTrue(federatedDatabase.getObservationStore().getDataStreams().isEmpty());
        assertTrue(federatedDatabase.getProcedureStore().isEmpty());
        
        assertEquals(0, federatedDatabase.getSystemDescStore().keySet().size());
        assertEquals(0, federatedDatabase.getFoiStore().keySet().size());
        assertEquals(0, federatedDatabase.getObservationStore().keySet().size());
        assertEquals(0, federatedDatabase.getObservationStore().getDataStreams().keySet().size());
        assertEquals(0, federatedDatabase.getProcedureStore().keySet().size());
        
        assertEquals(0, federatedDatabase.getSystemDescStore().entrySet().size());
        assertEquals(0, federatedDatabase.getFoiStore().entrySet().size());
        assertEquals(0, federatedDatabase.getObservationStore().entrySet().size());
        assertEquals(0, federatedDatabase.getObservationStore().getDataStreams().entrySet().size());
        assertEquals(0, federatedDatabase.getProcedureStore().entrySet().size());
        
        assertEquals(0, federatedDatabase.getSystemDescStore().values().size());
        assertEquals(0, federatedDatabase.getFoiStore().values().size());
        assertEquals(0, federatedDatabase.getObservationStore().values().size());
        assertEquals(0, federatedDatabase.getObservationStore().getDataStreams().values().size());
        assertEquals(0, federatedDatabase.getProcedureStore().values().size());
        
        assertEquals(0, federatedDatabase.getSystemDescStore().selectEntries(new SystemFilter.Builder().build()).count());
        assertEquals(0, federatedDatabase.getFoiStore().selectEntries(new FoiFilter.Builder().build()).count());
        assertEquals(0, federatedDatabase.getObservationStore().selectEntries(new ObsFilter.Builder().build()).count());
        assertEquals(0, federatedDatabase.getObservationStore().getDataStreams().selectEntries(new DataStreamFilter.Builder().build()).count());
        assertEquals(0, federatedDatabase.getProcedureStore().selectEntries(new ProcedureFilter.Builder().build()).count());
    }
    
    
    @Test
    public void testPublicKeys() throws Exception
    {
        IObsSystemDatabase db1 = new InMemorySystemStateDatabase((byte)1);
        IObsSystemDatabase db2 = new InMemorySystemStateDatabase((byte)2);
        registry.register(db1);
        registry.register(db2);
        
        AbstractProcess proc1 = new SimpleProcessImpl();
        proc1.setUniqueIdentifier("sensor001");
        FeatureKey fk1 = db1.getSystemDescStore().add(new SystemWrapper(proc1));
    
        AbstractProcess proc3 = new SimpleProcessImpl();
        proc3.setUniqueIdentifier("sensor003");
        FeatureKey fk3 = db2.getSystemDescStore().add(new SystemWrapper(proc3));
        
        FeatureKey id1 = federatedDatabase.getSystemDescStore().getCurrentVersionKey("sensor001");
        assertEquals(fk1, id1);
        assertTrue(id1.getInternalID().getScope() == db1.getDatabaseNum());
        
        FeatureKey id3 = federatedDatabase.getSystemDescStore().getCurrentVersionKey("sensor003");
        assertEquals(fk3, id3);
        assertTrue(id3.getInternalID().getScope() == db2.getDatabaseNum());
        
        assertEquals(2, federatedDatabase.getSystemDescStore().keySet().size());
    }

}
