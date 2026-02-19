/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.ui;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.ui.api.UIConstants;
import com.vaadin.ui.Component;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.event.ItemClickEvent.ItemClickListener;
import com.vaadin.v7.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings({ "serial", "deprecation" })
public class SystemSearchList extends VerticalLayout
{
    final static String PROP_SYSTEM_UID = "uid";
    final static String PROP_SYSTEM_NAME = "name";
    final static String PROP_SYSTEM_VALID_TIME = "valid";
    final static String PROP_SYSTEM_DESC = "desc";
    
    TreeTable table;
    
    
    public SystemSearchList(final IObsSystemDatabase db, final ItemClickListener selectionListener)
    {
        setMargin(false);
        
        // system uid / search box
        final var searchBox = new SearchBox("Search", "Type a UID prefix or keywords to search for systems");
        searchBox.setValue("*");
        searchBox.addToParent(this);
        searchBox.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event)
            {
                String txt = searchBox.getValue().trim();
                
                // build lists of uids and keywords
                var tokens = txt.split(" |,");
                var uids = new ArrayList<String>();
                var keywords = new ArrayList<String>();
                for (var t: tokens)
                {
                    if (txt.startsWith("urn:") || txt.equals("*"))
                        uids.add(t);
                    else if (t.length() > 0)
                        keywords.add(t);
                }
                
                var procFilter = new SystemFilter.Builder();
                if (!uids.isEmpty())
                    procFilter.withUniqueIDs(uids);
                if (!keywords.isEmpty())
                    procFilter.withKeywords(keywords);
                
                // update table
                updateTable(db, procFilter.build());
            }
        });
        
        // system table
        addComponent(buildSystemTable(db, selectionListener));
        
        // populate table with 10 first results
        updateTable(db, new SystemFilter.Builder()
            .build());
    }
    
    
    protected Component buildSystemTable(final IObsSystemDatabase db, final ItemClickListener selectionListener)
    {
        table = new TreeTable();
        table.setWidth(100, Unit.PERCENTAGE);
        table.setPageLength(5);
        table.setSelectable(true);
        table.addStyleName(UIConstants.STYLE_SMALL);
        
        // add column names
        table.addContainerProperty(PROP_SYSTEM_UID, String.class, null, "System UID", null, null);
        table.addContainerProperty(PROP_SYSTEM_NAME, String.class, null, "Name", null, null);
        table.addContainerProperty(PROP_SYSTEM_VALID_TIME, String.class, null, "Validity", null, null);
        table.addContainerProperty(PROP_SYSTEM_DESC, String.class, null, "Description", null, null);
        
        table.addItemClickListener(selectionListener);
        
        return table;
    }
    
    
    protected void updateTable(final IObsSystemDatabase db, final SystemFilter procFilter)
    {
        table.removeAllItems();
        db.getSystemDescStore().select(procFilter)
            .forEach(proc -> {
                String itemId = proc.getUniqueIdentifier();
                Item item = table.addItem(itemId);
                
                if (item != null)
                {
                    item.getItemProperty(PROP_SYSTEM_UID).setValue(proc.getUniqueIdentifier());
                    item.getItemProperty(PROP_SYSTEM_NAME).setValue(proc.getName());
                    item.getItemProperty(PROP_SYSTEM_VALID_TIME).setValue(getValidTimeString(proc));
                    item.getItemProperty(PROP_SYSTEM_DESC).setValue(proc.getDescription());
                    table.setChildrenAllowed(itemId, false);
                }
                else
                {
                    item = table.getItem(itemId);
                    table.setChildrenAllowed(itemId, true);
                    
                    // also show all historical version as children
                    String childId = proc.getUniqueIdentifier() + "_" + proc.getValidTime();
                    Item childItem = table.addItem(childId);
                    childItem.getItemProperty(PROP_SYSTEM_NAME).setValue(proc.getName());
                    childItem.getItemProperty(PROP_SYSTEM_VALID_TIME).setValue(getValidTimeString(proc));
                    childItem.getItemProperty(PROP_SYSTEM_DESC).setValue(proc.getDescription());
                    table.setParent(childId, itemId);
                    table.setChildrenAllowed(childId, false);
                }
            });
    }
    
    
    protected String getValidTimeString(ISystemWithDesc proc)
    {
        var validTime = proc.getValidTime();
        if (validTime == null)
            return "ALWAYS";
        
        return validTime.begin().truncatedTo(ChronoUnit.SECONDS) + " / " +
               (validTime.endsNow() ? "now" : validTime.end().truncatedTo(ChronoUnit.SECONDS));
    }
    
    
    public TreeTable getTable()
    {
        return table;
    }
}
