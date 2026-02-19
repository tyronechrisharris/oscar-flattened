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

import static org.junit.Assert.*;
import org.junit.Test;


public class TestMapWithWildcards
{

    @Test
    public void testPutWildcardAndGet()
    {
        var map = new MapWithWildcards<Integer>();
        map.put("prefix:*", 0);
        
        assertEquals(0, (int)map.get("prefix:001"));
        assertEquals(0, (int)map.get("prefix:00ezfefafaefae1"));
        assertEquals(0, (int)map.get("prefix:" + "\1"));
        assertEquals(0, (int)map.get("prefix:" + "\0"));
        assertEquals(0, (int)map.get("prefix:001*"));
        
        assertNull(map.get("prefixbad:001"));
        assertNull(map.get("prefix':001"));
        assertNull(map.get("prefix*"));
        assertNull(map.get("prefix:"));
    }
    

    @Test
    public void testPutWildcardAndExactAndGet()
    {
        var map = new MapWithWildcards<Integer>();
        map.put("prefix1:*", 0);
        map.put("prefix--2*", 1);
        map.put("nowildcard:001", 2);
        
        assertEquals(0, (int)map.get("prefix1:001"));
        assertEquals(0, (int)map.get("prefix1:00ezfefafaefae1"));
        assertEquals(0, (int)map.get("prefix1:" + "\1"));
        
        assertEquals(1, (int)map.get("prefix--2-001"));
        assertEquals(1, (int)map.get("prefix--2:00ezfefafaefae1"));
        assertEquals(1, (int)map.get("prefix--2" + "\0"));
        assertNull(map.get("prefix--2"));
        assertNull(map.get("prefix--++"));
        
        assertEquals(2, (int)map.get("nowildcard:001"));
        assertNull(map.get("nowildcard:001++"));
        
    }

}
