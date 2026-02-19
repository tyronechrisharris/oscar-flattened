/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.h2;

import java.nio.ByteBuffer;
import org.h2.mvstore.DataUtils;
import org.h2.mvstore.WriteBuffer;


/**
 * <p>
 * Write buffer that doesn't grow as needed like a normal WriteBuffer.
 * This is used to write keys to BigId byte[] more efficiently when needed
 * size is known in advance.
 * </p>
 *
 * @author Alex Robin
 * @since Apr 18, 2022
 */
public class FixedSizeWriteBuffer extends WriteBuffer
{

    FixedSizeWriteBuffer(int size)
    {
        super(size);
    }
    
    @Override
    public WriteBuffer putVarInt(int x) {
        DataUtils.writeVarInt(getBuffer(), x);
        return this;
    }

    @Override
    public WriteBuffer putVarLong(long x) {
        DataUtils.writeVarLong(getBuffer(), x);
        return this;
    }

    @Override
    public WriteBuffer putStringData(String s, int len) {
        DataUtils.writeStringData(getBuffer(), s, len);
        return this;
    }

    @Override
    public WriteBuffer put(byte x) {
        getBuffer().put(x);
        return this;
    }

    @Override
    public WriteBuffer putChar(char x) {
        getBuffer().putChar(x);
        return this;
    }

    @Override
    public WriteBuffer putShort(short x) {
        getBuffer().putShort(x);
        return this;
    }

    @Override
    public WriteBuffer putInt(int x) {
        getBuffer().putInt(x);
        return this;
    }

    @Override
    public WriteBuffer putLong(long x) {
        getBuffer().putLong(x);
        return this;
    }

    @Override
    public WriteBuffer putFloat(float x) {
        getBuffer().putFloat(x);
        return this;
    }

    @Override
    public WriteBuffer putDouble(double x) {
        getBuffer().putDouble(x);
        return this;
    }

    @Override
    public WriteBuffer put(byte[] bytes) {
        getBuffer().put(bytes);
        return this;
    }

    @Override
    public WriteBuffer put(byte[] bytes, int offset, int length) {
        getBuffer().put(bytes, offset, length);
        return this;
    }

    @Override
    public WriteBuffer put(ByteBuffer src) {
        getBuffer().put(src);
        return this;
    }
}
