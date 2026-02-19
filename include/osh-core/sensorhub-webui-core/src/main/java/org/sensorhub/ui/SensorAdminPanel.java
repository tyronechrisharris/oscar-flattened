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

import org.sensorhub.api.command.IStreamingControlInterface;
import org.sensorhub.api.module.ModuleConfig;
import org.sensorhub.api.sensor.ISensorModule;
import org.sensorhub.ui.data.MyBeanItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;


/**
 * <p>
 * Admin panel for sensor modules.<br/>
 * This adds a section to view structure of inputs and outputs,
 * and allows the user to send commands and view output data values.
 * </p>
 *
 * @author Alex Robin
 * @since 1.0
 */
@SuppressWarnings("serial")
public class SensorAdminPanel extends DataSourceAdminPanel<ISensorModule<?>>
{
    VerticalLayout commandsSection;
    
    
    @Override
    public void build(final MyBeanItem<ModuleConfig> beanItem, final ISensorModule<?> module)
    {
        super.build(beanItem, module);

        // command inputs
        if (module.isInitialized())
        {
            // control inputs section
            if (!module.getCommandInputs().isEmpty())
            {
                // title
                addComponent(new Spacing());
                HorizontalLayout titleBar = new HorizontalLayout();
                titleBar.setSpacing(true);
                Label sectionLabel = new Label("Command Inputs");
                sectionLabel.addStyleName(STYLE_H3);
                sectionLabel.addStyleName(STYLE_COLORED);
                titleBar.addComponent(sectionLabel);
                titleBar.setComponentAlignment(sectionLabel, Alignment.MIDDLE_LEFT);
                titleBar.setHeight(31.0f, Unit.PIXELS);
                addComponent(titleBar);
                
                // control panels
                buildControlInputsPanels(module);
            }
        }
    }
    
    
    protected void buildControlInputsPanels(ISensorModule<?> module)
    {
        if (module != null)
        {
            VerticalLayout oldSection;
            
            // command inputs
            oldSection = commandsSection;
            commandsSection = new VerticalLayout();
            commandsSection.setMargin(false);
            commandsSection.setSpacing(true);
            for (IStreamingControlInterface input: module.getCommandInputs().values())
            {
                var panel = newPanel(null);
                Component sweForm = new SWEControlForm(input);
                ((Layout)panel.getContent()).addComponent(sweForm);
                commandsSection.addComponent(panel);
            }

            if (oldSection != null)
                replaceComponent(oldSection, commandsSection);
            else
                addComponent(commandsSection);
        }
    }
}
