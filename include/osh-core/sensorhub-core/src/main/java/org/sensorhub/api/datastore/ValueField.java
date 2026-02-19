/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore;

import com.google.common.base.Objects;


/**
 * <p>
 * Base class for all datastore value fields.<br/>
 * Value fields can be used to select what fields should be included in value
 * objects returned by a datastore select methods.
 * </p>
 *
 * @author Alex Robin
 * @date Mar 23, 2020
 */
public class ValueField
{
    String name;
        
    
    public ValueField(String name)
    {
        this.name = name;
    }


    @Override
    public int hashCode()
    {
        return name.hashCode();
    }


    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof ValueField))
            return false;
        return Objects.equal(name, ((ValueField)obj).name);
    }


    @Override
    public String toString()
    {
        return name;
    }    
    
}
