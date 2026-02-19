/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys;

import java.io.IOException;


@SuppressWarnings("serial")
public class InvalidRequestException extends IOException
{
    
    public enum ErrorCode
    {
        UNSUPPORTED_OPERATION,
        BAD_REQUEST,
        NOT_FOUND,
        BAD_PAYLOAD,
        REQUEST_REJECTED,
        REQUEST_ACCEPTED_TIMEOUT,
        FORBIDDEN,
        INTERNAL_ERROR
    }
    
    
    ErrorCode errorCode;
    
    
    public InvalidRequestException(ErrorCode errorCode, String msg)
    {
        this(errorCode, msg, null);
    }
    
    
    public InvalidRequestException(ErrorCode errorCode, String msg, Throwable e)
    {
        super(msg, e);
        this.errorCode = errorCode;
    }
    
    
    public ErrorCode getErrorCode()
    {
        return errorCode;
    }
}
