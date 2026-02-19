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
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import org.sensorhub.impl.service.consys.resource.PropertyFilter;
import org.vast.json.JsonInliningWriter;
import com.google.gson.stream.JsonWriter;


/**
 * <p>
 * Extension to Gson stream writer to allow skipping some elements while
 * writing out JSON.
 * </p>
 *
 * @author Alex Robin
 * @since Nov 6, 2020
 */
public class FilteredJsonWriter extends JsonInliningWriter
{
    private static final String ARRAY_MARKER = "{array}";
    
    Set<String> excludedProps;
    Set<String> includedProps;
    Deque<String> currentPath = new ArrayDeque<>();
    String skippedName;
    
    
    public FilteredJsonWriter(Writer out, PropertyFilter propFilter)
    {
        super(out);
        this.excludedProps = propFilter.getExcludedProps();
        this.includedProps = propFilter.getIncludedProps();
    }
    

    @Override
    public JsonWriter name(String name) throws IOException
    {
        currentPath.push(name);
        
        if (skippedName == null)
        {
            if (name.equals("items"))
                return super.name(name);
                
            if (excludedProps.contains(name) ||
                (!includedProps.isEmpty() && !includedProps.contains(name)))// && currentPath.size() == 1))
                skippedName = name;
            else
                super.name(name);
        }
        
        return this;
    }
    
    
    protected boolean isSkipped()
    {
        return skippedName != null;
    }
    
    
    protected void popAndCheckSkipEnded()
    {
        if (!currentPath.isEmpty() && currentPath.peek() != ARRAY_MARKER)
        {
            var name = currentPath.pop();
            if (skippedName == name)
                skippedName = null;
        }
    }


    @Override
    public JsonWriter beginArray() throws IOException
    {
        if (!currentPath.isEmpty())
            currentPath.push(ARRAY_MARKER);
        
        if (!isSkipped())
            super.beginArray();
        
        return this;
    }


    @Override
    public JsonWriter endArray() throws IOException
    {
        if (!isSkipped())
            super.endArray();
        
        if (!currentPath.isEmpty())
            currentPath.pop(); // pop array marker
        
        popAndCheckSkipEnded();
        return this;
    }


    @Override
    public JsonWriter beginObject() throws IOException
    {
        if (!isSkipped())
            super.beginObject();
        return this;
    }


    @Override
    public JsonWriter endObject() throws IOException
    {
        if (!isSkipped())
            super.endObject();
        popAndCheckSkipEnded();
        return this;
    }


    @Override
    public JsonWriter value(String value) throws IOException
    {
        if (!isSkipped())
            super.value(value);
        popAndCheckSkipEnded();
        return this;
    }


    @Override
    public JsonWriter jsonValue(String value) throws IOException
    {
        if (!isSkipped())
            super.jsonValue(value);
        popAndCheckSkipEnded();
        return this;
    }


    @Override
    public JsonWriter nullValue() throws IOException
    {
        if (!isSkipped())
            super.nullValue();
        popAndCheckSkipEnded();
        return this;
    }


    @Override
    public JsonWriter value(boolean value) throws IOException
    {
        if (!isSkipped())
            super.value(value);
        popAndCheckSkipEnded();
        return this;
    }


    @Override
    public JsonWriter value(Boolean value) throws IOException
    {
        if (!isSkipped())
            super.value(value);
        popAndCheckSkipEnded();
        return this;
    }


    @Override
    public JsonWriter value(double value) throws IOException
    {
        if (!isSkipped())
            super.value(value);
        popAndCheckSkipEnded();
        return this;
    }


    @Override
    public JsonWriter value(long value) throws IOException
    {
        if (!isSkipped())
            super.value(value);
        popAndCheckSkipEnded();
        return this;
    }


    @Override
    public JsonWriter value(Number value) throws IOException
    {
        if (!isSkipped())
            super.value(value);
        popAndCheckSkipEnded();
        return this;
    }
}
