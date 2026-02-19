/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.serialization.kryo.compat;

import static com.esotericsoftware.minlog.Log.TRACE;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;


/**
 * <p>
 * Base class to implement backward compatible deserializers by customizing
 * CacheFields created by the default FieldSerializer. Customization must be
 * done in {@link FieldSerializer#initializeCachedFields()} method.
 * </p>
 * 
 * @param <T> Type of serialized object
 * 
 * @author Alex Robin
 * @since Aug 29, 2021
 */
public abstract class BackwardCompatFieldSerializer<T> extends FieldSerializer<T>
{
    protected CachedField[] compatFields;
    
    
    @SuppressWarnings("rawtypes")
    public BackwardCompatFieldSerializer(Kryo kryo, Class type)
    {
        super(kryo, type);
    }
    
    
    public T read(Kryo kryo, Input input, Class<? extends T> type)
    {
        int pop = pushTypeVariables();

        T object = create(kryo, input, type);
        kryo.reference(object);

        CachedField[] fields = compatFields;
        for (int i = 0, n = fields.length; i < n; i++) {
            if (TRACE) log("Read", fields[i], input.position());
            try {
                fields[i].read(input, object);
            } catch (KryoException e) {
                throw e;
            } catch (OutOfMemoryError | Exception e) {
                throw new KryoException("Error reading " + fields[i] + " at position " + input.position(), e);
            }
        }

        popTypeVariables(pop);
        return object;
    }
    
    
    public void write (Kryo kryo, Output output, T object) {
        int pop = pushTypeVariables();

        CachedField[] fields = compatFields;
        for (int i = 0, n = fields.length; i < n; i++) {
            if (TRACE) log("Write", fields[i], output.position());
            try {
                fields[i].write(output, object);
            } catch (KryoException e) {
                throw e;
            } catch (OutOfMemoryError | Exception e) {
                throw new KryoException("Error writing " + fields[i] + " at position " + output.position(), e);
            }
        }

        popTypeVariables(pop);
    }
}
