/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.system;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.DataStoreException;
import org.sensorhub.api.datastore.command.CommandStreamKey;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.api.datastore.obs.DataStreamKey;
import org.sensorhub.api.event.IEventBus;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.api.system.SystemAddedEvent;
import org.sensorhub.api.utils.OshAsserts;
import org.sensorhub.impl.datastore.DataStoreUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.ogc.gml.IFeature;
import org.vast.util.Asserts;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


/**
 * <p>
 * Helper class for creating/updating/deleting (observing) systems and their
 * components in the associated database and publishing the corresponding
 * events.
 * </p>
 *
 * @author Alex Robin
 * @date Dec 21, 2020
 */
public class SystemDatabaseTransactionHandler
{
    static final Logger log = LoggerFactory.getLogger(SystemDatabaseTransactionHandler.class);
    
    final protected IEventBus eventBus;
    final protected IObsSystemDatabase db;
    
    
    /*
     * db must always be a transactional DB, not the federated DB
     * exception is when submitting commands to systems that are not in the local DB
     */
    public SystemDatabaseTransactionHandler(IEventBus eventBus, IObsSystemDatabase db)
    {
        this.eventBus = Asserts.checkNotNull(eventBus, IEventBus.class);
        this.db = Asserts.checkNotNull(db, IObsSystemDatabase.class);
    }
    
    
    /**
     * Add a new system with the provided description
     * @param system System description
     * @return The transaction handler linked to the system
     * @throws DataStoreException if a system with the same UID already exists
     */
    public SystemTransactionHandler addSystem(ISystemWithDesc system) throws DataStoreException
    {
        OshAsserts.checkSystemObject(system);
        
        // add system to store
        var systemKey = db.getSystemDescStore().add(system);
        var sysUID = system.getUniqueIdentifier();
        log.debug("Added System {}", sysUID);
        
        // create new system handler
        var sysHandler = createSystemHandler(systemKey, sysUID);
        
        // publish event
        sysHandler.publishSystemEvent(new SystemAddedEvent(sysUID, null));
        
        return sysHandler;
    }
    
    
    /**
     * Add system or return ID of existing one.
     * If no system with the same UID and validTime already exists, a new one will be created,
     * otherwise the existing one will be returned.
     * @param system New system description
     * @return The transaction handler linked to the system
     * @throws DataStoreException if the system couldn't be added or updated
     */
    public SystemTransactionHandler addSystemOrReturnExisting(ISystemWithDesc system) throws DataStoreException
    {
        OshAsserts.checkSystemObject(system);
        
        var systemHandler = getSystemHandler(system.getUniqueIdentifier());
        if (systemHandler != null)
        {
            var validTime = system.getValidTime();
            var sameValidTime = validTime == null ||
                systemHandler.sysKey.getValidStartTime().equals(validTime.begin());
            
            if (sameValidTime)
                return systemHandler;
        }
        
        return addSystem(system);
    }
    
    
    /**
     * Add or update a system.
     * If no system with the same UID already exists, a new one will be created,
     * otherwise the existing one will be updated or versioned depending if the the validity
     * period was changed.
     * @param system New system description
     * @return The transaction handler linked to the system
     * @throws DataStoreException if the system couldn't be added or updated
     */
    public SystemTransactionHandler addOrUpdateSystem(ISystemWithDesc system) throws DataStoreException
    {
        OshAsserts.checkSystemObject(system);
        
        var systemHandler = getSystemHandler(system.getUniqueIdentifier());
        if (systemHandler != null)
        {
            systemHandler.update(system);
            return systemHandler;
        }
        else
            return addSystem(system);
    }
    
    
    /**
     * Add or update a feature of interest that is not associated to a particular system
     * @param parentId Internal ID of parent, or null if no parent. 
     * @param foi New feature description
     * @return The key of the newly created or updated feature
     * @throws DataStoreException
     */
    public synchronized FeatureKey addOrUpdateFoi(BigId parentId, IFeature foi) throws DataStoreException
    {
        DataStoreUtils.checkFeatureObject(foi);
        
        // add or update if feature with same ID and valid time exists
        FeatureKey fk = db.getFoiStore().getCurrentVersionKey(foi.getUniqueIdentifier());
        if (fk != null &&
           (foi.getValidTime() == null || Objects.equals(foi.getValidTime().begin(), fk.getValidStartTime())))
        {
            db.getFoiStore().put(fk, foi);
            log.debug("Updated FOI {}", foi.getUniqueIdentifier());
        }
        else
            fk = addFoi(parentId, foi);
        
        return fk;
    }
    
    
    /**
     * Add a new feature of interest that is not associated to a particular system
     * @param parentId Internal ID of parent, or null if no parent.
     * @param foi New feature description
     * @return The key of the newly created feature
     * @throws DataStoreException
     */
    public synchronized FeatureKey addFoi(BigId parentId, IFeature foi) throws DataStoreException
    {
        DataStoreUtils.checkFeatureObject(foi);
        
        var fk = db.getFoiStore().add(parentId, foi);
        log.debug("Added FOI {}", foi.getUniqueIdentifier());
        return fk;
    }
    
    
    /**
     * Update the feature of interest with the specified internal ID
     * @param internalId
     * @param foi
     * @return True if the feature was updated, false otherwise
     * @throws DataStoreException
     */
    public synchronized boolean updateFoi(BigId internalId, IFeature foi) throws DataStoreException
    {
        OshAsserts.checkValidInternalID(internalId);
        DataStoreUtils.checkFeatureObject(foi);
        
        var fk = db.getFoiStore().getCurrentVersionKey(internalId);
        if (fk == null)
            return false;
        
        try
        {
            db.getFoiStore().put(fk, foi);
            return true;
        }
        catch (IllegalArgumentException e)
        {
            throw new DataStoreException(e.getMessage());
        }
    }
    
    
    /**
     * Create a handler for an existing system with the specified ID
     * @param id System internal ID
     * @return The new system handler or null if system doesn't exist
     */
    public SystemTransactionHandler getSystemHandler(BigId id)
    {
        OshAsserts.checkValidInternalID(id);
        
        // load system object from DB
        var systemEntry = db.getSystemDescStore().getCurrentVersionEntry(id);
        if (systemEntry == null)
            return null;
        
        // create new system handler
        var systemKey = systemEntry.getKey();
        var sysUID = systemEntry.getValue().getUniqueIdentifier();
        return createSystemHandler(systemKey, sysUID);
    }
    
    
    /**
     * Create a handler for an existing system with the specified unique ID
     * @param sysUID System unique ID
     * @return The new system handler or null if system doesn't exist
     */
    public SystemTransactionHandler getSystemHandler(String sysUID)
    {
        OshAsserts.checkValidUID(sysUID);
        
        // load system object from DB
        var systemKey = db.getSystemDescStore().getCurrentVersionKey(sysUID);
        if (systemKey == null)
            return null;
        
        // create new system handler
        return createSystemHandler(systemKey, sysUID);
    }
    
    
    protected SystemTransactionHandler createSystemHandler(FeatureKey systemKey, String sysUID)
    {
        return new SystemTransactionHandler(systemKey, sysUID, this);
    }
    
    
    /**
     * Create a handler for an existing datastream with the specified ID
     * @param id Datastream internal ID
     * @return The new datastream handler or null if datastream doesn't exist
     */
    public DataStreamTransactionHandler getDataStreamHandler(BigId id)
    {
        OshAsserts.checkValidInternalID(id);
        
        // load datastream info from DB
        var dsKey = new DataStreamKey(id);
        var dsInfo = db.getDataStreamStore().get(dsKey);
        if (dsInfo == null)
            return null;
        
        // create new datastream handler
        return new DataStreamTransactionHandler(dsKey, dsInfo, this);
    }
    
    
    /**
     * Create a handler for an existing datastream with the specified system and output name
     * @param sysUID System unique ID
     * @param outputName Output name
     * @return The new datastream handler or null if datastream doesn't exist
     */
    public DataStreamTransactionHandler getDataStreamHandler(String sysUID, String outputName)
    {
        OshAsserts.checkValidUID(sysUID);
        Asserts.checkNotNullOrEmpty(outputName, "outputName");
        
        // load datastream info from DB
        var dsEntry = db.getDataStreamStore().getLatestVersionEntry(sysUID, outputName);
        if (dsEntry == null)
            return null;
        
        // create new datastream handler
        return new DataStreamTransactionHandler(dsEntry.getKey(), dsEntry.getValue(), this);
    }
    
    
    /**
     * Create a handler for an existing command stream with the specified ID
     * @param id Command stream internal ID
     * @return The new command stream handler or null if command stream doesn't exist
     */
    public CommandStreamTransactionHandler getCommandStreamHandler(BigId id)
    {
        OshAsserts.checkValidInternalID(id);
        
        // load command stream info from DB
        var csKey = new CommandStreamKey(id);
        var csInfo = db.getCommandStreamStore().get(csKey);
        if (csInfo == null)
            return null;
        
        // create new command stream handler
        return new CommandStreamTransactionHandler(csKey, csInfo, this);
    }
    
    
    /**
     * Create a handler for an existing command stream with the specified system
     * and control input name
     * @param sysUID system unique ID
     * @param controlInputName Control input name
     * @return The new command stream handler or null if command stream doesn't exist
     */
    public CommandStreamTransactionHandler getCommandStreamHandler(String sysUID, String controlInputName)
    {
        OshAsserts.checkValidUID(sysUID);
        Asserts.checkNotNullOrEmpty(controlInputName, "controlInputName");
        
        // load command stream info from DB
        var csEntry = db.getCommandStreamStore().getLatestVersionEntry(sysUID, controlInputName);
        if (csEntry == null)
            return null;
        
        // create new command stream handler
        return new CommandStreamTransactionHandler(csEntry.getKey(), csEntry.getValue(), this);
    }
    
    
    protected LoadingCache<String, BigId> createFoiIdCache()
    {
        return CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(new CacheLoader<String, BigId>() {
                @Override
                public BigId load(String uid)
                {
                    var fk = db.getFoiStore().getCurrentVersionKey(uid);
                    if (fk == null)
                        throw new IllegalStateException("Unknown FOI: " + uid);
                    return fk.getInternalID();
                }
            });
    }
}
