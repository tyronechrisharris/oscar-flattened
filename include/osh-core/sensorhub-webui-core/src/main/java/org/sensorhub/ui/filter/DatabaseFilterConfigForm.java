/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.ui.filter;

import java.util.function.Consumer;
import org.sensorhub.api.datastore.IQueryFilter;
import org.sensorhub.ui.GenericConfigForm;
import org.sensorhub.ui.data.ComplexProperty;
import org.sensorhub.ui.data.MyBeanItem;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Component;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.Table.Align;
import com.vaadin.v7.ui.Table.ColumnHeaderMode;
import com.vaadin.v7.ui.TableFieldFactory;
import com.vaadin.v7.ui.TextArea;
import com.vaadin.v7.ui.TreeTable;
import com.vaadin.v7.ui.AbstractTextField.TextChangeEventMode;


@SuppressWarnings({"serial", "deprecation"})
public class DatabaseFilterConfigForm extends GenericConfigForm
{
    TreeTable tree;
    FilterTree<IQueryFilter, ?> root;
    Object rootItemId;
    ComplexProperty property;
    
    
    @Override
    public void build(String propId, ComplexProperty prop, boolean includeSubForms)
    {
        super.build(propId, prop, includeSubForms);
        this.property = prop;
    }
    
    
    @Override
    public void build(String title, String popupText, MyBeanItem<Object> beanItem, boolean includeSubForms)
    {
        setSpacing(false);
        setMargin(new MarginInfo(true, false));
        
        // header
        setCaption(title);
        setDescription(popupText);
        
        this.tree = new TreeTable();
        tree.setPageLength(0);
        tree.addStyleName(STYLE_SMALL);
        tree.addContainerProperty(FilterTree.PROP_NAME, String.class, null);
        tree.addContainerProperty(FilterTree.PROP_VALUE, String.class, null);
        tree.addContainerProperty(FilterTree.PROP_CALLBACK, Consumer.class, null);
        tree.setVisibleColumns(FilterTree.PROP_NAME, FilterTree.PROP_VALUE);
        tree.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
        tree.setColumnWidth(FilterTree.PROP_NAME, 230);
        tree.setColumnWidth(FilterTree.PROP_VALUE, 400);
        tree.setColumnAlignment(FilterTree.PROP_NAME, Align.LEFT);
        
        // make some cells editable
        tree.setTableFieldFactory(new TableFieldFactory() {
            @Override
            @SuppressWarnings("unchecked")
            public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {
                
                var item = tree.getItem(itemId);
                Field<?> field = null;
                
                if (FilterTree.PROP_VALUE.equals(propertyId))
                {
                    var value = item.getItemProperty(FilterTree.PROP_VALUE).getValue();
                    var callback = (Consumer<Boolean>)item.getItemProperty(FilterTree.PROP_CALLBACK).getValue();
                    
                    if (callback != null)
                    {
                        var checkbox = new CheckBox();
                        
                        checkbox.addValueChangeListener(e -> {
                            callback.accept(checkbox.getValue());
                        });
                        
                        field = checkbox;
                    }
                    else if (value != null)
                    {
                        var textArea = new TextArea();
                        textArea.setStyleName(STYLE_SMALL);
                        textArea.setImmediate(true);
                        textArea.setRows(1);
                        textArea.setWidth(100, Unit.PERCENTAGE);
                        textArea.setTextChangeEventMode(TextChangeEventMode.EAGER);
                        
                        textArea.addTextChangeListener(e -> {
                            var txt = e.getText();
                            resizeTextField(textArea, txt);
                        });
                        
                        textArea.addValueChangeListener(e -> {
                            resizeTextField(textArea, textArea.getValue());
                        });
                        
                        field = textArea;
                    }
                }
                
                return field;
            }
        });
        tree.setEditable(true);
        
        // create root filter tree
        this.root = FilterTree.create(beanItem.getBean().getClass());
        this.rootItemId = root.renderFilterAsTree(tree, null, (IQueryFilter)beanItem.getBean());
        
        // expand all items
        for (Object item: tree.getItemIds())
            tree.setCollapsed(item, false);
        
        /*tree.addActionHandler(new Handler() {
            @Override
            public Action[] getActions(Object itemId, Object sender)
            {
                List<Action> actions = new ArrayList<>(10);
                
                if (itemId != null)
                {
                    var item = tree.getItem(itemId);
                    if (item.toString().contains("Filter"))
                        FilterTree.getActions(beanItem.getBean().getClass(), actions);
                    else
                        actions.add(REMOVE_ACTION);
                }
                
                return actions.toArray(new Action[0]);
            }
            
            @Override
            public void handleAction(final Action action, Object sender, Object target)
            {
            
            }
        });*/
        
        
        this.addComponent(tree);
    }
    
    
    void resizeTextField(TextArea txtBox, String txt)
    {
        int numLines = 1;
        for (int i = 0; i < txt.length(); i++) {
            if (txt.charAt(i) == '\n') {
                numLines++;
            }
        }
        
        txtBox.setRows(numLines);
    }
    
    
    @Override
    public void commit() throws CommitException
    {
        var f = root.buildFilterFromTree(tree, rootItemId);
        property.getValue().setBean(f);
        property.setValue(property.getValue());
    }
}
