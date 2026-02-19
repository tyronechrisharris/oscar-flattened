/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.utils;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;


/**
 * <p>
 * Wraps an executor to serialize the execution of tasks via a queue
 * </p>
 *
 * @author Alex Robin
 * @date Dec 7, 2020
 */
public class SerialExecutor implements Executor
{
    final Queue<Runnable> tasks;
    final Executor executor;
    Runnable active;
    

    public SerialExecutor(Executor executor)
    {
        this.executor = executor;   
        this.tasks = new ArrayDeque<Runnable>();
    }
    
    
    public SerialExecutor(Executor executor, int maxQueueSize)
    {
        this.executor = executor;
        this.tasks = new ArrayBlockingQueue<Runnable>(maxQueueSize);
    }


    /**
     * Submit a new task to be run through this serial executor.<br/>
     * All tasks submitted through this method will run in the order they were
     * submitted even though the underlying executor is parallel.
     */
    public synchronized void execute(final Runnable r)
    {
        tasks.offer(new Runnable()
        {
            public void run()
            {
                try { r.run(); }
                finally { scheduleNext(); }
            }
        });
        
        if (active == null)
        {
            scheduleNext();
        }
    }


    protected synchronized void scheduleNext()
    {
        if ((active = tasks.poll()) != null)
        {
            executor.execute(active);
        }
    }
    
    
    public Queue<Runnable> getQueue()
    {
        return tasks;
    }
    
    
    /**
     * Empty the queue of this serial executor
     */
    public synchronized void clear()
    {
        tasks.clear();
    }

}
