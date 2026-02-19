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

import org.sensorhub.api.datastore.procedure.IProcedureStore;
import org.sensorhub.api.datastore.procedure.EmptyProcedureStore;
import org.sensorhub.api.datastore.property.IPropertyStore;
import org.sensorhub.api.datastore.property.EmptyPropertyStore;


/**
 * <p>
 * Common interface for procedure databases.<br/>
 * Procedure databases are used to store information about various kinds of
 * procedures, including:
 * 
 * <p>
 * - Observing procedures (e.g. sensor datasheet, description of measurement methodologies...)<br/>
 * - Actuating procedures (e.g. actuator datasheet, description of actions by humans or agents...)<br/>
 * - Sampling procedures (e.g. description of field or lab sampling methodologies...)<br/>
 * - Other procedures (e.g. platform datasheets...)<br/>
 * </p>
 * 
 * This information is provided through a separate interface because it will
 * often be implemented using a different backend (i.e. shared by different)
 * as procedure level metadata is typically much more static than information
 * related to particular system instances.
 * 
 * This kind of database is also capable of storing semantic information about
 * the properties measured or controlled by procedures.
 * </p>
 *
 * @author Alex Robin
 * @since June 22, 2023
 */
public interface IProcedureDatabase extends IDatabase
{
    
    /**
     * @return Data store containing the procedure descriptions.<br/>
     * If the database does not support persisting procedure descriptions, return
     * an instance of {@link EmptyProcedureStore}
     */
    IProcedureStore getProcedureStore();
    
    
    /**
     * @return Dataata store containing the property definitions.<br/>
     * If the database does not support persisting property definitions, return
     * an instance of {@link EmptyPropertyStore}
     */
    IPropertyStore getPropertyStore();
}
