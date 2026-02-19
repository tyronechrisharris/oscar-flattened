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

import org.sensorhub.api.module.ModuleConfig;
import org.sensorhub.impl.service.sps.SPSService;
import org.sensorhub.ui.api.IModuleAdminPanel;
import org.sensorhub.ui.api.UIConstants;
import org.sensorhub.ui.data.MyBeanItem;
import org.vast.ows.sps.SPSOfferingCapabilities;
import org.vast.ows.sps.SPSServiceCapabilities;
import com.vaadin.v7.data.Property;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;


/**
 * <p>
 * Admin panel for SOS service modules.<br/>
 * This adds a section with example links to SOS operations
 * </p>
 *
 * @author Alex Robin
 * @since 1.0
 */
@SuppressWarnings("serial")
public class SPSAdminPanel extends DefaultModulePanel<SPSService> implements IModuleAdminPanel<SPSService>
{
    private static final String PROP_ENDPOINT = "endPoint";
    private static final String LINK_TARGET = "osh-sps";
    
    
    static class LinkItem extends HorizontalLayout
    {
        public LinkItem(String title, String linkText, String href)
        {
            setSpacing(true);
            setMargin(false);
            
            Label label = new Label(title + ":");
            label.addStyleName(UIConstants.STYLE_SMALL);
            addComponent(label);
            
            addLinkItem(linkText, href);
        }
        
        public void addLinkItem(String linkText, String href)
        {
            Link link = new Link(linkText, new ExternalResource(href), LINK_TARGET, 0, 0, null);
            link.addStyleName(UIConstants.STYLE_SMALL);
            addComponent(link);
        }
    }    
    
    
    @Override
    public void build(final MyBeanItem<ModuleConfig> beanItem, final SPSService module)
    {
        super.build(beanItem, module);
        
        // get capabilities
        SPSServiceCapabilities caps = module.getCapabilities();
        
        // get base URL
        String baseUrl = null;
        Property<?> endPointProp = beanItem.getItemProperty(PROP_ENDPOINT);
        if (endPointProp != null)
        {
            baseUrl = (String)endPointProp.getValue();
            if (baseUrl != null)
                baseUrl = baseUrl.substring(1);
        }
        
        if (module.isStarted() && caps != null && baseUrl != null)
        {
            //ComponentContainer parent = (ComponentContainer)configTabs.getTab(0).getComponent();
            
            VerticalLayout parent = new VerticalLayout();
            configTabs.addTab(parent, "Test Links");
            LinkItem linkItem;
            baseUrl += "?service=SPS&version=2.0&request=";
            
            VerticalLayout topLevelLinks = new VerticalLayout();
            topLevelLinks.setSpacing(false);
            topLevelLinks.setMargin(false);
            parent.addComponent(topLevelLinks);
            
            // link to capabilities            
            String href = baseUrl + "GetCapabilities";
            linkItem = new LinkItem("Service Capabilities", "XML", href);
            topLevelLinks.addComponent(linkItem);
            
            // offering links in tabs
            TabSheet linkTabs = new TabSheet();
            parent.addComponent(linkTabs);
                        
            for (SPSOfferingCapabilities offering: caps.getLayers())
            {
                VerticalLayout tabLayout = new VerticalLayout();
                tabLayout.setMargin(true);
                tabLayout.setSpacing(false);
                Tab tab = linkTabs.addTab(tabLayout, offering.getTitle());
                tab.setDescription(offering.getDescription());
                
                
                // sensor description
                href = baseUrl + "DescribeSensor&procedure=" + offering.getMainProcedure();
                linkItem = new LinkItem("Sensor Description", "XML", href);
                tabLayout.addComponent(linkItem);
                
                // fois
                href = baseUrl + "DescribeTasking&procedure=" + offering.getMainProcedure();
                linkItem = new LinkItem("Tasking Parameters", "XML", href);
                tabLayout.addComponent(linkItem);
                
                
            }
        }
    }
}
