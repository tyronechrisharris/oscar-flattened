/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.sensorhub.api.ISensorHub;
import org.sensorhub.utils.OshBundleActivator;


public class Activator extends OshBundleActivator implements BundleActivator
{
    ISensorHub hub;
    
    
    @Override
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        
        var configFile = context.getProperty("org.sensorhub.config");
        if (configFile == null)
            throw new IllegalStateException("Property 'org.sensorhub.config' must be set to the path of the hub config file");
        
        // start hub only after all other bundles have been started
        context.addFrameworkListener(e -> {
            if (e.getType() == FrameworkEvent.STARTED)
            {
                try
                {
                    SensorHubConfig config = new SensorHubConfig(configFile, null);
                    hub = new SensorHub(config, context);
                    hub.start();
                }
                catch (Throwable err)
                {
                    log.error("Error starting SensorHub", err);
                }
            }
        });
    }


    @Override
    public void stop(BundleContext context) throws Exception
    {
        super.stop(context);
        
        if (hub != null)
            hub.stop();
    }

}
