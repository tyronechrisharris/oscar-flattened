/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.system;

import java.util.Collection;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.database.IProcedureDatabase;


/**
 * <p>
 * Extension to {@link IObsSystemDatabase} interface allowing a given database
 * instance to register itself with the system driver registry so it can 
 * automatically receive and persist all data related to a given list of
 * systems drivers, identified by their UID.
 * </p>
 *
 * @author Alex Robin
 * @date Nov 16, 2020
 */
public interface ISystemDriverDatabase extends IObsSystemDatabase, IProcedureDatabase
{
    
    /**
     * @return UIDs of systems handled by this database
     */
    Collection<String> getHandledSystems();
    
}
