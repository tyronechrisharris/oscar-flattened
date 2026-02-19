/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.utils;


/**
 * <p>
 * Generic runtime exception that can be thrown by callbacks or lambda functions
 * that don't allow for checked exceptions (e.g. stream API, map compute API...)
 * It is often used to wrap a checked exception. 
 * </p>
 *
 * @author Alex Robin
 * @date Dec 7, 2020
 */
@SuppressWarnings("serial")
public class CallbackException extends RuntimeException
{
    
    public CallbackException(String msg)
    {
        super(msg);
    }
    
    
    public CallbackException(Throwable cause)
    {
        super(cause);
    }
    
    
    public CallbackException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
