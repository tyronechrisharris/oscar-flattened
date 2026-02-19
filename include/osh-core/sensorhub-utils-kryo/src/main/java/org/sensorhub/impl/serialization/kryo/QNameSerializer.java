/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2023 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.serialization.kryo;

import javax.xml.namespace.QName;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;


public class QNameSerializer extends Serializer<QName> {

    public QNameSerializer()
    {
        setImmutable(true);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final QName qname)
    {
        var serializer = kryo.getSerializer(String.class);
        kryo.writeObjectOrNull(output, qname.getNamespaceURI(), serializer);
        kryo.writeObjectOrNull(output, qname.getLocalPart(), serializer);
        kryo.writeObjectOrNull(output, qname.getPrefix(), serializer);
    }

    @Override
    public QName read(final Kryo kryo, final Input input, final Class<? extends QName> clazz)
    {
        var serializer = kryo.getSerializer(String.class);
        var namespaceURI = kryo.readObjectOrNull(input, String.class, serializer);
        var localPart = kryo.readObjectOrNull(input, String.class, serializer);
        var prefix = kryo.readObjectOrNull(input, String.class, serializer);
        return new QName(namespaceURI, localPart, prefix);
    }
}