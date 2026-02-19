/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2017 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.vast.xml.DOMHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.vaadin.v7.data.Item;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.ProgressBar;
import com.vaadin.v7.ui.TreeTable;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


/**
 * <p>
 * Popup window showing a list of downloadable OSH modules
 * </p>
 *
 * @author Alex Robin
 * @since Feb 24, 2017
 */
@SuppressWarnings("serial")
public class DownloadModulesPopup extends Window
{
    private static final String BINTRAY_API_ROOT = "https://api.bintray.com/";
    private static final String BINTRAY_SUBJECT = "sensiasoft";
    private static final String BINTRAY_REPO = "osh";
    private static final String BINTRAY_PKG_LIST = "repos/" + BINTRAY_SUBJECT + "/" + BINTRAY_REPO + "/packages";
    private static final String BINTRAY_PKG = "packages/" + BINTRAY_SUBJECT + "/" + BINTRAY_REPO + "/";
    private static final String BINTRAY_WEB_ROOT = "https://bintray.com/" + BINTRAY_SUBJECT + "/" + BINTRAY_REPO + "/";
    private static final String BINTRAY_CONTENT_ROOT = "https://dl.bintray.com/" + BINTRAY_SUBJECT + "/" + BINTRAY_REPO + "/";
    private static final String LINK_TARGET = "osh-bintray";
    private static final String LOADING_MSG = "Loading...";
    
    TreeTable table;
    transient ExecutorService exec = Executors.newFixedThreadPool(2);
    
    
    static class BintrayArtifact
    {
        public String name;
        public String path;
        public String label;
        public String desc = LOADING_MSG;
        public String version;
        public String author = LOADING_MSG;
    }
    
    
    static class BintrayPackage
    {
        public String name;
        public String desc;
        public String website_url;
        public String updated;
        public Collection<BintrayArtifact> files;
    }
    
    
    public DownloadModulesPopup()
    {
        super("Download Add-on Modules");
        setModal(true);
                
        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
                
        final HorizontalLayout loading = new HorizontalLayout();
        loading.setSpacing(true);
        ProgressBar pb = new ProgressBar();
        pb.setIndeterminate(true);
        loading.addComponent(pb);
        loading.addComponent(new Label("Loading Package Information..."));
        layout.addComponent(loading);
        
        setContent(layout);
        center();
        
        // load info in separate thread
        exec.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    for (final String pkgName: getBintrayPackageNames())
                    {
                        // don't list core modules
                        if (pkgName.equals("osh-core"))
                            continue;
                        
                        try
                        {
                            BintrayPackage pkg = getBintrayPackageInfo(pkgName);
                            if (pkg != null)
                            {
                                pkg.files =  getBintrayPackageFiles(pkgName);
                                
                                // remove non-executable jar files
                                Iterator<BintrayArtifact> it = pkg.files.iterator();
                                while (it.hasNext())
                                {
                                    String fName = it.next().name;
                                    if (!fName.endsWith("jar") || fName.endsWith("-sources.jar") || fName.endsWith("-javadoc.jar"))
                                        it.remove();
                                }
                                
                                // add package info in table
                                updateTable(pkg, layout);
                            }
                        }
                        catch (Exception e)
                        {
                            ((AdminUI)UI.getCurrent()).getOshLogger().error("Cannot load package info for " + pkgName, e);
                        }
                    }
                    
                    // remove loading indicator when done
                    getUI().access(new Runnable() {
                        @Override
                        public void run()
                        {
                            layout.removeComponent(loading);
                            getUI().push();
                        }
                    });
                }
                catch (final Exception e)
                {
                    final UI ui = getUI();
                    ui.access(new Runnable() {
                        @Override
                        public void run()
                        {
                            DisplayUtils.showErrorPopup("Cannot fetch OSH package list", e);
                            DownloadModulesPopup.this.close();
                            ui.push();
                        }
                    });
                }
            }
        });
    }
    
    
    protected void updateTable(final BintrayPackage pkg, final VerticalLayout layout)
    {
        // update table in UI thread
        getUI().access(new Runnable() {
            @Override
            public void run()
            {
                // create table if not there yet
                if (table == null)
                {
                    setWidth(70, Unit.PERCENTAGE);
                    table = new TreeTable();
                    table.setSizeFull();      
                    table.setSelectable(false);
                    table.addContainerProperty(ModuleTypeSelectionPopup.PROP_NAME, Component.class, null);
                    table.addContainerProperty(ModuleTypeSelectionPopup.PROP_VERSION, String.class, null);
                    table.addContainerProperty(ModuleTypeSelectionPopup.PROP_DESC, String.class, null);
                    table.addContainerProperty(ModuleTypeSelectionPopup.PROP_AUTHOR, String.class, null);
                    table.setColumnHeaders(new String[] {"Package", "OSH Version", "Description", "Author"});
                    table.setColumnWidth(ModuleTypeSelectionPopup.PROP_NAME, 300);
                    table.setColumnExpandRatio(ModuleTypeSelectionPopup.PROP_DESC, 10);
                    layout.addComponent(table, 0);
                }
                
                // add package info
                String href = BINTRAY_WEB_ROOT + pkg.name; 
                Link link = new Link(pkg.name, new ExternalResource(href), LINK_TARGET, 0, 0, null);
                Object parentId = table.addItem(new Object[] {link, null, pkg.desc, null}, null);
                
                // add artifacts to table
                for (BintrayArtifact f: pkg.files)
                {
                    href = BINTRAY_CONTENT_ROOT + f.path; 
                    link = new Link(f.name, new ExternalResource(href), "_self", 0, 0, null);
                    Object id = table.addItem(new Object[] {link, f.version, f.desc, f.author}, f.name);
                    table.setParent(id, parentId);
                    table.setChildrenAllowed(id, false);
                }
                
                getUI().push();
                
                // also load info from POM in separate thread
                exec.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (final BintrayArtifact f: pkg.files)
                        {
                            String pomUrl = BINTRAY_CONTENT_ROOT + f.path.replaceAll(".jar", ".pom");
                            readInfoFromPom(pomUrl, f);
                            
                            getUI().access(new Runnable() {
                                @Override
                                public void run()
                                {
                                    Item item = table.getItem(f.name);
                                    item.getItemProperty(ModuleTypeSelectionPopup.PROP_DESC).setValue(f.desc);
                                    item.getItemProperty(ModuleTypeSelectionPopup.PROP_AUTHOR).setValue(f.author);
                                    getUI().push();
                                }
                            });                                                    
                        }
                    }
                }); 
            }
        });
    }
    
    
    protected Collection<String> getBintrayPackageNames() throws IOException
    {
     // get OSH package names
        ArrayList<String> pkgNames = new ArrayList<String>();
        URL url = new URL(BINTRAY_API_ROOT + BINTRAY_PKG_LIST);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream())))
        {
            JsonParser parser = new JsonParser();
            JsonArray root = (JsonArray)parser.parse(reader);
            for (JsonElement elt: root)
            {
                if (elt.isJsonObject())
                {
                    JsonElement name = ((JsonObject)elt).get("name");
                    if (name != null)
                        pkgNames.add(name.getAsString());
                }
            }
        }
        
        return pkgNames;
    }
    
    
    protected BintrayPackage getBintrayPackageInfo(String pkgName) throws IOException
    {
        URL url = new URL(BINTRAY_API_ROOT + BINTRAY_PKG + pkgName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream())))
        {
            Gson gson = new Gson();
            return gson.fromJson(reader, BintrayPackage.class);
        }
    }
    
    
    protected Collection<BintrayArtifact> getBintrayPackageFiles(String pkgName) throws IOException
    {
        final Collection<BintrayArtifact> files;
        
        URL url = new URL(BINTRAY_API_ROOT + BINTRAY_PKG + pkgName + "/files");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream())))
        {
            Gson gson = new Gson();
            Type collectionType = new TypeToken<Collection<BintrayArtifact>>(){}.getType();
            files = gson.fromJson(reader, collectionType);
        }  
        
        return files;
    }
    
    
    protected void readInfoFromPom(String url, BintrayArtifact item)
    {
        try
        {
            DOMHelper dom = new DOMHelper(url, false);
            
            String pomName = dom.getElementValue("name");
            if (pomName != null && !pomName.isEmpty())
                item.label = pomName;
            item.desc = dom.getElementValue("description");
            item.author = dom.getElementValue("developers/developer/organization");
        }
        catch (Exception e)
        {
            ((AdminUI)UI.getCurrent()).getOshLogger().error("Cannot read POM at " + url, e);
        }
    }
    
}
