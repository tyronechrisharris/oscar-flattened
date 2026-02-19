/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.ui.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.sensorhub.api.module.IModuleProvider;
import org.sensorhub.ui.data.BaseProperty;
import org.sensorhub.ui.data.ComplexProperty;
import org.sensorhub.ui.data.MyBeanItem;
import com.vaadin.v7.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Layout;


/**
 * <p>
 * Interface for all UI forms used to configure modules
 * </p>
 *
 * @author Alex Robin
 * @since 0.5
 */
public interface IModuleConfigForm extends ComponentContainer, Layout.MarginHandler
{
    /**
     * Special value to use as propID to identify the object at the root of the form
     */
    public static final String ROOT_PROPERTY = "_ROOT_"; 
            
            
    /**
     * Builds the whole form for a complex property (i.e. whose value is an object)
     * @param propertyId
     * @param prop
     * @param includeSubForms 
     */
    public void build(String propertyId, ComplexProperty prop, boolean includeSubForms);
    
    
    /**
     * Builds the whole form for a given config object.
     * @param title title of the form
     * @param popupText help popup text shown when hovering on the form title
     * @param beanItem object to render a form for
     * @param includeSubForms true to include sub forms in the component
     */
    public void build(String title, String popupText, MyBeanItem<Object> beanItem, boolean includeSubForms);
    
    
    /**
     * @return the parent form or null if non exists
     */
    public IModuleConfigForm getParentForm();
    
    
    public void setParentForm(IModuleConfigForm parentForm);
    
    
    /**
     * @return list of subforms included into this form
     */
    public List<Component> getSubForms();
    
    
    /**
     * Return possible module types for the given property ID
     * @param propId property ID
     * @param beanType base module type for this property
     * @return map of names to types assignable to that property
     */
    public Collection<IModuleProvider> getPossibleModuleTypes(String propId, Class<?> moduleType);
    
    
    /**
     * Return possible object types for the given property ID
     * @param propId property ID
     * @param beanType declared value type for the property
     * @return map of names to types assignable to that property
     */
    public Map<String, Class<?>> getPossibleTypes(String propId, BaseProperty<?> prop);
    
    
    /**
     * Return possible values for the given property ID
     * @param propId property ID
     * @return list of values allowed for that property
     */
    public List<Object> getPossibleValues(String propId);
    
    
    /**
     * Commit all changes made in the UI values to the underlying bean object
     * @throws CommitException if data cannot be committed
     */
    public void commit() throws CommitException;
    
}
