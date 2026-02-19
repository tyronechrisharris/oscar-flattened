/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2016 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.comm;

import org.sensorhub.api.event.Event;


/**
 * <p>
 * Event sent when a device is found as a result of scanning a network or bus
 * </p>
 *
 * @author Alex Robin
 * @date Mar 2, 2019
 */
public class DeviceScanEvent extends Event
{
    public IDeviceInfo device;
    

    @Override
    public String getSourceID()
    {
        return device.getAddress();
    }
}