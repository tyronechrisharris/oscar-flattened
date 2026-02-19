/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.processing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;
import org.sensorhub.api.ISensorHub;
import org.sensorhub.api.processing.IProcessProvider;
import org.sensorhub.api.processing.IProcessingManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.process.IProcessExec;
import org.vast.process.ProcessException;
import org.vast.process.ProcessInfo;


/**
 * <p>
 * Default implementation of the processing manager interface
 * </p>
 *
 * @author Alex Robin
 * @since Feb 28, 2015
 */
public class ProcessingManagerImpl implements IProcessingManager
{
    private static final Logger log = LoggerFactory.getLogger(ProcessingManagerImpl.class);
    protected ISensorHub hub;
    
    
    public ProcessingManagerImpl(ISensorHub hub)
    {
        this.hub = hub;
    }


    @Override
    public Collection<IProcessProvider> getAllProcessingPackages()
    {
        ArrayList<IProcessProvider> providers = new ArrayList<>();
        
        ServiceLoader<IProcessProvider> sl = ServiceLoader.load(IProcessProvider.class);
        try
        {
            for (IProcessProvider provider: sl)
                providers.add(provider);
        }
        catch (Exception e)
        {
            log.error("Invalid reference to process provider", e);
        }
        
        return providers;
    }


    @Override
    public IProcessExec loadProcess(String uri) throws ProcessException
    {
        for (IProcessProvider provider: getAllProcessingPackages())
        {
            if (provider.getProcessMap().containsKey(uri))
            {
                try
                {
                    ProcessInfo info = provider.getProcessMap().get(uri);
                    IProcessExec processInstance = info.getImplementationClass().getDeclaredConstructor().newInstance();
                    
                    // assign parent hub
                    if (processInstance instanceof ISensorHubProcess)
                        ((ISensorHubProcess) processInstance).setParentHub(hub);
                        
                    return processInstance;
                }
                catch (ReflectiveOperationException e)
                {
                    throw new ProcessException("Cannot instantiate process " + uri, e);
                }
            }
        }
        
        throw new ProcessException("Unknown process " + uri);
    }    
}
