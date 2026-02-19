/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.system.wrapper;

import org.sensorhub.api.system.ISystemWithDesc;
import net.opengis.sensorml.v20.AbstractProcess;


/**
 * <p>
 * Wrapper class for AbstractProcess implementing {@link ISystemWithDesc}
 * and allowing to override outputs, parameters and validity time period.
 * </p>
 *
 * @author Alex Robin
 * @date Oct 4, 2020
 */
public class SystemWrapper extends SmlFeatureWrapper implements ISystemWithDesc
{
    
    public SystemWrapper(AbstractProcess fullDesc)
    {
        super(fullDesc);
    }
}
