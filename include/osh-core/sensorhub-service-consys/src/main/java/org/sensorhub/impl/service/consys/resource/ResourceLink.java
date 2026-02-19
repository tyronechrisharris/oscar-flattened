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

import org.vast.util.Asserts;
import org.vast.util.BaseBuilder;


public class ResourceLink
{
    public final static String REL_SELF = "self";
    public final static String REL_PREV = "prev";
    public final static String REL_NEXT = "next";
    public final static String REL_ITEMS = "items";
    public final static String REL_COLLECTION = "collection";
    
    String rel;
    String title;
    String href;
    String type;


    public String getRel()
    {
        return rel;
    }


    public String getTitle()
    {
        return title;
    }


    public String getHref()
    {
        return href;
    }
    

    public String getType()
    {
        return type;
    }
    
    
    public ResourceLink withFormat(String formatName, ResourceFormat format)
    {
        var newLink = new ResourceLink();
        newLink.rel = this.rel;
        newLink.title = this.title + " in " + formatName + " format";
        newLink.href = this.href + (this.href.contains("?") ? "&" : "?") + "f=" + format.getShortName();
        newLink.type = format.getMimeType();
        return newLink;
    }
    
    
    public static ResourceLink self(String url, String mimeType)
    {
        return new ResourceLink.Builder()
            .rel(REL_SELF)
            .type(mimeType)
            .title("This document")
            .href(url)
            .build();
    }
    
    
    public static Builder builder()
    {
        return new Builder();
    }
    
    
    public static class Builder extends BaseBuilder<ResourceLink>
    {
        ResourceFormat format;
        
        public Builder()
        {
            this.instance = new ResourceLink();
        }
        
        public Builder copyFrom(ResourceLink other)
        {
            instance.rel = other.rel;
            instance.href = other.href;
            instance.type = other.type;
            instance.title = other.title;
            return this;
        }
        
        public Builder rel(String rel)
        {
            instance.rel = rel;
            return this;
        }
        
        public Builder title(String title)
        {
            instance.title = title;
            return this;
        }
        
        public Builder href(String href)
        {
            instance.href = href;
            return this;
        }
        
        public Builder type(String type)
        {
            instance.type = type;
            return this;
        }
        
        public Builder withFormat(ResourceFormat format)
        {
            this.format = Asserts.checkNotNull(format, ResourceFormat.class);
            return this;
        }
        
        @Override
        public ResourceLink build()
        {
            if (format != null && instance.href != null)
            {
                var hasQuery = instance.href.contains("?");
                instance.href = instance.href + (hasQuery ? "&" : "?") + "f=" + format.getShortName();
                instance.type = format.getMimeType();
            }
            return super.build();
        }
    }
}
