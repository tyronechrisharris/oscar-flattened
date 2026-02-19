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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import javax.validation.constraints.*;
import org.sensorhub.api.config.DisplayInfo.Required;
import org.sensorhub.api.config.DisplayInfo.ValueRange;
import org.vast.util.Asserts;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;


/**
 * <p>
 * Helper class to validate OSH config classes based on javax.validation
 * annotations and our custom annotations
 * </p>
 *
 * @author Alex Robin
 * @since Sep 22, 2021
 */
public class ConfigValidator
{
    Map<Class<?>, FieldValidator> validators;
    
    
    public interface FieldValidator
    {
        public void validate(Object bean, Field f, Annotation constraint);
    }
    
    
    public ConfigValidator()
    {
        // use default validators
        validators = ImmutableMap.<Class<?>, FieldValidator>builder()
            .put(Min.class, MIN_VALIDATOR)
            .put(Max.class, MAX_VALIDATOR)
            .put(ValueRange.class, RANGE_VALIDATOR)
            .put(Positive.class, POSITIVE_VALIDATOR)
            .put(PositiveOrZero.class, POSITIVEORZERO_VALIDATOR)
            .put(Negative.class, NEGATIVE_VALIDATOR)
            .put(NegativeOrZero.class, NEGATIVEORZERO_VALIDATOR)
            .put(NotNull.class, NOTNULL_VALIDATOR)
            .put(Required.class, NOTNULL_VALIDATOR)
            .put(NotEmpty.class, NOTEMPTY_VALIDATOR)
            .put(Size.class, SIZE_VALIDATOR)
            .put(NotBlank.class, NOTBLANK_VALIDATOR)
            .put(Pattern.class, REGEX_VALIDATOR)
            .build();
    }
    
    
    public void validate(Object bean)
    {
        Asserts.checkNotNull(bean, Object.class);
        
        // validate all public fields with javax.validation annotations by reflection
        Field[] fields = bean.getClass().getFields();
        for (Field f : fields)
        {
            if (!Modifier.isStatic(f.getModifiers()))
            {
                for (var annot: f.getAnnotations())
                {
                    var annotType = annot.annotationType();
                    var validator = validators.get(annotType);
                    if (validator != null)
                        validator.validate(bean, f, annot);
                }
            }
        }
    }
    
    
    private static void checkLongValue(Object bean, Field f, Range<Long> range) throws IllegalAccessException
    {
        var type = f.getType();
        long val;
        
        // first read field value as long
        if (Number.class.isAssignableFrom(f.getType()))
        {
            if (f.get(bean) == null) // null boxed value is ok
                return;
            val = ((Number)f.get(bean)).longValue();
        }
        else if (type == long.class || type == int.class || type == short.class || type == byte.class)
        {
            val = f.getLong(bean);
        }
        else if (type == double.class || type == float.class)
        {
            val = (long)f.getDouble(bean);
        }
        else
            throw new IllegalAccessException();
        
        // check value against range
        Asserts.checkValueInRange(val, range, f.getName());
    }
    
    
    public static FieldValidator MIN_VALIDATOR = new MinAnnotationValidator();
    public static class MinAnnotationValidator implements FieldValidator
    {
        @Override
        public void validate(Object bean, Field f, Annotation constraint)
        {
            try {
                var range = Range.atLeast(((Min)constraint).value());
                checkLongValue(bean, f, range);
            }
            catch (IllegalAccessException e) {
                throw new IllegalStateException("Min constraint can only be used on a numeric field");
            }
        }
    };
    
    
    public static FieldValidator MAX_VALIDATOR = new MaxAnnotationValidator();
    public static class MaxAnnotationValidator implements FieldValidator
    {
        @Override
        public void validate(Object bean, Field f, Annotation constraint)
        {
            try {
                var range = Range.atMost(((Max)constraint).value());
                checkLongValue(bean, f, range);
            }
            catch (IllegalAccessException e) {
                throw new IllegalStateException("Max constraint can only be used on a numeric field");
            }
        }
    };
    
    
    public static FieldValidator RANGE_VALIDATOR = new ValueRangeAnnotationValidator();
    public static class ValueRangeAnnotationValidator implements FieldValidator
    {
        @Override
        public void validate(Object bean, Field f, Annotation constraint)
        {
            try {
                var r = (ValueRange)constraint;
                var range = r.minExclusive() && r.maxExclusive() ? Range.open(r.min(), r.max()) :
                    r.minExclusive() ? Range.openClosed(r.min(), r.max()) :
                    r.maxExclusive() ? Range.closedOpen(r.min(), r.max()) :
                    Range.closed(r.min(), r.max());
                checkLongValue(bean, f, range);
            }
            catch (IllegalAccessException e) {
                throw new IllegalStateException("ValueRange constraint can only be used on a numeric field");
            }
        }
    };
    
    
    public static FieldValidator POSITIVE_VALIDATOR = new PostiveAnnotationValidator();
    public static class PostiveAnnotationValidator implements FieldValidator
    {
        @Override
        public void validate(Object bean, Field f, Annotation constraint)
        {
            try {
                var range = Range.greaterThan(0L);
                checkLongValue(bean, f, range);
            }
            catch (IllegalAccessException e) {
                throw new IllegalStateException("Positive constraint can only be used on a numeric field");
            }
        }
    };
    
    
    public static FieldValidator POSITIVEORZERO_VALIDATOR = new PostiveOrZeroAnnotationValidator();
    public static class PostiveOrZeroAnnotationValidator implements FieldValidator
    {
        @Override
        public void validate(Object bean, Field f, Annotation constraint)
        {
            try {
                var range = Range.atLeast(0L);
                checkLongValue(bean, f, range);
            }
            catch (IllegalAccessException e) {
                throw new IllegalStateException("PositiveOrZero constraint can only be used on a numeric field");
            }
        }
    };
    
    
    public static FieldValidator NEGATIVE_VALIDATOR = new NegativeAnnotationValidator();
    public static class NegativeAnnotationValidator implements FieldValidator
    {
        @Override
        public void validate(Object bean, Field f, Annotation constraint)
        {
            try {
                var range = Range.lessThan(0L);
                checkLongValue(bean, f, range);
            }
            catch (IllegalAccessException e) {
                throw new IllegalStateException("Negative constraint can only be used on a numeric field");
            }
        }
    };
    
    
    public static FieldValidator NEGATIVEORZERO_VALIDATOR = new NegativeOrZeroAnnotationValidator();
    public static class NegativeOrZeroAnnotationValidator implements FieldValidator
    {
        @Override
        public void validate(Object bean, Field f, Annotation constraint)
        {
            try {
                var range = Range.atMost(0L);
                checkLongValue(bean, f, range);
            }
            catch (IllegalAccessException e) {
                throw new IllegalStateException("NegativeOrZero constraint can only be used on a numeric field");
            }
        }
    };
    
    
    public static FieldValidator NOTNULL_VALIDATOR = new NotNullAnnotationValidator();
    public static class NotNullAnnotationValidator implements FieldValidator
    {
        @Override
        public void validate(Object bean, Field f, Annotation constraint)
        {
            try {
                Asserts.checkNotNull(f.get(bean), f.getName());
            }
            catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    };
    
    
    public static FieldValidator NOTEMPTY_VALIDATOR = new NotEmptyAnnotationValidator();
    public static class NotEmptyAnnotationValidator implements FieldValidator
    {
        @Override
        public void validate(Object bean, Field f, Annotation constraint)
        {
            try {
                var fieldVal = f.get(bean);
                if (CharSequence.class.isAssignableFrom(f.getType()))
                    Asserts.checkNotNullOrEmpty((CharSequence)fieldVal, f.getName());
                else if (Collection.class.isAssignableFrom(f.getType()))
                    Asserts.checkNotNullOrEmpty((Collection<?>)fieldVal, f.getName());
                else if (Map.class.isAssignableFrom(f.getType()))
                    Asserts.checkNotNullOrEmpty(((Map<?,?>)fieldVal).keySet(), f.getName());
                else if (f.getType().isArray())
                    Asserts.checkNotNullOrEmpty(Arrays.asList(fieldVal), f.getName());
                else
                    throw new IllegalStateException("NotEmpty constraint can only be used on a field of type String, Collection, Array or Map");
            }
            catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }
    
    
    public static FieldValidator SIZE_VALIDATOR = new SizeAnnotationValidator();
    public static class SizeAnnotationValidator implements FieldValidator
    {
        @Override
        public void validate(Object bean, Field f, Annotation constraint)
        {
            try {
                var fieldVal = f.get(bean);
                Size size = (Size)constraint;
                
                if (CharSequence.class.isAssignableFrom(f.getType()))
                    Asserts.checkCharLength((CharSequence)fieldVal, f.getName(), size.min(), size.max());
                else if (Collection.class.isAssignableFrom(f.getType()))
                    Asserts.checkCollectionSize((Collection<?>)fieldVal, f.getName(), size.min(), size.max());
                else if (Map.class.isAssignableFrom(f.getType()))
                    Asserts.checkCollectionSize(((Map<?,?>)fieldVal).keySet(), f.getName(), size.min(), size.max());
                else if (f.getType().isArray())
                    Asserts.checkCollectionSize(Arrays.asList(fieldVal), f.getName(), size.min(), size.max());
                else
                    throw new IllegalStateException("Size constraint can only be used on a field of type String, Collection, Array or Map");
            }
            catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }
    
    
    public static FieldValidator NOTBLANK_VALIDATOR = new NotBlankAnnotationValidator();
    public static class NotBlankAnnotationValidator implements FieldValidator
    {
        @Override
        public void validate(Object bean, Field f, Annotation constraint)
        {
            try {
                var fieldVal = f.get(bean);
                if (f.getType() == String.class)
                    Asserts.checkNotNullOrBlank((String)fieldVal, f.getName());
                else
                    throw new IllegalStateException("NotBlank constraint can only be used on a field of type String");
            }
            catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }
    
    
    public static FieldValidator REGEX_VALIDATOR = new RegexAnnotationValidator();
    public static class RegexAnnotationValidator implements FieldValidator
    {
        @Override
        public void validate(Object bean, Field f, Annotation constraint)
        {
            try {
                var fieldVal = f.get(bean);
                if (CharSequence.class.isAssignableFrom(f.getType())) {
                    if (fieldVal == null) // null is ok
                        return;
                    var pattern = java.util.regex.Pattern.compile(((Pattern)constraint).regexp());
                    if (!pattern.matcher((CharSequence)fieldVal).matches())
                        throw new IllegalArgumentException(f.getName() + " does not match validation pattern");
                }
                else
                    throw new IllegalStateException("Pattern constraint can only be used on a field of type String");
            }
            catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
