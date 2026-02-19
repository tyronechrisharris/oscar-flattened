/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.resource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.sensorhub.api.common.IdEncoders;
import org.vast.xml.XMLImplFinder;


/**
 * <p>
 * Base class for all XML resource formatters
 * </p>
 * 
 * @param <K> Resource Key
 * @param <V> Resource Object
 *
 * @author Alex Robin
 * @since Jan 26, 2021
 */
public abstract class ResourceBindingXml<K, V> extends ResourceBinding<K, V>
{
    public static final String INVALID_XML_ERROR_MSG = "Invalid XML: ";
    public static final String MISSING_PROP_ERROR_MSG = "Missing property: ";
    
    protected final XMLStreamReader xmlReader;
    protected final XMLStreamWriter xmlWriter;
    
    
    protected ResourceBindingXml(RequestContext ctx, IdEncoders idEncoders, boolean forReading) throws IOException
    {
        super(ctx, idEncoders);
        
        try
        {
            if (forReading)
            {
                var factory = XMLImplFinder.getStaxInputFactory();
                var is = new BufferedInputStream(ctx.getInputStream());
                xmlReader = factory.createXMLStreamReader(is, StandardCharsets.UTF_8.name());
                xmlWriter = null;
            }
            else
            {
                var factory = XMLImplFinder.getStaxOutputFactory();
                var os = ctx.getOutputStream();//new BufferedOutputStream(ctx.getOutputStream());
                xmlWriter = factory.createXMLStreamWriter(os, StandardCharsets.UTF_8.name());
                xmlReader = null;
            }
        }
        catch (XMLStreamException e)
        {
            throw new IOException("Error initializing XML bindings", e);
        }
    }
}