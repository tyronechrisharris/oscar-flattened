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

import org.sensorhub.api.feature.ISmlFeature;
import org.vast.ogc.om.IProcedure;
import net.opengis.sensorml.v20.AbstractProcess;


/**
 * <p>
 * Interface for observing system resources associated to a SensorML description.
 * </p><p>
 * An instance of this class can be used to model different kinds of systems,
 * and with different levels of granularity. Examples of systems this class
 * can represent are:
 * <li>A hardware device (e.g. sensors, actuators)</li>
 * <li>A platform or complex system with subsystems (e.g. robot, vehicle, smartphone)</li>
 * <li>A logical system (e.g. an executable instance of a process or algorithm)</li>
 * <li>A human being or animal implementing a specific procedure</li>
 * </p><p>
 * Note that a system is an instance of a procedure implemented by the
 * IProcedure interface in OSH. For a hardware system, the procedure is typically
 * the system model as described by its datasheet. For humans, the procedure
 * is often the method that has to be carried to collect a sample and/or make
 * an observation.
 * </p>
 * 
 * @author Alex Robin
 * @date Oct 4, 2020
 */
public interface ISystemWithDesc extends ISmlFeature<AbstractProcess>, IProcedure
{
    
}
