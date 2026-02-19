/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.

******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.system;

import org.sensorhub.api.event.IEventProducer;
import net.opengis.sensorml.v20.AbstractProcess;


/**
 * <p>
 * Base interface for all OSH system drivers that allow communication with an
 * external (observing) system and provide a SensorML description for it.
 * </p><p>
 * Complex systems with subsystems can be modeled using the {@link ISystemGroupDriver}
 * interface when such level of details is required, but if not, such systems
 * can also be modeled as a "black box" using a single instance of this class.
 * </p>
 *
 * @see ISystemWithDesc 
 * @author Alex Robin
 * @since June 9, 2017
 */
public interface ISystemDriver extends IEventProducer
{

    /**
     * @return The system's name
     */
    public String getName();
    
    
    /**
     * @return A short description of the system
     */
    public String getDescription();
    
    
    /**
     * @return The globally unique identifier of the system.
     */
    public String getUniqueIdentifier();


    /**
     * @return The unique ID of the parent system or null if this
     * system is not a member of any parent system
     */
    public String getParentSystemUID();


    /**
     * @return The parent system driver or null if the parent is not
     * a system driver defined in the same module. Note that in this case, 
     * the {@link #getParentSystemUID()} must still return a non null value.
     */
    public ISystemGroupDriver<? extends ISystemDriver> getParentSystem();


    /**
     * Retrieves the most current SensorML description of the system.
     * All implementations must return an instance of AbstractProcess with
     * a valid unique identifier.<br/>
     * In the case of a module generating data from multiple subsystems (e.g.
     * sensor network), this returns the description of the group as a whole.<br/>
     * The returned object is mutable but must NOT be mutated by the caller.
     * @return The SensorML description of the system or null if none
     * is available at the time of the call
     */
    public AbstractProcess getCurrentDescription();


    /**
     * Used to check when the SensorML description was last updated.
     * This is useful to avoid requesting the object when it hasn't changed.
     * @return Date/time of last description update as unix time (millis since 1970)
     * or {@link Long#MIN_VALUE} if description was never updated.
     */
    public long getLatestDescriptionUpdate();


    /**
     * Check if the system is enabled. A system marked as disabled
     * is not producing live data but historical data may still be available.
     * @return True if system is enabled, false otherwise
     */
    public boolean isEnabled();

}