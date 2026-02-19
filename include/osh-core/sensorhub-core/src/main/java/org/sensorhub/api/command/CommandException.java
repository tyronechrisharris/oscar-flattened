/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.command;

import org.sensorhub.api.common.SensorHubException;


/**
 * <p>
 * Exceptions generated when a command fails
 * </p>
 *
 * @author Alex Robin
 * @since Mar 11, 2021
 */
public class CommandException extends SensorHubException
{
    private static final long serialVersionUID = 3168784295650597795L;


    public CommandException(String message)
    {
        super(message);
    }
    
    
    public CommandException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    
    public CommandException(String message, int code, Throwable cause)
    {
        super(message, code, cause);
    }    
}
