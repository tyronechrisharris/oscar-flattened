/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.system.wrapper;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.isotc211.v2005.gmd.MDLegalConstraints;
import org.vast.ogc.gml.GMLUtils;
import org.vast.sensorML.SMLHelper;
import org.vast.util.TimeExtent;
import com.google.common.collect.ImmutableList;
import net.opengis.OgcProperty;
import net.opengis.OgcPropertyList;
import net.opengis.gml.v32.AbstractGeometry;
import net.opengis.gml.v32.AbstractTimeGeometricPrimitive;
import net.opengis.gml.v32.CodeWithAuthority;
import net.opengis.gml.v32.Envelope;
import net.opengis.gml.v32.Reference;
import net.opengis.gml.v32.TimeInstant;
import net.opengis.gml.v32.TimePeriod;
import net.opengis.sensorml.v20.AbstractModes;
import net.opengis.sensorml.v20.AbstractProcess;
import net.opengis.sensorml.v20.AggregateProcess;
import net.opengis.sensorml.v20.CapabilityList;
import net.opengis.sensorml.v20.CharacteristicList;
import net.opengis.sensorml.v20.ClassifierList;
import net.opengis.sensorml.v20.ContactList;
import net.opengis.sensorml.v20.DataInterface;
import net.opengis.sensorml.v20.DocumentList;
import net.opengis.sensorml.v20.EventList;
import net.opengis.sensorml.v20.FeatureList;
import net.opengis.sensorml.v20.IOPropertyList;
import net.opengis.sensorml.v20.IdentifierList;
import net.opengis.sensorml.v20.KeywordList;
import net.opengis.sensorml.v20.ObservableProperty;
import net.opengis.sensorml.v20.PhysicalComponent;
import net.opengis.sensorml.v20.PhysicalSystem;
import net.opengis.sensorml.v20.Settings;
import net.opengis.sensorml.v20.SimpleProcess;
import net.opengis.swe.v20.AbstractSWEIdentifiable;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataStream;


/**
 * <p>
 * Read-only wrapper for SensorML descriptions, also allowing to override
 * some information such as:
 * - output list
 * - parameter list
 * - valid time period
 * </p>
 * 
 * @param <T> Type of wrapped process
 *
 * @author Alex Robin
 * @date Feb 2, 2021
 */
public class ProcessWrapper<T extends AbstractProcess> implements AbstractProcess
{
    protected static final String IMMUTABLE_ERROR = "Object is immutable";
    private static final long serialVersionUID = 6844422855935436064L;
    
    final T delegate;
    String id;
    IOPropertyList overrideOutputs;
    IOPropertyList overrideParams;
    AbstractTimeGeometricPrimitive validTime;
    
    
    protected ProcessWrapper(T delegate)
    {
        this.delegate = delegate;
    }
    
    
    @SuppressWarnings("unchecked")
    public static <T extends AbstractProcess> ProcessWrapper<T> getWrapper(T delegate)
    {        
        if (delegate instanceof PhysicalComponent)
            return (ProcessWrapper<T>)new PhysicalComponentWrapper((PhysicalComponent)delegate);
        else if (delegate instanceof PhysicalSystem)
            return (ProcessWrapper<T>)new PhysicalSystemWrapper((PhysicalSystem)delegate);
        else if (delegate instanceof SimpleProcess)
            return (ProcessWrapper<T>)new SimpleProcessWrapper((SimpleProcess)delegate);
        else if (delegate instanceof AggregateProcess)
            return (ProcessWrapper<T>)new AggregateProcessWrapper((AggregateProcess)delegate);
        
        else
            throw new IllegalArgumentException("Unsupported process type");
    }


    public ProcessWrapper<T> withId(String id)
    {
        this.id = id;
        return this;
    }


    public ProcessWrapper<T> withOutputs(IOPropertyList overrideOutputs)
    {
        this.overrideOutputs = overrideOutputs;
        return this;
    }


    public ProcessWrapper<T> withParams(IOPropertyList overrideParams)
    {
        this.overrideParams = overrideParams;
        return this;
    }


    public ProcessWrapper<T> withValidTime(TimeExtent validTime)
    {
        this.validTime = GMLUtils.timeExtentToTimePrimitive(validTime, true);
        return this;
    }
    
    
    public Envelope getBoundedBy()
    {
        return delegate.getBoundedBy();
    }


    public CapabilityList getCapabilities(String name)
    {
        return delegate.getCapabilities(name);
    }


    public OgcPropertyList<CapabilityList> getCapabilitiesList()
    {
        return delegate.getCapabilitiesList();
    }


    public CharacteristicList getCharacteristics(String name)
    {
        return delegate.getCharacteristics(name);
    }


    public OgcPropertyList<CharacteristicList> getCharacteristicsList()
    {
        return delegate.getCharacteristicsList();
    }


    public OgcPropertyList<ClassifierList> getClassificationList()
    {
        return delegate.getClassificationList();
    }


    public Settings getConfiguration()
    {
        return delegate.getConfiguration();
    }


    public OgcPropertyList<ContactList> getContactsList()
    {
        return delegate.getContactsList();
    }


    public String getDefinition()
    {
        return delegate.getDefinition();
    }


    public String getDescription()
    {
        return delegate.getDescription();
    }


    public Reference getDescriptionReference()
    {
        return delegate.getDescriptionReference();
    }


    public OgcPropertyList<DocumentList> getDocumentationList()
    {
        return delegate.getDocumentationList();
    }


    public List<Object> getExtensionList()
    {
        return delegate.getExtensionList();
    }


    public FeatureList getFeaturesOfInterest()
    {
        return delegate.getFeaturesOfInterest();
    }


    public AbstractGeometry getGeometry()
    {
        return delegate.getGeometry();
    }


    public OgcProperty<AbstractGeometry> getGeometryProperty()
    {
        return delegate.getGeometryProperty();
    }


    public OgcPropertyList<EventList> getHistoryList()
    {
        return delegate.getHistoryList();
    }


    public String getId()
    {
        if (id != null)
            return id;
        return delegate.getId();
    }


    public OgcPropertyList<IdentifierList> getIdentificationList()
    {
        return delegate.getIdentificationList();
    }


    public CodeWithAuthority getIdentifier()
    {
        return delegate.getIdentifier();
    }


    public AbstractSWEIdentifiable getInput(String name)
    {
        return delegate.getInput(name);
    }


    public DataComponent getInputComponent(String name)
    {
        return delegate.getInputComponent(name);
    }


    public IOPropertyList getInputList()
    {
        return delegate.getInputList();
    }


    public OgcPropertyList<KeywordList> getKeywordsList()
    {
        return delegate.getKeywordsList();
    }


    public String getLang()
    {
        return delegate.getLang();
    }


    public OgcPropertyList<MDLegalConstraints> getLegalConstraintsList()
    {
        return delegate.getLegalConstraintsList();
    }


    public AbstractGeometry getLocation()
    {
        return delegate.getLocation();
    }


    public OgcPropertyList<Serializable> getMetaDataPropertyList()
    {
        return delegate.getMetaDataPropertyList();
    }


    public List<AbstractModes> getModesList()
    {
        return delegate.getModesList();
    }


    public String getName()
    {
        return delegate.getName();
    }


    public List<CodeWithAuthority> getNameList()
    {
        return delegate.getNameList();
    }


    public int getNumCapabilities()
    {
        return delegate.getNumCapabilities();
    }


    public int getNumCharacteristics()
    {
        return delegate.getNumCharacteristics();
    }


    public int getNumClassifications()
    {
        return delegate.getNumClassifications();
    }


    public int getNumContacts()
    {
        return delegate.getNumContacts();
    }


    public int getNumDocumentations()
    {
        return delegate.getNumDocumentations();
    }


    public int getNumExtensions()
    {
        return delegate.getNumExtensions();
    }


    public int getNumHistorys()
    {
        return delegate.getNumHistorys();
    }


    public int getNumIdentifications()
    {
        return delegate.getNumIdentifications();
    }


    public int getNumInputs()
    {
        return delegate.getNumInputs();
    }


    public int getNumKeywords()
    {
        return delegate.getNumKeywords();
    }


    public int getNumLegalConstraints()
    {
        return delegate.getNumLegalConstraints();
    }


    public int getNumModes()
    {
        return delegate.getNumModes();
    }


    public int getNumNames()
    {
        return delegate.getNumNames();
    }


    public int getNumOutputs()
    {
        if (overrideOutputs != null)
            return overrideOutputs.size();
        return delegate.getNumOutputs();
    }


    public int getNumParameters()
    {
        if (overrideParams != null)
            return overrideParams.size();
        return delegate.getNumParameters();
    }


    public int getNumSecurityConstraints()
    {
        return delegate.getNumSecurityConstraints();
    }


    public int getNumValidTimes()
    {
        if (validTime != null)
            return 1;
        return delegate.getNumValidTimes();
    }


    public AbstractSWEIdentifiable getOutput(String name)
    {
        if (overrideOutputs != null)
            return overrideOutputs.get(name);
        return delegate.getOutput(name);
    }


    public DataComponent getOutputComponent(String name)
    {
        if (overrideOutputs != null)
            return SMLHelper.getIOComponent(getOutput(name));
        return delegate.getOutputComponent(name);
    }


    public IOPropertyList getOutputList()
    {
        if (overrideOutputs != null)
            return overrideOutputs;
        return delegate.getOutputList();
    }


    public AbstractSWEIdentifiable getParameter(String name)
    {
        if (overrideParams != null)
            return overrideParams.get(name);
        return delegate.getParameter(name);
    }


    public DataComponent getParameterComponent(String name)
    {
        if (overrideParams != null)
            return SMLHelper.getIOComponent(getParameter(name));
        return delegate.getParameterComponent(name);
    }


    public IOPropertyList getParameterList()
    {
        return delegate.getParameterList();
    }


    public Map<QName, Object> getProperties()
    {
        return delegate.getProperties();
    }


    public QName getQName()
    {
        return delegate.getQName();
    }


    public List<Object> getSecurityConstraintsList()
    {
        return delegate.getSecurityConstraintsList();
    }


    public Reference getTypeOf()
    {
        return delegate.getTypeOf();
    }


    public String getUniqueIdentifier()
    {
        return delegate.getUniqueIdentifier();
    }


    public TimeExtent getValidTime()
    {
        if (validTime != null)
            return GMLUtils.timePrimitiveToTimeExtent(validTime);
        return delegate.getValidTime();
    }


    public List<AbstractTimeGeometricPrimitive> getValidTimeList()
    {
        if (validTime != null)
            return ImmutableList.of(validTime);
        return delegate.getValidTimeList();
    }


    public boolean isSetBoundedBy()
    {
        return delegate.isSetBoundedBy();
    }


    public boolean isSetConfiguration()
    {
        return delegate.isSetConfiguration();
    }


    public boolean isSetDefinition()
    {
        return delegate.isSetDefinition();
    }


    public boolean isSetDescription()
    {
        return delegate.isSetDescription();
    }


    public boolean isSetDescriptionReference()
    {
        return delegate.isSetDescriptionReference();
    }


    public boolean isSetFeaturesOfInterest()
    {
        return delegate.isSetFeaturesOfInterest();
    }


    public boolean isSetGeometry()
    {
        return delegate.isSetGeometry();
    }


    public boolean isSetIdentifier()
    {
        return delegate.isSetIdentifier();
    }


    public boolean isSetLang()
    {
        return delegate.isSetLang();
    }


    public boolean isSetLocation()
    {
        return delegate.isSetLocation();
    }


    public boolean isSetTypeOf()
    {
        return delegate.isSetTypeOf();
    }
    
    
    public void addCapabilities(String name, CapabilityList capabilities)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }
    
    
    public void addCharacteristics(String name, CharacteristicList characteristics)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }
    
    
    public void addClassification(ClassifierList classification)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }
    
    
    public void addContacts(ContactList contacts)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addDocumentation(DocumentList documentation)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addExtension(Object extension)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addHistory(EventList history)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addIdentification(IdentifierList identification)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addInput(String name, DataComponent input)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addInput(String name, DataStream input)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addInput(String name, DataInterface input)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addInput(String name, ObservableProperty input)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addKeywords(KeywordList keywords)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addLegalConstraints(MDLegalConstraints legalConstraints)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addModes(AbstractModes modes)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addName(CodeWithAuthority name)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addOutput(String name, DataComponent output)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addOutput(String name, DataStream output)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addOutput(String name, DataInterface output)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addOutput(String name, ObservableProperty output)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addParameter(String name, DataComponent parameter)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addParameter(String name, DataStream parameter)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addParameter(String name, DataInterface parameter)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addParameter(String name, ObservableProperty parameter)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addSecurityConstraints(Object securityConstraints)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addValidTimeAsTimeInstant(TimeInstant validTime)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void addValidTimeAsTimePeriod(TimePeriod validTime)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void setBoundedByAsEnvelope(Envelope boundedBy)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void setConfiguration(Settings configuration)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void setDefinition(String definition)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void setDescription(String description)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void setDescriptionReference(Reference descriptionReference)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void setFeaturesOfInterest(FeatureList featuresOfInterest)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void setGeometry(AbstractGeometry geom)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void setId(String id)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void setIdentifier(CodeWithAuthority identifier)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void setLang(String lang)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void setLocation(AbstractGeometry geom)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void setName(String name)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void setTypeOf(Reference typeOf)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    public void setUniqueIdentifier(String identifier)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }
}
