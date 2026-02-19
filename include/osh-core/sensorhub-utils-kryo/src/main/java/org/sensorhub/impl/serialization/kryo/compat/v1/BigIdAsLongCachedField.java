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
import org.sensorhub.api.common.BigId;
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
public class BigIdAsLongCachedField extends CachedField
{
    int idScope;
    
    
    public BigIdAsLongCachedField(Field field, int idScope)
    {
        super(field);
        this.idScope = idScope;
    }
    

    @Override
    public void write(Output output, Object object)
    {
        try {
            var id = (BigId)getField().get(object);
            output.writeVarLong(id.getIdAsLong(), false);
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
            var id = input.readVarLong(false);
            getField().set(object, id <= 0 ? BigId.NONE : BigId.fromLong(idScope, id));
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
