/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2023 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.sensorhub.api.semantic.IDerivedProperty;
import org.vast.util.Asserts;
import org.vast.util.BaseBuilder;
import net.opengis.gml.v32.Reference;
import net.opengis.swe.v20.DataComponent;


public class DerivedProperty implements IDerivedProperty
{
    protected String uri;
    protected String name;
    protected String description;
    protected String basePropUri;
    protected String objectTypeUri;
    protected String statisticUri;
    protected Collection<DataComponent> qualifiers;
    protected Collection<Reference> references;
    
    
    @Override
    public String getURI()
    {
        return uri;
    }
    
    
    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public String getDescription()
    {
        return description;
    }


    @Override
    public String getBaseProperty()
    {
        return basePropUri;
    }


    @Override
    public String getObjectType()
    {
        return objectTypeUri;
    }


    @Override
    public String getStatistic()
    {
        return statisticUri;
    }


    @Override
    public Collection<DataComponent> getQualifiers()
    {
        if (qualifiers == null)
            return Collections.emptyList();
        return qualifiers;
    }


    @Override
    public Collection<Reference> getReferences()
    {
        if (references == null)
            return Collections.emptyList();
        return references;
    }
    
    
    /*
     * Builder
     */
    public static class Builder extends DerivedPropertyBuilder<Builder, DerivedProperty>
    {
        public Builder()
        {
            this.instance = new DerivedProperty();
        }
    
        public static Builder from(DerivedProperty base)
        {
            return new Builder().copyFrom(base);
        }
    }
    
    
    @SuppressWarnings("unchecked")
    public static abstract class DerivedPropertyBuilder<B extends DerivedPropertyBuilder<B, T>, T extends DerivedProperty>
        extends BaseBuilder<T>
    {
        protected DerivedPropertyBuilder()
        {
        }
    
    
        protected B copyFrom(DerivedProperty base)
        {
            instance.name = base.getName();
            instance.description = base.getDescription();
            instance.uri = base.getURI();
            instance.basePropUri = base.getBaseProperty();
            instance.objectTypeUri = base.getObjectType();
            instance.statisticUri = base.getStatistic();
            return (B)this;
        }
        
        
        public B uri(String uri)
        {
            instance.uri = uri;
            return (B)this;
        }
        
        
        public B name(String name)
        {
            instance.name = name;
            return (B)this;
        }
        
        
        public B description(String desc)
        {
            instance.description = desc;
            return (B)this;
        }
        
        
        public B baseProperty(String basePropUri)
        {
            instance.basePropUri = basePropUri;
            return (B)this;
        }
        
        
        public B objectType(String objectTypeUri)
        {
            instance.objectTypeUri = objectTypeUri;
            return (B)this;
        }
        
        
        public B statistic(String statisticUri)
        {
            instance.statisticUri = statisticUri;
            return (B)this;
        }
        
        
        public B addQualifier(DataComponent qualifier)
        {
            if (instance.qualifiers == null)
                instance.qualifiers = new ArrayList<>();
            instance.qualifiers.add(Asserts.checkNotNull(qualifier));
            return (B)this;
        }
        
        
        public B addReference(Reference ref)
        {
            if (instance.references == null)
                instance.references = new ArrayList<>();
            instance.references.add(Asserts.checkNotNull(ref));
            return (B)this;
        }
        
        
        @Override
        public T build()
        {
            Asserts.checkNotNullOrEmpty(instance.name, "name");
            Asserts.checkNotNullOrEmpty(instance.uri, "uri");
            Asserts.checkNotNullOrEmpty(instance.basePropUri, "baseProperty");
            return super.build();
        }
    }
    
}
