/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.utils;

import static org.junit.Assert.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.*;
import org.junit.Test;
import org.sensorhub.api.config.DisplayInfo.ValueRange;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;


public class TestConfigValidator
{
    static class InvalidAnnotations
    {
        @Min(value = 0)
        public String val;
    }
    
    
    static class StringTest
    {
        @NotBlank
        public String val1 = "a test";
        
        @NotNull
        public String val2 = "test2";
        
        @NotEmpty
        public String val3 = "test3";
        
        @Size(min=4, max=64)
        public String val4 = "test4";
        
        @Pattern(regexp="[a-z]{4}[0-9]")
        public String val5 = "test5";
    }
    
    
    static class NumberTest
    {
        @Min(value = 10)
        public double val1 = 10.0;
        
        @Max(value = 100)
        public float val2 = 90.4f;
        
        @Min(value = 50)
        @Max(value = 100)
        public int val3 = 60;
        
        @PositiveOrZero
        public long val4 = 0;
        
        @Positive
        public int val5 = 1;
        
        @NegativeOrZero
        public short val6 = 0;
        
        @Negative
        public byte val7 = -3;
        
        @ValueRange(min=20, max=30, minExclusive=true)
        public Integer val8 = 25;
    }
    
    
    static class CollectionTest
    {
        @NotNull
        public Set<Integer> val1 = ImmutableSet.<Integer>of(2, 4);
        
        @NotEmpty
        public Map<Integer, String> val2 = ImmutableMap.<Integer, String>of(1, "test");
        
        @Size(min=2, max=4)
        public List<String> val3 = ImmutableList.of("test1", "test2", "test3");
    }
    
    
    @Test
    public void testValidString()
    {
        var bean = new StringTest();
        new ConfigValidator().validate(bean);
    }
    
    
    @Test
    public void testInvalidStringNull()
    {
        assertThrows(NullPointerException.class, () -> {
            var bean = new StringTest();
            bean.val1 = null;
            new ConfigValidator().validate(bean);
        });
        
        assertThrows(NullPointerException.class, () -> {
            var bean = new StringTest();
            bean.val2 = null;
            new ConfigValidator().validate(bean);
        });
        
        assertThrows(NullPointerException.class, () -> {
            var bean = new StringTest();
            bean.val3 = null;
            new ConfigValidator().validate(bean);
        });
    }
    
    
    @Test
    public void testInvalidStringBlank()
    {
        {
            var bean = new StringTest();
            bean.val2 = "";
            bean.val3 = " ";
            new ConfigValidator().validate(bean);
        }
        
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new StringTest();
            bean.val1 = " ";
            new ConfigValidator().validate(bean);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new StringTest();
            bean.val1 = "";
            new ConfigValidator().validate(bean);
        });
    }
    
    
    @Test
    public void testInvalidStringEmpty()
    {
        {
            var bean = new StringTest();
            bean.val2 = "";
            new ConfigValidator().validate(bean);
        }
        
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new StringTest();
            bean.val3 = "";
            new ConfigValidator().validate(bean);
        });
    }
    
    
    @Test
    public void testInvalidStringSize()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new StringTest();
            bean.val4 = "aaa";
            new ConfigValidator().validate(bean);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new StringTest();
            var sb = new StringBuffer();
            for (int i=0;i<65; i++)
                sb.append("a");
            bean.val4 = sb.toString();
            new ConfigValidator().validate(bean);
        });
    }
    
    
    @Test
    public void testInvalidStringPattern()
    {
        {
            var bean = new StringTest();
            bean.val5 = null; // null is allowed when only pattern constraint is used
            new ConfigValidator().validate(bean);
        }
        
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new StringTest();
            bean.val5 = "abcd";
            new ConfigValidator().validate(bean);
        });
    }
    
    
    @Test
    public void testValidNumber()
    {
        var bean = new NumberTest();
        new ConfigValidator().validate(bean);
    }
    
    
    @Test
    public void testInvalidNumberMinMax()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new NumberTest();
            bean.val1 = 9;
            new ConfigValidator().validate(bean);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new NumberTest();
            bean.val2 = 201.3f;
            new ConfigValidator().validate(bean);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new NumberTest();
            bean.val3 = 23;
            new ConfigValidator().validate(bean);
        });
    }
    
    
    @Test
    public void testInvalidNumberPosNeg()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new NumberTest();
            bean.val3 = 101;
            new ConfigValidator().validate(bean);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new NumberTest();
            bean.val4 = -1;
            new ConfigValidator().validate(bean);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new NumberTest();
            bean.val5 = 0;
            new ConfigValidator().validate(bean);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new NumberTest();
            bean.val6 = 1;
            new ConfigValidator().validate(bean);
        });
    }
    
    
    @Test
    public void testInvalidNumberRange()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new NumberTest();
            bean.val7 = 0;
            new ConfigValidator().validate(bean);
        });
        
        {
            var bean = new NumberTest();
            bean.val8 = null; // null is ok with boxed numbers
            new ConfigValidator().validate(bean);
        }
        
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new NumberTest();
            bean.val8 = 20;
            new ConfigValidator().validate(bean);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new NumberTest();
            bean.val8 = 31;
            new ConfigValidator().validate(bean);
        });
    }
    
    
    @Test
    public void testValidCollection()
    {
        var bean = new CollectionTest();
        new ConfigValidator().validate(bean);
    }
    
    
    @Test
    public void testInvalidCollectionNull()
    {
        assertThrows(NullPointerException.class, () -> {
            var bean = new CollectionTest();
            bean.val1 = null;
            new ConfigValidator().validate(bean);
        });
        
        assertThrows(NullPointerException.class, () -> {
            var bean = new CollectionTest();
            bean.val2 = null;
            new ConfigValidator().validate(bean);
        });
        
        assertThrows(NullPointerException.class, () -> {
            var bean = new CollectionTest();
            bean.val3 = null;
            new ConfigValidator().validate(bean);
        });
    }
    
    
    @Test
    public void testInvalidCollectionEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new CollectionTest();
            bean.val2 = Collections.emptyMap();
            new ConfigValidator().validate(bean);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new CollectionTest();
            bean.val3 = Collections.emptyList();
            new ConfigValidator().validate(bean);
        });
    }
    
    
    @Test
    public void testInvalidCollectionSize()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new CollectionTest();
            bean.val3 = ImmutableList.of("bad1");
            new ConfigValidator().validate(bean);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            var bean = new CollectionTest();
            bean.val3 = ImmutableList.of("bad1", "bad1", "bad1", "bad1", "bad1");
            new ConfigValidator().validate(bean);
        });
    }

}
