/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.procedure;

import org.sensorhub.api.feature.ISmlFeature;
import org.vast.ogc.om.IProcedure;
import net.opengis.sensorml.v20.AbstractProcess;


/**
 * <p>
 * Interface for (observing) procedure resources associated to a SensorML
 * description.
 * </p><p>
 * An instance of this class can be used to model different kinds of procedures,
 * and with different levels of granularity. Examples of procedures this class
 * can represent are:
 * <li>A sensor type datasheet/specsheet</li>
 * <li>A platform type datasheet/specsheet</li>
 * <li>A method that can be followed by a person</li>
 * <li>An algorithm that can be implemented by a processing instance</li>
 * </p>
 * 
 * @author Alex Robin
 * @date Oct 4, 2021
 */
public interface IProcedureWithDesc extends ISmlFeature<AbstractProcess>, IProcedure
{
    
}
