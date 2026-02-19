/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore;

import java.time.Instant;
import java.util.stream.Stream;
import org.sensorhub.api.command.CommandStreamInfo;
import org.sensorhub.api.command.ICommandStreamInfo;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.data.DataStreamInfo;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.api.datastore.DataStoreException;
import org.sensorhub.api.datastore.IdProvider;
import org.sensorhub.api.datastore.command.CommandStreamFilter;
import org.sensorhub.api.datastore.command.CommandStreamKey;
import org.sensorhub.api.datastore.command.ICommandStore;
import org.sensorhub.api.datastore.command.ICommandStreamStore;
import org.sensorhub.api.datastore.feature.FeatureFilterBase;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.api.datastore.feature.IFeatureStoreBase;
import org.sensorhub.api.datastore.obs.DataStreamFilter;
import org.sensorhub.api.datastore.obs.DataStreamKey;
import org.sensorhub.api.datastore.obs.IDataStreamStore;
import org.sensorhub.api.datastore.procedure.IProcedureStore;
import org.sensorhub.api.datastore.procedure.ProcedureFilter;
import org.sensorhub.api.datastore.property.PropertyKey;
import org.sensorhub.api.datastore.system.ISystemDescStore;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.api.semantic.IConceptDef;
import org.sensorhub.api.semantic.IDerivedProperty;
import org.sensorhub.api.utils.OshAsserts;
import org.sensorhub.impl.datastore.command.EmptyCommandStore;
import org.vast.ogc.gml.IFeature;
import org.vast.ogc.om.IProcedure;
import org.vast.util.Asserts;
import org.vast.util.TimeExtent;
import com.google.common.hash.Hashing;


/**
 * <p>
 * Helper methods to implement datastores
 * </p>
 *
 * @author Alex Robin
 * @date Nov 10, 2020
 */
public class DataStoreUtils
{
    public static final String ERROR_INVALID_KEY = "Key must be an instance of " + BigId.class.getSimpleName();
    public static final String ERROR_EXISTING_RESOURCE = "Datastore already contains entry with the same UID: ";
    
    public static final String ERROR_INVALID_DATASTREAM_KEY = "Key must be an instance of " + DataStreamKey.class.getSimpleName();
    public static final String ERROR_EXISTING_DATASTREAM = "Datastore already contains datastream for the same system, output and validTime";
    public static final String ERROR_INVALID_COMMANDSTREAM_KEY = "Key must be an instance of " + CommandStreamKey.class.getSimpleName();
    public static final String ERROR_EXISTING_COMMANDSTREAM = "Datastore already contains command stream for the same system, control input and validTime";
    
    public static final String ERROR_INVALID_FEATURE_KEY = "Key must be an instance of " + FeatureKey.class.getSimpleName();
    public static final String ERROR_EXISTING_FEATURE = "Datastore already contains entry with the same UID: ";
    public static final String ERROR_EXISTING_FEATURE_VERSION = "Datastore already contains entry with the same UID and validTime";
    public static final String ERROR_CHANGED_FEATURE_UID = "Feature UID cannot be changed";
    public static final String ERROR_UNKNOWN_PARENT_FEATURE = "Unknown parent feature: ";
    
    public static final String ERROR_EXISTING_KEY = "Key already exists";
    public static final String ERROR_INVALID_PROPERTY_KEY = "Key must be an instance of " + PropertyKey.class.getSimpleName();
    public static final String ERROR_EXISTING_PROPERTY = "Datastore already contains concept with the same URI: ";
    
    
    public static final ICommandStore EMPTY_COMMAND_STORE = new EmptyCommandStore();
    
    
    public static long checkInternalID(long internalID)
    {
        Asserts.checkArgument(internalID > 0, "ID must be > 0");
        return internalID;
    }
    
    
    public static BigId checkInternalID(BigId internalID)
    {
        return OshAsserts.checkValidInternalID(internalID, "internalID");
    }
    
    
    public static BigId checkBigIdKey(Object key)
    {
        Asserts.checkNotNull(key, BigId.class);
        Asserts.checkArgument(key instanceof BigId, ERROR_INVALID_KEY);
        return (BigId)key;
    }
    
    
    //////////////////////////////////////
    // Helper methods for feature stores
    //////////////////////////////////////
    
    public static FeatureKey checkFeatureKey(Object key)
    {
        Asserts.checkNotNull(key, FeatureKey.class);
        Asserts.checkArgument(key instanceof FeatureKey, ERROR_INVALID_FEATURE_KEY);
        return (FeatureKey)key;
    }
    
    public static String checkFeatureObject(IFeature f)
    {
        return OshAsserts.checkFeatureObject(f);
    }
    
    public static String checkSystemObject(IProcedure f)
    {
        return OshAsserts.checkSystemObject(f);
    }
    
    public static String checkUniqueID(String uid)
    {
        return OshAsserts.checkValidUID(uid);
    }
    
    public static void checkParentFeatureExists(IFeatureStoreBase<?,?,?> dataStore, BigId parentID) throws DataStoreException
    {
        OshAsserts.checkValidInternalID(parentID);
        if (parentID != null && parentID != BigId.NONE && !dataStore.contains(parentID))
            throw new DataStoreException(DataStoreUtils.ERROR_UNKNOWN_PARENT_FEATURE + parentID);
    }
    
    public static void checkParentFeatureExists(BigId parentID, IFeatureStoreBase<?,?,?>... dataStores) throws DataStoreException
    {
        OshAsserts.checkValidInternalID(parentID);
        if (parentID != null && parentID != BigId.NONE)
        {
            for (var dataStore: dataStores)
            {
                if (dataStore.contains(parentID))
                    return;
            }
            
            throw new DataStoreException(DataStoreUtils.ERROR_UNKNOWN_PARENT_FEATURE + parentID);
        }   
    }
    
    
    //////////////////////////////////////
    // Helper methods for property stores
    //////////////////////////////////////
    
    public static PropertyKey checkPropertyKey(Object key)
    {
        Asserts.checkNotNull(key, PropertyKey.class);
        Asserts.checkArgument(key instanceof PropertyKey, ERROR_INVALID_PROPERTY_KEY);
        return (PropertyKey)key;
    }
    
    
    public static void checkPropertyDef(IDerivedProperty prop) throws DataStoreException
    {
        Asserts.checkNotNull(prop, IDerivedProperty.class);
        Asserts.checkNotNullOrBlank(prop.getURI(), "URI");
    }
    
    
    ////////////////////////////////////////////////
    // Helpers methods for datastream stream stores
    ////////////////////////////////////////////////
    
    public static DataStreamKey checkDataStreamKey(Object key)
    {
        Asserts.checkNotNull(key, DataStreamKey.class);
        Asserts.checkArgument(key instanceof DataStreamKey, ERROR_INVALID_DATASTREAM_KEY);
        return (DataStreamKey)key;
    }
    
    
    public static void checkDataStreamInfo(ISystemDescStore systemStore, IDataStreamInfo dsInfo) throws DataStoreException
    {
        Asserts.checkNotNull(dsInfo, IDataStreamInfo.class);
        Asserts.checkNotNull(dsInfo.getSystemID(), "systemID");
        Asserts.checkNotNull(dsInfo.getOutputName(), "outputName");
        checkParentSystemExists(systemStore, dsInfo);
    }
    
    
    public static void checkParentSystemExists(ISystemDescStore systemStore, IDataStreamInfo dsInfo) throws DataStoreException
    {
        var sysID = dsInfo.getSystemID().getInternalID();
        if (systemStore != null && systemStore.getCurrentVersionKey(sysID) == null)
            throw new DataStoreException("Unknown parent system: " + sysID);
    }
    
    
    public static IDataStreamInfo ensureValidTime(ISystemDescStore systemStore, IDataStreamInfo dsInfo)
    {
        // use valid time of parent system or current time if none was set
        if (dsInfo.getValidTime() == null)
        {
            TimeExtent validTime = null;
            
            if (systemStore != null)
            {
                var fk = systemStore.getCurrentVersionKey(dsInfo.getSystemID().getInternalID());
                if (fk.getValidStartTime() != Instant.MIN)
                    validTime = TimeExtent.endNow(fk.getValidStartTime());
            }
            
            if (validTime == null)
                validTime = TimeExtent.endNow(Instant.now());
            
            // create new datastream obj with proper valid time
            return DataStreamInfo.Builder.from(dsInfo)
                .withValidTime(validTime)
                .build();
        }
        
        return dsInfo;
    }
    
    
    /////////////////////////////////////////////
    // Helpers methods for command stream stores
    /////////////////////////////////////////////
    
    public static CommandStreamKey checkCommandStreamKey(Object key)
    {
        Asserts.checkNotNull(key, CommandStreamKey.class);
        Asserts.checkArgument(key instanceof CommandStreamKey, ERROR_INVALID_COMMANDSTREAM_KEY);
        return (CommandStreamKey)key;
    }
    
    
    public static void checkCommandStreamInfo(ISystemDescStore systemStore, ICommandStreamInfo csInfo) throws DataStoreException
    {
        Asserts.checkNotNull(csInfo, ICommandStreamInfo.class);
        Asserts.checkNotNull(csInfo.getSystemID(), "systemID");
        Asserts.checkNotNull(csInfo.getControlInputName(), "controlInputName");
        checkParentSystemExists(systemStore, csInfo);
    }
    
    
    public static void checkParentSystemExists(ISystemDescStore systemStore, ICommandStreamInfo csInfo) throws DataStoreException
    {
        var sysID = csInfo.getSystemID().getInternalID();
        if (systemStore != null && systemStore.getCurrentVersionKey(sysID) == null)
            throw new DataStoreException("Unknown parent system: " + sysID);
    }
    
    
    public static ICommandStreamInfo ensureValidTime(ISystemDescStore systemStore, ICommandStreamInfo csInfo)
    {
        // use valid time of parent system or current time if none was set
        if (csInfo.getValidTime() == null)
        {
            TimeExtent validTime = null;
            
            if (systemStore != null)
            {
                var fk = systemStore.getCurrentVersionKey(csInfo.getSystemID().getInternalID());
                if (fk.getValidStartTime() != Instant.MIN)
                    validTime = TimeExtent.endNow(fk.getValidStartTime());
            }
            
            if (validTime == null)
                validTime = TimeExtent.endNow(Instant.now());
            
            // create new datastream obj with proper valid time
            return CommandStreamInfo.Builder.from(csInfo)
                .withValidTime(validTime)
                .build();
        }
        
        return csInfo;
    }
    
    
    //////////////////////////////////////////
    // Helpers methods for JOIN operations
    //////////////////////////////////////////
        
    public static <V extends IFeature, F extends FeatureFilterBase<? super V>> Stream<BigId> selectFeatureIDs(IFeatureStoreBase<V,?,F> featureStore, F filter)
    {
        if (filter.getInternalIDs() != null && !filter.includeMembers())
        {
            // if only internal IDs were specified, no need to search the linked datastore
            return filter.getInternalIDs().stream();
        }
        else
        {
            Asserts.checkState(featureStore != null, "No linked feature store");
            
            // otherwise get all feature keys matching the filter from linked datastore
            // we apply the distinct operation to make sure the same feature is not
            // listed twice (it can happen when there exists several versions of the 
            // same feature with different valid times)
            return featureStore.selectKeys(filter)
                .map(k -> k.getInternalID())
                .distinct();
        }
    }
    
    
    public static Stream<BigId> selectSystemIDs(ISystemDescStore systemStore, SystemFilter filter)
    {
        if (filter.getInternalIDs() != null && !filter.includeMembers())
        {
            // if only internal IDs were specified, no need to search the linked datastore
            return filter.getInternalIDs().stream();
        }
        else
        {
            Asserts.checkState(systemStore != null, "No linked system store");
            
            // otherwise get all systems matching the filter from linked datastore
            // we apply the distinct operation to make sure the same system is not
            // listed twice (it can happen when there exists several versions of the
            // same system description with different valid times)
            return systemStore.selectKeys(filter)
                .map(k -> k.getInternalID())
                .distinct();
        }
    }
    
    
    public static Stream<String> selectSystemUIDs(ISystemDescStore systemStore, SystemFilter filter)
    {
        if (filter.getUniqueIDs() != null)
        {
            // if only internal unique IDs were specified, no need to search the linked datastore
            return filter.getUniqueIDs().stream();
        }
        else
        {
            Asserts.checkState(systemStore != null, "No linked system store");
            
            // otherwise get all systems matching the filter from linked datastore
            // we apply the distinct operation to make sure the same system is not
            // listed twice (it can happen when there exists several versions of the
            // same system description with different valid times)
            return systemStore.select(filter)
                .map(sys -> sys.getUniqueIdentifier())
                .distinct();
        }
    }
    
    
    public static Stream<BigId> selectProcedureIDs(IProcedureStore procStore, ProcedureFilter filter)
    {
        if (filter.getInternalIDs() != null)
        {
            // if only internal IDs were specified, no need to search the linked datastore
            return filter.getInternalIDs().stream();
        }
        else
        {
            Asserts.checkState(procStore != null, "No linked procedure store");
            
            // otherwise get all procedures matching the filter from linked datastore
            // we apply the distinct operation to make sure the same system is not
            // listed twice (it can happen when there exists several versions of the
            // same system description with different valid times)
            return procStore.selectKeys(filter)
                .map(k -> k.getInternalID())
                .distinct();
        }
    }
    
    
    public static Stream<String> selectProcedureUIDs(IProcedureStore procStore, ProcedureFilter filter)
    {
        if (filter.getUniqueIDs() != null)
        {
            // if only internal unique IDs were specified, no need to search the linked datastore
            return filter.getUniqueIDs().stream();
        }
        else
        {
            Asserts.checkState(procStore != null, "No linked procedure store");
            
            // otherwise get all procedures matching the filter from linked datastore
            // we apply the distinct operation to make sure the same procedure is not
            // listed twice (it can happen when there exists several versions of the
            // same procedure description with different valid times)
            return procStore.select(filter)
                .map(proc -> proc.getUniqueIdentifier())
                .distinct();
        }
    }
    
    
    public static Stream<BigId> selectDataStreamIDs(IDataStreamStore dataStreamStore, DataStreamFilter filter)
    {
        if (filter.getInternalIDs() != null)
        {
            // if only internal IDs were specified, no need to search the linked datastore
            return filter.getInternalIDs().stream();
        }
        else
        {
            // otherwise get all datastream keys matching the filter from linked datastore
            Asserts.checkState(dataStreamStore != null, "No linked datastream store");
            return dataStreamStore.selectKeys(filter)
                .map(k -> k.getInternalID());
        }
    }
    
    
    public static Stream<IDataStreamInfo> selectDataStreams(IDataStreamStore dataStreamStore, DataStreamFilter filter)
    {
        Asserts.checkState(dataStreamStore != null, "No linked datastream store");
        return dataStreamStore.select(filter);
    }
    
    
    public static Stream<BigId> selectCommandStreamIDs(ICommandStreamStore cmdStreamStore, CommandStreamFilter filter)
    {
        if (filter.getInternalIDs() != null)
        {
            // if only internal IDs were specified, no need to search the linked datastore
            return filter.getInternalIDs().stream();
        }
        else
        {
            Asserts.checkState(cmdStreamStore != null, "No linked command stream store");
            
            // otherwise get all keys matching the filter from linked datastore
            return cmdStreamStore.selectKeys(filter)
                .map(k -> k.getInternalID());
        }
    }
    
    
    public static Stream<ICommandStreamInfo> selectCommandStreams(ICommandStreamStore cmdStreamStore, CommandStreamFilter filter)
    {
        Asserts.checkState(cmdStreamStore != null, "No linked command stream store");
        return cmdStreamStore.select(filter);
    }
    
    
    public static <T extends IFeature> IdProvider<T> getFeatureHashIdProvider(int seed)
    {
        var hashFunc = Hashing.murmur3_128(seed);
        
        return f -> {
            // compute hash
            var hash = hashFunc.hashUnencodedChars(f.getUniqueIdentifier());
            
            // We keep only 42-bits so it can fit on a 8-bytes DES encrypted block,
            // along with the ID scope and using variable length encoding.
            return hash.asLong() & 0x3FFFFFFFFFFL;
        };
    }
    
    
    public static <T extends IConceptDef> IdProvider<T> getConceptHashIdProvider(int seed)
    {
        var hashFunc = Hashing.murmur3_128(seed);
        
        return o -> {
            // compute hash
            var hash = hashFunc.hashUnencodedChars(o.getURI());
            
            // We keep only 42-bits so it can fit on a 8-bytes DES encrypted block,
            // along with the ID scope and using variable length encoding.
            return hash.asLong() & 0x3FFFFFFFFFFL;
        };
    }
    
    
    public static IdProvider<IDataStreamInfo> getDataStreamHashIdProvider(int seed)
    {
        var hashFunc = Hashing.murmur3_128(seed);
        
        return dsInfo -> {
            // compute hash
            var hash = hashFunc.newHasher()
                .putUnencodedChars(dsInfo.getSystemID().getUniqueID())
                .putUnencodedChars(dsInfo.getOutputName())
                .putInt(dsInfo.getValidTime().hashCode())
                .hash();
            
            // We keep only 42-bits so it can fit on a 8-bytes DES encrypted block,
            // along with the ID scope and using variable length encoding.
            return hash.asLong() & 0x3FFFFFFFFFFL;
        };
    }
    
    
    public static IdProvider<ICommandStreamInfo> getCommandStreamHashIdProvider(int seed)
    {
        var hashFunc = Hashing.murmur3_128(seed);
        
        return dsInfo -> {
            // compute hash
            var hash = hashFunc.newHasher()
                .putUnencodedChars(dsInfo.getSystemID().getUniqueID())
                .putUnencodedChars(dsInfo.getControlInputName())
                .putInt(dsInfo.getValidTime().hashCode())
                .hash();
            
            // We keep only 42-bits so it can fit on a 8-bytes DES encrypted block,
            // along with the ID scope and using variable length encoding.
            return hash.asLong() & 0x3FFFFFFFFFFL;
        };
    }
}
