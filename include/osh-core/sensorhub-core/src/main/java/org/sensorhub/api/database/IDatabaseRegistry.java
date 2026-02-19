/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.

******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.database;

import java.util.Collection;
import org.sensorhub.api.system.ISystemDriverDatabase;


/**
 * <p>
 * Interface for the main database registry on a sensor hub.
 * </p><p>
 * This database registry keeps track of which database contains data for each
 * system registered on the hub.
 * </p><p>
 * It also exposes a federated database which provides read-only access
 * to all historical observations available from this hub, along with the
 * corresponding systems, datastreams, command channels and features of interest
 * metadata. The federated database aggregates data from all databases registered
 * with this registry.
 * </p><p>
 * With a minimum setup (i.e. no database configured), the federated
 * database gives access to the latest state of all registered systems
 * (e.g. latest system description, latest observations, latest observed
 * FOI) but no historical data will be available.
 * </p>
 *
 * @author Alex Robin
 * @date Sep 18, 2019
 */
public interface IDatabaseRegistry
{

    /**
     * Register a database to be exposed through the federated database.
     * <br/><br/>
     * If the database is a {@link ISystemDriverDatabase} a mapping is
     * registered with the system UIDs obtained by calling the
     * {@link ISystemDriverDatabase#getHandledSystems()}
     * method 
     * @param db
     */
    void register(IDatabase db);
    
    
    /**
     * Unregister a database and all its mappings
     * @param db
     */
    void unregister(IDatabase db);
    
    
    /**
     * @return The list of all databases registered on the hub (read-only)
     */
    Collection<IDatabase> getAllDatabases();
    
    
    /**
     * Provides direct (potentially read/write) access to the obs system
     * database with the specified number
     * @param dbNum Database number (unique on a given hub instance)
     * @return The database instance or null if none has been assigned
     * the specified number
     */
    IObsSystemDatabase getObsDatabaseByNum(int dbNum);

    
    /**
     * Provides direct (potentially read/write) access to the database with
     * the specified module ID
     * @param moduleID Id of the database module
     * @return The database instance
     * @throws IllegalArgumentException if no database module with the given
     * ID exists
     */
    IObsSystemDatabase getObsDatabaseByModuleID(String moduleID);
    
    
    /**
     * @return The list of all obs system databases registered on the hub (read-only)
     */
    Collection<IObsSystemDatabase> getObsSystemDatabases();
    
    
    /**
     * @return The list of all procedure databases registered on the hub (read-only)
     */
    Collection<IProcedureDatabase> getProcedureDatabases();
    
    
    /**
     * Provides direct (potentially read/write) access to the procedure
     * database with the specified number
     * @param dbNum Database number (unique on a given hub instance)
     * @return The database instance or null if none has been assigned
     * the specified number
     */
    public IProcedureDatabase getProcedureDatabaseByNum(int dbNum);
    
    
    /**
     * @return The list of all feature databases registered on the hub (read-only)
     */
    Collection<IFeatureDatabase> getFeatureDatabases();
    
    
    /**
     * Provides direct (potentially read/write) access to the feature
     * database with the specified number
     * @param dbNum Database number (unique on a given hub instance)
     * @return The database instance or null if none has been assigned
     * the specified number
     */
    public IFeatureDatabase getFeatureDatabaseByNum(int dbNum);


    /**
     * @return This hub's federated observation database.<br/>
     * See class description for more information about the federated DB
     */
    IFederatedDatabase getFederatedDatabase();
    
}
