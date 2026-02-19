/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sos;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 * Time out monitor providing a unique thread for monitoring all connection
 * timeouts
 * </p>
 *
 * @author Alex Robin
 * @date Apr 15, 2020
 */
public class TimeOutMonitor
{
    ScheduledExecutorService threadPool;
    Set<Callable<Boolean>> triggers = ConcurrentHashMap.newKeySet();
    
    
    public TimeOutMonitor(ScheduledExecutorService threadPool)
    {
        // schedule to run all triggers every 10 seconds by default
        this(threadPool, 1000);
    }
    
    
    /**
     * @param threadPool Thread pool to schedule monitoring task on
     * @param resolution resolution of timeout detection in millis
     */
    public TimeOutMonitor(ScheduledExecutorService threadPool, int resolution)
    {
        this.threadPool = threadPool;
        threadPool.scheduleWithFixedDelay(this::triggerAll, 0, resolution, TimeUnit.MILLISECONDS);
    }
    
    
    protected void triggerAll()
    {
        for (var trigger: triggers)
        {
            try
            {
                if (trigger.call())
                    triggers.remove(trigger);
            }
            catch (Exception e)
            {
                triggers.remove(trigger);
            }
        }
    }
    
    
    public void register(Callable<Boolean> trigger)
    {
        triggers.add(trigger);
    }
    
    
    public void unregister(Callable<Boolean> trigger)
    {
        triggers.remove(trigger);
    }
}
