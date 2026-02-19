/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2017 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.sensor;


/**
 * <p>
 * Interface for actuator drivers combining data and control interfaces.
 * </p><p>
 * <i>Note: this is just a semantic interface for systems that are 
 * considered actuators. Actuators have the same functionality as sensors
 * because, in addition to receiving commands, they often have outputs that
 * provide some measured internal values or status information.</i> 
 * </p>
 *
 * @author Alex Robin
 * @since Jun 11, 2017
 */
public interface IActuatorDriver extends ISensorDriver
{

}
