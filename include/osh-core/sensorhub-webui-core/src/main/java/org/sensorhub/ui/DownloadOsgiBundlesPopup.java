/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.sensorhub.ui.api.UIConstants;
import org.vast.util.Asserts;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.v7.data.Container.Filterable;
import com.vaadin.v7.data.util.filter.SimpleStringFilter;
import com.vaadin.v7.event.FieldEvents.TextChangeEvent;
import com.vaadin.v7.event.FieldEvents.TextChangeListener;
import com.vaadin.v7.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


/**
 * <p>
 * Popup window showing a list of installable OSH bundles
 * </p>
 *
 * @author Alex Robin
 * @since April 10, 2021
 */
@SuppressWarnings({ "serial", "deprecation" })
public class DownloadOsgiBundlesPopup extends Window
{
    Table table;
    transient ExecutorService exec = Executors.newFixedThreadPool(2);
    
    
    public DownloadOsgiBundlesPopup(Collection<String> repoUrls, BundleContext osgiCtx)
    {
        super("Download Add-on Modules");
        Asserts.checkNotNull(repoUrls, "repoUrls");
        setModal(true);
                
        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
                
        final HorizontalLayout loading = new HorizontalLayout();
        loading.setSpacing(true);
        ProgressBar pb = new ProgressBar();
        pb.setIndeterminate(true);
        loading.addComponent(pb);
        loading.addComponent(new Label("Loading Bundles Information..."));
        layout.addComponent(loading);
        
        // buttons bar
        final HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        layout.addComponent(buttons);
        layout.setComponentAlignment(buttons, Alignment.MIDDLE_CENTER);
        
        // OK button
        Button installBtn = new Button("Install Selected");
        installBtn.addStyleName(UIConstants.STYLE_SMALL);
        buttons.addComponent(installBtn);
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.addStyleName(UIConstants.STYLE_SMALL);
        cancelBtn.addClickListener(event -> DownloadOsgiBundlesPopup.this.close());
        buttons.addComponent(cancelBtn);
        
        setContent(layout);
        center();
        
        // load bundle list in separate thread
        exec.execute(() -> {
            try
            {
                var ref = osgiCtx.getServiceReference(RepositoryAdmin.class);
                var repo = osgiCtx.getService(ref);
                
                for (var url: repoUrls)
                    repo.addRepository(url);
                
                var resources = repo.discoverResources("(symbolicname=*)");
                for (var res: resources)
                {
                     addToTable(res, layout);
                }
                
                installBtn.addClickListener(event -> {
                    var log = ((AdminUI)UI.getCurrent()).getOshLogger();
                    
                    @SuppressWarnings("unchecked")
                    var selectedBundles = (Collection<Resource>)table.getValue();
                    if (selectedBundles != null && !selectedBundles.isEmpty())
                    {
                        var resolver = repo.resolver();
                        selectedBundles.forEach(r -> {
                            resolver.add(r);
                            log.info("Installing {}:{}...", r.getSymbolicName(), r.getVersion());
                        });
                        if (resolver.resolve())
                        {
                            var installedList = new ArrayList<String>();
                            osgiCtx.addBundleListener(e -> {
                                if (e.getType() == BundleEvent.INSTALLED)
                                {
                                    var b = e.getBundle();
                                    var name = b.getSymbolicName();
                                    name += " v" + b.getVersion();
                                    installedList.add(name);
                                }
                            });
                            
                            // install and start all bundles
                            resolver.deploy(Resolver.START);
                            DownloadOsgiBundlesPopup.this.close();
                            DisplayUtils.showOperationSuccessful("Bundles successfully installed<br/><br/>"
                                + String.join("<br/>", installedList), Notification.DELAY_FOREVER);
                        }
                        else
                        {
                            for (var req: resolver.getUnsatisfiedRequirements())
                                log.error("Unable to resolve: " + req);
                            DisplayUtils.showErrorPopup("Error installing bundles. See log for details", null);
                        }
                    }
                });
                
                // remove loading indicator when done
                getUI().access(() -> {
                    layout.removeComponent(loading);
                    getUI().push();
                });
            }
            catch (final Exception e)
            {
                final UI ui = getUI();
                ui.access(() -> {
                    DisplayUtils.showErrorPopup("Cannot fetch OSH bundle list", e);
                    DownloadOsgiBundlesPopup.this.close();
                    ui.push();
                });
            }
        });
    }
    
    
    protected void addToTable(final Resource pkg, final VerticalLayout layout)
    {
        // update table in UI thread
        getUI().access(() -> {
         // create table if not there yet
            if (table == null)
            {
                setWidth(70, Unit.PERCENTAGE);
                table = new Table();
                table.addStyleName(UIConstants.STYLE_SMALL);
                table.setSizeFull();
                table.setSelectable(true);
                table.setMultiSelect(true);
                table.addContainerProperty(ModuleTypeSelectionPopup.PROP_DESC, String.class, null);
                table.addContainerProperty(ModuleTypeSelectionPopup.PROP_VERSION, String.class, null);
                table.addContainerProperty(ModuleTypeSelectionPopup.PROP_NAME, String.class, null);
                table.setColumnHeaders(new String[] {"Description", "Version", "Bundle Name"});
                table.setVisibleColumns(
                    ModuleTypeSelectionPopup.PROP_DESC,
                    ModuleTypeSelectionPopup.PROP_VERSION,
                    ModuleTypeSelectionPopup.PROP_NAME);
                //table.setColumnWidth(ModuleTypeSelectionPopup.PROP_NAME, 300);
                //table.setColumnExpandRatio(ModuleTypeSelectionPopup.PROP_DESC, 10);
                layout.addComponent(table, 0);
                
                var searchBox = new SearchBox("Search", null);
                searchBox.focus();
                searchBox.addToParent(layout, 0);
                searchBox.addTextChangeListener(new TextChangeListener() {
                    SimpleStringFilter filter = null;

                    public void textChange(TextChangeEvent event) {
                        Filterable f = (Filterable)
                            table.getContainerDataSource();
                        
                        // remove old filter
                        if (filter != null)
                            f.removeContainerFilter(filter);
                        
                        // set new filter for the description column
                        filter = new SimpleStringFilter(
                            ModuleTypeSelectionPopup.PROP_DESC, event.getText(), true, false);
                        f.addContainerFilter(filter);
                    }
                });
            }
            
            // add bundle info to table
            var description = pkg.getProperties().get(Resource.DESCRIPTION);
            table.addItem(new Object[] {description, pkg.getVersion().toString(), pkg.getPresentationName()}, pkg);
        });
    }
    
}
