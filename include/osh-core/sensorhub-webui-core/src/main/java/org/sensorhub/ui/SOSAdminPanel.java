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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.sensorhub.api.module.ModuleConfig;
import org.sensorhub.impl.service.sos.SOSService;
import org.sensorhub.ui.api.IModuleAdminPanel;
import org.sensorhub.ui.api.UIConstants;
import org.sensorhub.ui.data.MyBeanItem;
import org.vast.ows.OWSUtils;
import org.vast.ows.sos.SOSOfferingCapabilities;
import org.vast.ows.sos.SOSServiceCapabilities;
import com.vaadin.v7.data.Property;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.ContentMode;
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
public class SOSAdminPanel extends DefaultModulePanel<SOSService> implements IModuleAdminPanel<SOSService>
{
    private static final String PROP_ENDPOINT = "endPoint";
    private static final String LINK_TARGET = "osh-sos";
    
    
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
    public void build(final MyBeanItem<ModuleConfig> beanItem, final SOSService module)
    {
        super.build(beanItem, module);       
        
        // get capabilities
        SOSServiceCapabilities caps = module.getCapabilities();
        
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
            baseUrl += "?service=SOS&version=2.0&request=";
            
            VerticalLayout topLevelLinks = new VerticalLayout();
            topLevelLinks.setSpacing(false);
            topLevelLinks.setMargin(false);
            parent.addComponent(topLevelLinks);
            
            // link to capabilities            
            String href = baseUrl + "GetCapabilities";
            linkItem = new LinkItem("Service Capabilities", "XML", href);
            topLevelLinks.addComponent(linkItem);
            
            // all fois
            href = baseUrl + "GetFeatureOfInterest";
            linkItem = new LinkItem("All Features of Interest", "XML", href);
            href += "&responseFormat=" + OWSUtils.JSON_MIME_TYPE;
            linkItem.addLinkItem("JSON", href);
            topLevelLinks.addComponent(linkItem);
            
            // offering links in tabs
            TabSheet linkTabs = new TabSheet();
            parent.addComponent(linkTabs);
                        
            for (SOSOfferingCapabilities offering: caps.getLayers())
            {
                VerticalLayout tabLayout = new VerticalLayout();
                tabLayout.setMargin(true);
                tabLayout.setSpacing(false);
                Tab tab = linkTabs.addTab(tabLayout, offering.getTitle());
                tab.setDescription(offering.getDescription());
                
                
                // sensor description
                href = baseUrl + "DescribeSensor&procedure=" + offering.getMainProcedure();
                linkItem = new LinkItem("Sensor Description", "XML", href);
                href += "&procedureDescriptionFormat=" + SOSOfferingCapabilities.FORMAT_SML2_JSON;
                linkItem.addLinkItem("JSON", href);
                tabLayout.addComponent(linkItem);
                
                // fois
                href = baseUrl + "GetFeatureOfInterest&procedure=" + offering.getMainProcedure();
                linkItem = new LinkItem("Features of Interest", "XML", href);
                href += "&responseFormat=" + OWSUtils.JSON_MIME_TYPE;
                linkItem.addLinkItem("JSON", href);
                tabLayout.addComponent(linkItem);
                
                for (String obs: offering.getObservableProperties())
                {                
                    // spacer
                    Label spacer = new Label();
                    spacer.setStyleName(STYLE_SPACER);
                    spacer.setHeight(10, Unit.PIXELS);
                    tabLayout.addComponent(spacer);
                    
                    // observable name
                    String label = obs.substring(obs.lastIndexOf('/')+1);
                    tabLayout.addComponent(new Label("<b>"+label+"</b>", ContentMode.HTML));
                    
                    // result template
                    href = baseUrl + "GetResultTemplate&offering=" + offering.getIdentifier() + "&observedProperty=" + obs;
                    linkItem = new LinkItem("Record Description", "XML", href);
                    href += "&responseFormat=" + OWSUtils.JSON_MIME_TYPE;
                    linkItem.addLinkItem("JSON", href);
                    tabLayout.addComponent(linkItem);
                    
                    // latest obs
                    href = baseUrl + "GetResult&offering=" + offering.getIdentifier() + "&observedProperty=" + obs +
                                     "&temporalFilter=phenomenonTime,now";
                    linkItem = new LinkItem("Latest Measurements", "RAW", href);
                    href += "&responseFormat=" + OWSUtils.JSON_MIME_TYPE;
                    linkItem.addLinkItem("JSON", href);
                    tabLayout.addComponent(linkItem);
                    
                    // live feed
                    href = baseUrl + "GetResult&offering=" + offering.getIdentifier() + "&observedProperty=" + obs +
                                     "&temporalFilter=phenomenonTime,now/" + Instant.now().plus(1, ChronoUnit.HOURS);
                    linkItem = new LinkItem("Live Feed", "RAW", href);
                    href += "&responseFormat=" + OWSUtils.JSON_MIME_TYPE;
                    linkItem.addLinkItem("JSON", href);
                    tabLayout.addComponent(linkItem);
                    
                    // historical data
                    href = baseUrl + "GetResult&offering=" + offering.getIdentifier() + "&observedProperty=" + obs +
                                     "&temporalFilter=phenomenonTime," + Instant.now().minus(1, ChronoUnit.MINUTES) + "/now";
                    linkItem = new LinkItem("Historical Data", "RAW", href);
                    href += "&responseFormat=" + OWSUtils.JSON_MIME_TYPE;
                    linkItem.addLinkItem("JSON", href);
                    tabLayout.addComponent(linkItem);
                }
            }
        }
    }
}
