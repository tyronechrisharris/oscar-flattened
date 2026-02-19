/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.h2;

import org.vast.util.Asserts;
import org.vast.util.BaseBuilder;


/**
 * <p>
 * Data structure for storing data store information
 * </p>
 *
 * @author Alex Robin
 * @date Apr 5, 2018
 */
public class MVDataStoreInfo
{
    protected String name;
    

    protected MVDataStoreInfo()
    {        
    }
    
    
    public String getName()
    {
        return name;
    }
    
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T extends Builder> T builder()
    {
        return (T)new Builder(new MVDataStoreInfo());
    }
    
    
    @SuppressWarnings("unchecked")
    public static class Builder<B extends Builder<B, T>, T extends MVDataStoreInfo> extends BaseBuilder<T>
    {
        protected Builder(T instance)
        {
            super(instance);
        }


        public B withName(String name)
        {
            instance.name = name;
            return (B)this;
        }
        
        
        public T build()
        {
            Asserts.checkNotNull(instance.name, "name");
            return super.build();
        }
    }
}
