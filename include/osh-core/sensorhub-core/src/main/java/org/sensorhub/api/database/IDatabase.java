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

import java.util.concurrent.Callable;


/**
 * <p>
 * Base interface for all database implementations.<br/>
 * A database contains one or more data stores.
 * </p>
 *
 * @author Alex Robin
 * @date Oct 19, 2019
 */
public interface IDatabase
{
    
    /**
     * @return The number assigned to this database, or null if none is set.
     * Database instances attached to the same hub must have different numbers.
     */
    Integer getDatabaseNum();
    
    
    /**
     * Execute a transaction guaranteeing that all changes are applied
     * consistently or reverted in case of error.
     * @param transaction Callable running all creation/deletion/update commands.
     * @return The result of the transaction
     * @throws Exception Any exception thrown by the callable itself
     */
    <T> T executeTransaction(Callable<T> transaction) throws Exception;
    
    
    /**
     * Commit changes to the database.<br/>
     * Note that this is equivalent to calling commit on each data store separately
     */
    void commit();
    
    
    /**
     * @return True if the database is open, false otherwise
     */
    boolean isOpen();
    
    
    /**
     * @return True if the database is open as read-only, false otherwise
     */
    boolean isReadOnly();    
    
}
