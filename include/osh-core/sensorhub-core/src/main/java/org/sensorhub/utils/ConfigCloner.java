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

import java.util.Date;
import java.util.EnumSet;
import java.util.Map;
import org.sensorhub.impl.security.PermissionSetting;
import com.rits.cloning.Cloner;
import com.rits.cloning.FastClonerCustomCollection;
import com.rits.cloning.IDeepCloner;
import com.rits.cloning.IFastCloner;


public class ConfigCloner extends Cloner
{
    static class FastClonerDate implements IFastCloner
    {
        @Override
        public Object clone(Object t, IDeepCloner cloner, Map<Object, Object> clones)
        {
            return ((Date)t).clone();
        }        
    }
    
    
    static class FastClonerEnumSet implements IFastCloner
    {
        @Override
        public Object clone(Object t, IDeepCloner cloner, Map<Object, Object> clones)
        {
            return ((EnumSet<?>)t).clone();
        }        
    }
    
    
    static class FastClonerPermissionSetting extends FastClonerCustomCollection<PermissionSetting>
    {
        @Override
        public PermissionSetting getInstance(PermissionSetting o)
        {
            return new PermissionSetting();
        }        
    }
    
    
    public ConfigCloner()
    {
        super();
        
        try {
            this.registerFastCloner(Date.class, new FastClonerDate());
            this.registerFastCloner(Class.forName("java.util.RegularEnumSet"), new FastClonerEnumSet());
            this.registerFastCloner(PermissionSetting.class, new FastClonerPermissionSetting());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    
    @Override
    public <T> T deepClone(T o)
    {
        return super.deepClone(o);
    }

}
