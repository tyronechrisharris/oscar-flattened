/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.serialization.kryo.compat.v1;

import java.lang.reflect.Field;
import java.math.BigInteger;
import org.sensorhub.api.common.BigId;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer.CachedField;


/**
 * <p>
 * Internal class used to configure Kryo to serialize BigId fields as longs
 * </p>
 *
 * @author Alex Robin
 * @since May 3, 2022
 */
public class BigIdAsBigIntCachedField extends CachedField
{
    Kryo kryo;
    int idScope;
    
    
    public BigIdAsBigIntCachedField(Field field, Kryo kryo, int idScope)
    {
        super(field);
        this.kryo = kryo;
        this.idScope = idScope;
    }
    

    @Override
    public void write(Output output, Object object)
    {
        try {
            var id = (BigId)getField().get(object);
            kryo.writeClassAndObject(output, id != null ? new BigInteger(id.getIdAsBytes()) : null);
        }
        catch (Throwable t) {
            KryoException ex = new KryoException(t);
            ex.addTrace(getName() + " (BigId)");
            throw ex;
        }
    }
    

    @Override
    public void read(Input input, Object object)
    {
        try {
            var id = (BigInteger)kryo.readClassAndObject(input);
            if (id != null)
                getField().set(object, id == BigInteger.ZERO ? BigId.NONE : BigId.fromBytes(idScope, id.toByteArray()));
        }
        catch (Throwable t) {
            KryoException ex = new KryoException(t);
            ex.addTrace(getName() + " (BigId)");
            throw ex;
        }
    }
    

    @Override
    public void copy(Object original, Object copy)
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public String getName()
    {
        return getField().getName();
    }
    
    
    @Override
    public String toString ()
    {
        return getName();
    }
}
