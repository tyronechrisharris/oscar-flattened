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

/**
 * <p>
 * Event sent when a system is enabled
 * </p>
 *
 * @author Alex Robin
 * @date Mar 2, 2019
 */
public class SystemEnabledEvent extends SystemEvent
{
    String parentGroupUID;
    
    
    /**
     * @param timeStamp time of event generation (unix time in milliseconds, base 1970)
     * @param sysUID Unique ID of enabled system
     * @param parentGroupUID ID of parent system (or null if system
     * is not a member of any parent system)
     */
    public SystemEnabledEvent(long timeStamp, String sysUID, String parentGroupUID)
    {
        super(timeStamp, sysUID);
        this.parentGroupUID = parentGroupUID;
    }
    
    
    /**
     * Helper constructor that sets the timestamp to current time
     */
    @SuppressWarnings("javadoc")
    public SystemEnabledEvent(String sysUID, String parentGroupUID)
    {
        super(sysUID);
        this.parentGroupUID = parentGroupUID;
    }


    /**
     * @return Unique ID of parent system
     */
    public String getParentGroupUID()
    {
        return parentGroupUID;
    }
}
