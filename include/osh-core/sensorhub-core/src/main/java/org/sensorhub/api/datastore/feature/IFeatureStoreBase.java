/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore.feature;

import java.time.Instant;
import java.util.stream.Stream;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.DataStoreException;
import org.sensorhub.api.datastore.TemporalFilter;
import org.sensorhub.api.datastore.feature.FeatureFilterBase.FeatureFilterBaseBuilder;
import org.sensorhub.api.datastore.feature.IFeatureStoreBase.FeatureField;
import org.sensorhub.api.datastore.resource.IResourceStore;
import org.vast.ogc.gml.IFeature;
import org.vast.util.Bbox;


/**
 * <p>
 * Base interface for data stores containing objects derived from IFeature.
 * </p>
 * 
 * @param <V> Feature type
 * @param <VF> Feature field enum type
 * @param <F> Feature filter type
 *
 * @author Alex Robin
 * @date Mar 19, 2018
 */
public interface IFeatureStoreBase<V extends IFeature, VF extends FeatureField, F extends FeatureFilterBase<? super V>> extends IResourceStore<FeatureKey, V, VF, F>
{
    
    public static class FeatureField extends ResourceField
    {
        public static final FeatureField TYPE = new FeatureField("type");
        public static final FeatureField UID = new FeatureField("UID");
        public static final FeatureField GEOMETRY = new FeatureField("geometry");
        public static final FeatureField VALID_TIME = new FeatureField("validTime");
        public static final FeatureField PROPERTIES = new FeatureField("properties");
        
        public FeatureField(String name)
        {
            super(name);
        }
    }
    
    
    @Override
    public FeatureFilterBaseBuilder<?,?,F> filterBuilder();
    
    
    /**
     * Add a new feature to the store, generating a new key for it.<br/>
     * If a feature with the same UID already exists, a new version of the
     * feature whose validity starts at the specified valid time is created
     * and it shares the same internal ID with the previous version.
     * @param value New feature object
     * @return The newly allocated key (internal ID)
     * @throws DataStoreException if a feature with the same UID and
     * valid time already exists
     */
    default FeatureKey add(V value) throws DataStoreException
    {
        return add(BigId.NONE, value);
    }
    
    
    /**
     * Add a new feature to the store, generating a new key for it, and
     * also add it as a child of the specified parent
     * @param parentID Internal ID of parent feature
     * @param value New feature object
     * @return The newly allocated key (internal ID)
     * @throws DataStoreException if a feature with the same UID and
     * valid time already exists, or if the parent ID is unknown
     */
    FeatureKey add(BigId parentID, V value) throws DataStoreException;


    /**
     * Checks if store contains a feature with the given internal ID
     * @param internalID The feature internal ID
     * @return True if a feature with the given ID exists, false otherwise
     */
    public default boolean contains(BigId internalID)
    {
        return selectKeys(filterBuilder()
                .withInternalIDs(internalID)
                .build())
            .findFirst()
            .isPresent();
    }


    /**
     * Checks if store contains a feature with the given unique ID
     * @param uid The feature unique ID
     * @return True if a feature with the given ID exists, false otherwise
     */
    public default boolean contains(String uid)
    {
        return selectKeys(filterBuilder()
                .withUniqueIDs(uid)
                .build())
            .findFirst()
            .isPresent();
    }
    
    
    /**
     * Get a feature's parent ID
     * @param internalID Internal ID of feature
     * @return Internal ID of parent feature or null if no feature with
     * the given ID was found or the feature has no parent
     */
    public BigId getParent(BigId internalID);
    
    
    /**
     * Helper method to retrieve the entry corresponding to the current version
     * of the feature with the given unique ID
     * @param uid The feature unique ID
     * @return The feature entry or null if no feature with the given ID was found
     */
    public default Entry<FeatureKey, V> getCurrentVersionEntry(String uid)
    {
        /*return selectEntries(filterBuilder()
                .withUniqueIDs(uid)
                .withCurrentVersion()
                .build())
            .findFirst()
            .orElse(null);*/
        
        return getLatestEntry(selectEntries(filterBuilder()
            .withUniqueIDs(uid)
            .build()));
    }
    
    
    /**
     * Helper method to retrieve the entry corresponding to the current version
     * of the feature with the given internal ID
     * @param internalID The feature internal ID
     * @return The feature entry or null if no feature with the given ID was found
     */
    public default Entry<FeatureKey, V> getCurrentVersionEntry(BigId internalID)
    {
        /*return selectEntries(filterBuilder()
                .withInternalIDs(internalID)
                .withCurrentVersion()
                .build())
            .findFirst()
            .orElse(null);*/
        
        return getLatestEntry(selectEntries(filterBuilder()
            .withInternalIDs(internalID)
            .build()));
    }
    
    
    public default Entry<FeatureKey, V> getLatestEntry(Stream<Entry<FeatureKey, V>> featureVersions)
    {
        var now = Instant.now();
        var it = featureVersions.iterator();
        
        Entry<FeatureKey, V> latestEntry = null;
        while (it.hasNext())
        {
            latestEntry = it.next();
            var validStart = latestEntry.getKey().getValidStartTime();
            if (validStart.isAfter(now))
                break;
        }
        
        return latestEntry;
    }
    
    
    /**
     * Helper method to retrieve the full key corresponding to the current version
     * of the feature with the given unique ID
     * @param uid The feature unique ID
     * @return The feature key or null if no feature with the given ID was found
     */
    public default FeatureKey getCurrentVersionKey(String uid)
    {
        var e = getCurrentVersionEntry(uid);
        return e != null ? e.getKey() : null;
    }
    
    
    /**
     * Helper method to retrieve the full key corresponding to the current version
     * of the feature with the given internal ID
     * @param internalID The feature internal ID
     * @return The feature key or null if no feature with the given ID was found
     */
    public default FeatureKey getCurrentVersionKey(BigId internalID)
    {
        var e = getCurrentVersionEntry(internalID);
        return e != null ? e.getKey() : null;
    }
    
    
    /**
     * Helper method to retrieve the current version of the feature with the given
     * unique ID
     * @param uid The feature unique ID
     * @return The feature representation or null if no feature with the
     * given ID was found
     */
    public default V getCurrentVersion(String uid)
    {
        var e = getCurrentVersionEntry(uid);
        return e != null ? e.getValue() : null;
    }
    
    
    /**
     * Helper method to retrieve the current version of the feature with the given
     * internal ID
     * @param internalID The feature internal ID
     * @return The feature representation or null if no feature with the
     * given ID was found
     */
    public default V getCurrentVersion(BigId internalID)
    {
        var e = getCurrentVersionEntry(internalID);
        return e != null ? e.getValue() : null;
    }
    
    
    /**
     * Helper method to remove all versions of the feature with the given UID
     * @param uid The feature unique ID
     * @return The feature key of the current version or null if nothing was removed
     */
    public default FeatureKey remove(String uid)
    {
        var fk = getCurrentVersionKey(uid);
        if (fk == null)
            return null;
        
        long count = removeEntries(filterBuilder()
            .withUniqueIDs(uid)
            .build());
        
        return count > 0 ? fk : null;
    }
       
    
    /**
     * @return Total number of distinct features contained in this data store.<br/>
     * Note that this is different from {@link #getNumRecords()} because the
     * later will count one entry for each version of the same feature while this
     * method will count the feature only once.
     */
    public default long getNumFeatures()
    {
        return countMatchingEntries(filterBuilder()
            .withValidTime(new TemporalFilter.Builder()
                .withLatestTime()
                .build())
            .build());
    }


    /**
     * @return Overall bounding rectangle of all features contained in this
     * data store
     */
    public Bbox getFeaturesBbox();
    
    
    /**
     * Gets a set of more precise bounding rectangles for regions where features
     * contained in this data store are located
     * @param filter Spatial filter to limit the search
     * @return Stream of bounding boxes
     *
    public Stream<Bbox> getFeatureClusters(SpatialFilter filter);*/
    
}
