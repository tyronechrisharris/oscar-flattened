/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.utils;

import java.util.ServiceLoader;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.sensorhub.api.module.IModuleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class OshBundleActivator implements BundleActivator
{
    protected static Logger log = LoggerFactory.getLogger(OshBundleActivator.class);
    
    
    @Override
    public void start(BundleContext context) throws Exception
    {
        log.info("Activating bundle " + context.getBundle().getSymbolicName());
        
        // get bundle classloader
        //var bundle = context.getBundle();
        //var bundleWiring = bundle.adapt(BundleWiring.class);
        //var bundleClassLoader = bundleWiring.getClassLoader();
        var bundleClassLoader = this.getClass().getClassLoader();
        
        // register all module providers as OSGi services
        var modules = ServiceLoader.load(IModuleProvider.class, bundleClassLoader);
        modules.forEach(m -> {
            log.info("Registering module: " + m.getModuleName());
            context.registerService(IModuleProvider.class, m, null);
        });
    }


    @Override
    public void stop(BundleContext context) throws Exception
    {
    }

}
