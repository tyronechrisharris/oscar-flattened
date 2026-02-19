/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.json;

import java.io.IOException;
import java.io.StringWriter;
import com.google.gson.stream.JsonWriter;


public class DelegatingJsonWriter extends JsonWriter
{
    JsonWriter jsonWriter;


    public DelegatingJsonWriter(JsonWriter jsonWriter)
    {
        super(new StringWriter());
        this.jsonWriter = jsonWriter;
    }


    public boolean isLenient()
    {
        return jsonWriter.isLenient();
    }


    public JsonWriter beginArray() throws IOException
    {
        return jsonWriter.beginArray();
    }


    public JsonWriter endArray() throws IOException
    {
        return jsonWriter.endArray();
    }


    public JsonWriter beginObject() throws IOException
    {
        return jsonWriter.beginObject();
    }


    public JsonWriter endObject() throws IOException
    {
        return jsonWriter.endObject();
    }


    public JsonWriter value(String value) throws IOException
    {
        return jsonWriter.value(value);
    }


    public JsonWriter value(long value) throws IOException
    {
        return jsonWriter.value(value);
    }


    public void close() throws IOException
    {
        jsonWriter.close();
    }


    public boolean equals(Object obj)
    {
        return jsonWriter.equals(obj);
    }


    public JsonWriter name(String name) throws IOException
    {
        return jsonWriter.name(name);
    }


    public JsonWriter nullValue() throws IOException
    {
        return jsonWriter.nullValue();
    }


    public JsonWriter value(Boolean value) throws IOException
    {
        return jsonWriter.value(value);
    }


    public JsonWriter value(double value) throws IOException
    {
        return jsonWriter.value(value);
    }


    public void flush() throws IOException
    {
        jsonWriter.flush();
    }


    public int hashCode()
    {
        return jsonWriter.hashCode();
    }


    public JsonWriter jsonValue(String value) throws IOException
    {
        return jsonWriter.jsonValue(value);
    }


    public String toString()
    {
        return jsonWriter.toString();
    }


    public JsonWriter value(boolean value) throws IOException
    {
        return jsonWriter.value(value);
    }


    public JsonWriter value(Number value) throws IOException
    {
        return jsonWriter.value(value);
    }
}
