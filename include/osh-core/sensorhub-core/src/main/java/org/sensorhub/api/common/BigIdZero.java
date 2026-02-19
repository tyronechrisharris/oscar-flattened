/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.common;

import java.util.Arrays;


/**
 * <p>
 * BigId implementation backed by a single long value
 * </p>
 *
 * @author Alex Robin
 * @since Apr 14, 2022
 */
public class BigIdZero implements BigId
{
    static byte[] ZERO = new byte[] {0};
    
    
    @Override
    public int getScope()
    {
        return 0;
    }


    @Override
    public byte[] getIdAsBytes()
    {
        return ZERO;
    }


    @Override
    public long getIdAsLong()
    {
        return 0L;
    }


    @Override
    public int size()
    {
        return 1;
    }


    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof BigId))
            return false;
        
        return ((BigId)obj).getScope() == 0 &&
               Arrays.equals(((BigId)obj).getIdAsBytes(), getIdAsBytes());
    }


    @Override
    public int hashCode()
    {
        return 0;
    }


    @Override
    public String toString()
    {
        return "BigId {None}";
    }
}