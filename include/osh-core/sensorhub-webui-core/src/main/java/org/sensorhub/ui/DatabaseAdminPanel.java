/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.ui;

import java.util.ArrayList;
import java.util.List;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.database.IObsSystemDatabaseModule;
import org.sensorhub.api.datastore.obs.DataStreamFilter;
import org.sensorhub.api.datastore.obs.ObsFilter;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.api.module.ModuleConfig;
import org.sensorhub.impl.system.SystemDatabaseTransactionHandler;
import org.sensorhub.ui.api.IModuleAdminPanel;
import org.sensorhub.ui.data.FieldProperty;
import org.sensorhub.ui.data.MyBeanItem;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Alignment;
import com.vaadin.v7.ui.TreeTable;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


/**
 * <p>
 * Admin panel for database modules.<br/>
 * This adds a section to view storage content in a table + histograms to
 * view the distribution of data records over time
 * </p>
 *
 * @author Alex Robin
 * @since 1.0
 */
@SuppressWarnings({ "serial", "deprecation" })
public class DatabaseAdminPanel extends DefaultModulePanel<IObsSystemDatabaseModule<?>> implements IModuleAdminPanel<IObsSystemDatabaseModule<?>>
{
    private static final Action DELETE_SYSTEM_ACTION = new Action("Delete All System Data", new ThemeResource("icons/module_delete.png"));
    private static final Action DELETE_OBS_ACTION = new Action("Delete System Observations", new ThemeResource("icons/module_delete.png"));
    
    VerticalLayout layout;
    SystemSearchList systemTable;
    TabSheet dataStreamTabs;
    
    
    @Override
    public void build(final MyBeanItem<ModuleConfig> beanItem, final IObsSystemDatabaseModule<?> db)
    {
        super.build(beanItem, db);
        
        // assign default database number if not set and module hasn't been initialized yet
        if (!db.isInitialized() && db.getConfiguration().databaseNum == null)
        {
            int highestDbNum = 0;
            for (var otherDb: getParentHub().getDatabaseRegistry().getAllDatabases())
                highestDbNum = Math.max(otherDb.getDatabaseNum(), highestDbNum);
            for (var otherDb: getParentHub().getModuleRegistry().getLoadedModules(IObsSystemDatabase.class))
            {
                if (otherDb.getDatabaseNum() == null)
                    continue;
                highestDbNum = Math.max(otherDb.getDatabaseNum(), highestDbNum);
            }
            
            var nextDbNum = highestDbNum+1;
            //db.getConfiguration().databaseNum = nextDbNum;
            ((FieldProperty)beanItem.getItemProperty("databaseNum")).setValue(nextDbNum);
        }
        
        if (db != null && db.isStarted())
        {
            // section layout
            layout = new VerticalLayout();
            layout.setWidth(100.0f, Unit.PERCENTAGE);
            layout.setMargin(false);
            layout.setSpacing(true);
            
            // section title
            //layout.addComponent(new Label(""));
            HorizontalLayout titleBar = new HorizontalLayout();
            titleBar.setSpacing(true);
            Label sectionLabel = new Label("Database Content");
            sectionLabel.addStyleName(STYLE_H3);
            sectionLabel.addStyleName(STYLE_COLORED);
            titleBar.addComponent(sectionLabel);
            titleBar.setComponentAlignment(sectionLabel, Alignment.MIDDLE_LEFT);
            layout.addComponent(titleBar);
            
            systemTable = new SystemSearchList(db, event -> {
                if (event.getButton() == MouseButton.LEFT)
                {
                    try
                    {
                        // select and open module configuration
                        String sysUID = (String)event.getItem().getItemProperty(SystemSearchList.PROP_SYSTEM_UID).getValue();
                        if (sysUID != null)
                            showSystemData(db, sysUID);
                    }
                    catch (Exception e)
                    {
                        DisplayUtils.showErrorPopup("Unexpected error when selecting system", e);
                    }
                }
            });
            
            // also add context menu
            systemTable.getTable().addActionHandler(new Handler() {
                @Override
                public Action[] getActions(Object target, Object sender)
                {
                    List<Action> actions = new ArrayList<>(10);
                    actions.add(DELETE_SYSTEM_ACTION);
                    actions.add(DELETE_OBS_ACTION);
                    return actions.toArray(new Action[0]);
                }

                @Override
                public void handleAction(Action action, Object sender, Object target)
                {
                    String uid = (String)((TreeTable)sender).getValue();
                    
                    if (action == DELETE_SYSTEM_ACTION)
                    {
                        final ConfirmDialog popup = new ConfirmDialog("Are you sure you want to remove all data and metadata associated with system:<br/><b>" + uid + "?</b>");
                        popup.addCloseListener(event -> {
                            if (popup.isConfirmed())
                            {
                                try
                                {
                                    var eventBus = module.getParentHub().getEventBus();
                                    var txnHandler = new SystemDatabaseTransactionHandler(eventBus, db);
                                    txnHandler.getSystemHandler(uid).delete(true);
                                    systemTable.updateTable(db, new SystemFilter.Builder().build());
                                }
                                catch (Exception ex)
                                {
                                    getOshLogger().error("Error deleting system", ex);
                                }
                            }
                        });
                        systemTable.getUI().addWindow(popup);
                    }
                    else if (action == DELETE_OBS_ACTION)
                    {
                        final ConfirmDialog popup = new ConfirmDialog("Are you sure you want to remove all observations from system:<br/><b>" + uid + "?</b>");
                        popup.addCloseListener(event -> {
                            if (popup.isConfirmed())
                            {
                                try
                                {
                                    db.getObservationStore().removeEntries(new ObsFilter.Builder()
                                        .withDataStreams()
                                            .withSystems()
                                                .withUniqueIDs(uid)
                                                .done()
                                            .done()
                                        .build());
                                    systemTable.updateTable(db, new SystemFilter.Builder().build());
                                }
                                catch (Exception ex)
                                {
                                    getOshLogger().error("Error deleting observations", ex);
                                }
                            }
                        });
                        systemTable.getUI().addWindow(popup);
                    }
                }
            });
                        
            layout.addComponent(systemTable);
            
            Page.getCurrent().getStyles()
                .add(".datastore-table { min-height: 400px }");
            dataStreamTabs = new TabSheet();
            dataStreamTabs.addStyleName("datastore-table");
            dataStreamTabs.addSelectedTabChangeListener(e -> {
                // load data when tab is selected
                ((DatabaseStreamPanel)dataStreamTabs.getSelectedTab()).refreshContent();
            });
            
            layout.addComponent(dataStreamTabs);
            
            addComponent(layout);
        }
    }
    
    
    protected synchronized void showSystemData(final IObsSystemDatabase db, String sysUID)
    {
        // remove previous tabs
        dataStreamTabs.removeAllComponents();
        
        // show in tabs
        db.getDataStreamStore().selectEntries(new DataStreamFilter.Builder()
                .withSystems().withUniqueIDs(sysUID).done()
                .withLimit(10)
                .build())
            .forEach(dsEntry -> {
                var dsID = dsEntry.getKey().getInternalID();
                var dsInfo = dsEntry.getValue();
                var dsPanel = new DatabaseStreamPanel(db, dsInfo, dsID);
                dataStreamTabs.addTab(dsPanel, dsPanel.getCaption(), FontAwesome.DATABASE);
            });
    }
}
