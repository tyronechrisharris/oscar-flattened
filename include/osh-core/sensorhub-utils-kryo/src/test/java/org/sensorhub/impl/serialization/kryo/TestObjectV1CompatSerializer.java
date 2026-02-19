/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.serialization.kryo;

import java.awt.geom.Point2D;
import org.sensorhub.impl.serialization.kryo.compat.BackwardCompatFieldSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;


public class TestObjectV1CompatSerializer extends BackwardCompatFieldSerializer<TestObjectV2>
{
        
    public TestObjectV1CompatSerializer(Kryo kryo)
    {
        super(kryo, TestObjectV2.class);
    }
    
    
    protected void initializeCachedFields()
    {
        CachedField[] fields = getFields();
        
        // modify fields that have changed since v1
        compatFields = new CachedField[fields.length];
        compatFields[0] = fields[0];
        compatFields[1] = fields[1];
        compatFields[2] = fields[2];
        
        compatFields[3] = new CachedField(null) {
            @Override
            public void write(Output output, Object object)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public void read(Input input, Object object)
            {
                double[] oldField = (double[])getKryo().readObject(input, double[].class);
                var x = (float)oldField[0];
                var y = (float)oldField[1];
                ((TestObjectV2)object).att4_new_type = new Point2D.Float(x, y);
            }

            @Override
            public void copy(Object original, Object copy)
            {
                throw new UnsupportedOperationException();
            }
        };
        
        compatFields[4] = new CachedField(null) {
            @Override
            public void write(Output output, Object object)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public void read(Input input, Object object)
            {
                ((TestObjectV2)object).att5_new = "default_val";
            }

            @Override
            public void copy(Object original, Object copy)
            {
                throw new UnsupportedOperationException();
            }
        };
        
    }

}
