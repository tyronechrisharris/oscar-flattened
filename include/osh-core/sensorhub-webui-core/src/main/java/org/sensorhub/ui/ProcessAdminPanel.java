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

import net.opengis.sensorml.v20.AbstractProcess;
import net.opengis.sensorml.v20.AggregateProcess;
import net.opengis.sensorml.v20.Link;
import net.opengis.sensorml.v20.Settings;
import net.opengis.sensorml.v20.SimpleProcess;
import net.opengis.swe.v20.DataComponent;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.sensorhub.api.command.IStreamingControlInterface;
import org.sensorhub.api.data.IDataProducer;
import org.sensorhub.api.module.IModule;
import org.sensorhub.api.module.ModuleConfig;
import org.sensorhub.api.processing.IProcessModule;
import org.sensorhub.api.processing.ProcessingException;
import org.sensorhub.impl.processing.SMLProcessConfig;
import org.sensorhub.impl.processing.SMLProcessImpl;
import org.sensorhub.impl.processing.StreamDataSource;
import org.sensorhub.ui.ModuleInstanceSelectionPopup.ModuleInstanceSelectionCallback;
import org.sensorhub.ui.ProcessFlowDiagram.Connection;
import org.sensorhub.ui.ProcessFlowDiagram.ProcessBlock;
import org.sensorhub.ui.ProcessFlowDiagram.ProcessFlowState;
import org.sensorhub.ui.ProcessSelectionPopup.ProcessSelectionCallback;
import org.sensorhub.ui.data.MyBeanItem;
import org.sensorhub.utils.FileUtils;
import org.vast.data.DataRecordImpl;
import org.vast.process.ProcessInfo;
import org.vast.sensorML.SMLHelper;
import org.vast.sensorML.SMLUtils;
import org.vast.swe.SWEHelper;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;


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
public class ProcessAdminPanel extends DataSourceAdminPanel<IProcessModule<?>>
{
    Panel inputCommandsPanel, paramCommandsPanel, processFlowPanel;
    ProcessFlowDiagram diagram;
    SMLProcessConfig config;
    
    
    @Override
    public void build(final MyBeanItem<ModuleConfig> beanItem, final IProcessModule<?> module)
    {
        super.build(beanItem, module);
        
        // inputs control section
        if (!module.getInputDescriptors().isEmpty())
        {
            // title
            addComponent(new Spacing());
            HorizontalLayout titleBar = new HorizontalLayout();
            titleBar.setSpacing(true);
            Label sectionLabel = new Label("Process Inputs");
            sectionLabel.addStyleName(STYLE_H3);
            sectionLabel.addStyleName(STYLE_COLORED);
            titleBar.addComponent(sectionLabel);
            titleBar.setComponentAlignment(sectionLabel, Alignment.MIDDLE_LEFT);
            titleBar.setHeight(31.0f, Unit.PIXELS);
            addComponent(titleBar);

            // control panels
            buildControlInputsPanels(module);
        }
        
        // params control section
        if (!module.getParameterDescriptors().isEmpty())
        {
            // title
            addComponent(new Spacing());
            HorizontalLayout titleBar = new HorizontalLayout();
            titleBar.setSpacing(true);
            Label sectionLabel = new Label("Process Parameters");
            sectionLabel.addStyleName(STYLE_H3);
            sectionLabel.addStyleName(STYLE_COLORED);
            titleBar.addComponent(sectionLabel);
            titleBar.setComponentAlignment(sectionLabel, Alignment.MIDDLE_LEFT);
            titleBar.setHeight(31.0f, Unit.PIXELS);
            addComponent(titleBar);

            // control panels
            buildParamInputsPanels(module);
        }
        
        // process flow section
        if (module instanceof SMLProcessImpl)
        {
            //addProcessFlowEditor((SMLProcessImpl)module);
            this.config = (SMLProcessConfig)beanItem.getBean();
        }
    }
    
    
    protected void buildControlInputsPanels(IProcessModule<?> module)
    {
        if (module != null)
        {
            Panel oldPanel;
            
            // command inputs
            oldPanel = inputCommandsPanel;
            inputCommandsPanel = newPanel(null);
            for (IStreamingControlInterface input: module.getCommandInputs().values())
            {
                Component sweForm = new SWEControlForm(input);
                ((Layout)inputCommandsPanel.getContent()).addComponent(sweForm);
            }

            if (oldPanel != null)
                replaceComponent(oldPanel, inputCommandsPanel);
            else
                addComponent(inputCommandsPanel);
        }
    }
    
    
    protected void buildParamInputsPanels(IProcessModule<?> module)
    {
        if (module != null)
        {
            Panel oldPanel;

            // command inputs
            oldPanel = paramCommandsPanel;
            paramCommandsPanel = newPanel(null);

            // wrap all parameters into a single datarecord so we can submit them together
            DataRecordImpl params = new DataRecordImpl();
            params.setName("Parameters");
            for (DataComponent param: module.getParameterDescriptors().values())
                params.addComponent(param.getName(), param);
            params.combineDataBlocks();

            Component sweForm = new SWEControlForm(params);
            ((Layout)paramCommandsPanel.getContent()).addComponent(sweForm);

            if (oldPanel != null)
                replaceComponent(oldPanel, paramCommandsPanel);
            else
                addComponent(paramCommandsPanel);
        }
    }
        
        
    protected void addProcessFlowEditor(final SMLProcessImpl module)
    {
        HorizontalLayout buttonBar = new HorizontalLayout();
        buttonBar.setSpacing(true);
        addComponent(buttonBar);        
        
        // add data source button
        Button addDatasrcBtn = new Button("Datasource", ADD_ICON);
        addDatasrcBtn.addStyleName(STYLE_SMALL);
        buttonBar.addComponent(addDatasrcBtn);        
        addDatasrcBtn.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event)
            {
                // show popup to select among available module types
                final ModuleInstanceSelectionCallback callback = new ModuleInstanceSelectionCallback() {
                    @Override
                    @SuppressWarnings("rawtypes")
                    public void onSelected(IModule module) throws ProcessingException
                    {
                        diagram.addNewDataSource((IDataProducer)module);
                    }
                };
                
                // popup the list so the user can select what he wants
                ModuleInstanceSelectionPopup popup = new ModuleInstanceSelectionPopup(IDataProducer.class, callback);
                popup.setModal(true);
                getUI().addWindow(popup);
            }
        });
        
        // add process button
        Button addProcessBtn = new Button("Process", ADD_ICON);
        addProcessBtn.addStyleName(STYLE_SMALL);
        buttonBar.addComponent(addProcessBtn);
        addProcessBtn.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event)
            {
                // show popup to select among available module types
                final ProcessSelectionCallback callback = new ProcessSelectionCallback() {
                    @Override
                    public void onSelected(String name, ProcessInfo info) throws ProcessingException
                    {
                        diagram.addNewProcess(name, info);
                    }
                };
                
                // popup the list so the user can select what he wants
                ProcessSelectionPopup popup = new ProcessSelectionPopup(getParentHub().getProcessingManager().getAllProcessingPackages(), callback);
                popup.setModal(true);
                getUI().addWindow(popup);
            }
        });
        
        // add process flow diagram
        diagram = new ProcessFlowDiagram(module.getProcessChain());
        addComponent(diagram);
    }
    
    
    @Override
    protected void beforeUpdateConfig() throws ProcessingException
    {
        //if (module instanceof SMLProcessImpl)
        //    saveProcessChain();
    }
    
    
    protected void saveProcessChain() throws ProcessingException
    {
        AggregateProcess processChain = null;
        
        try
        {
            ProcessFlowState state = diagram.getState();
            
            // do nothing if diagram is empty
            // maybe we just want to read from file
            if (state.processBlocks.isEmpty())
                return;
            
            // convert diagram state to SensorML process chain
            String uid = module.getUniqueIdentifier();
            SMLHelper sml = new SMLHelper();
                
            processChain = sml.createAggregateProcess()
                .uniqueID(uid)
                .build();
            
            // data sources            
            for (ProcessBlock block: state.dataSources.values())
            {
                SimpleProcess p = sml.createSimpleProcess()
                    .uniqueID(block.id)
                    .typeOf(block.uri)
                    .build();
                Settings settings = sml.getFactory().newSettings();
                settings.addSetValue("parameters/"+StreamDataSource.PRODUCER_URI_PARAM, block.id);
                p.setConfiguration(settings);
                saveShape(block, p);
                
                processChain.addComponent(block.name, p);
            }
            
            // process blocks
            for (ProcessBlock block: state.processBlocks.values())
            {
                SimpleProcess p = sml.createSimpleProcess()
                    .uniqueID(block.id)
                    .typeOf(block.uri)
                    .build();
                
                saveShape(block, p);
                processChain.addComponent(block.name, p);
                processChain.addOutput("test", new SWEHelper().newQuantity());
            }
            
            // connections
            for (Connection c: state.connections.values())
            {
                Link link = sml.getFactory().newLink();
                link.setSource(c.src);
                link.setDestination(c.dest);
                processChain.addConnection(link);
            }
        }
        catch (Exception e)
        {
            throw new ProcessingException("Cannot create SensorML process chain", e);
        }
        
        // save SensorML file
        String smlPath = config.getSensorMLPath();
        if (smlPath == null || !FileUtils.isSafeFilePath(smlPath))
            throw new ProcessingException("Cannot save process chain: A valid SensorML file path must be provided");
        
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(smlPath)))
        {
            new SMLUtils(SMLUtils.V2_0).writeProcess(os, processChain, true);
        }
        catch (Exception e)
        {
            throw new ProcessingException(String.format("Cannot write SensorML description at '%s'", smlPath), e);
        }        
    }
    
    
    protected void saveShape(ProcessBlock block, AbstractProcess p)
    {
    }
    
    
    /*@Override
    protected void refreshContent()
    {
        ProcessFlowDiagram oldDiagram = diagram;
        diagram = new ProcessFlowDiagram(((SMLProcessImpl)module).getProcessChain());
        replaceComponent(oldDiagram, diagram);
    }*/
}
