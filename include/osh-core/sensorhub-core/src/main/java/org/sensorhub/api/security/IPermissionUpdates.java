/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.security;

import java.util.Collection;


public interface IPermissionUpdates
{
    /**
     * Add 'allow' permissions to specified user/role
     * @param id
     * @param permissions
     */
    public void addAllowPermissions(String id, Collection<IPermissionPath> permissions);
    
    
    /**
     * Add 'deny' permissions to specified user/role
     * @param id
     * @param permissions
     */
    public void addDenyPermissions(String id, Collection<IPermissionPath> permissions);
    
    
    /**
     * Remove 'allow' permissions from specified user/role
     * @param id
     * @param permissions
     */
    public void removeAllowPermissions(String id, Collection<IPermissionPath> permissions);
    
    
    /**
     * Remove 'deny' permissions from specified user/role
     * @param id
     * @param permissions
     */
    public void removeDenyPermissions(String id, Collection<IPermissionPath> permissions);
}
