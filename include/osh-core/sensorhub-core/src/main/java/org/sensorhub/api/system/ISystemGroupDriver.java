/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

Copyright (C) 2012-2017 Sensia Software LLC. All Rights Reserved.

******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.system;

import java.util.Map;


/**
 * <p>
 * Interface for systems that are themselves groups of subsystems
 * (e.g. sensor networks, platforms carrying other systems, etc.).
 * </p>
 *
 * @author Alex Robin
 * @param <T> Type of subsystem composing this group
 * @since Jun 9, 2017
 */
public interface ISystemGroupDriver<T extends ISystemDriver> extends ISystemDriver
{

    /**
     * @return Map of UID to subsystems
     */
    public Map<String, ? extends T> getMembers();
}
