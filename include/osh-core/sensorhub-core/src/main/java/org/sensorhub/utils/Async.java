/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2017 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.utils;

import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;


public class Async
{
    private Async() {}
    
    
    
    /**
     * Calls {@link #waitForCondition(BooleanSupplier, long, long)} with retryInterval=100 ms
     * @see #waitForCondition(BooleanSupplier, long, long)
     */
    @SuppressWarnings("javadoc")
    public static void waitForCondition(BooleanSupplier condition, long timeout) throws TimeoutException
    {
        waitForCondition(condition, 100L, timeout);
    }
    
    
    /**
     * Helper method to block until a condition is true.<br/>
     * Always prefer inter-thread communication with wait()/notify() to this method when possible.
     * This is to be used solely when inter-thread communication is difficult to implement.
     * @param condition
     * @param retryInterval
     * @param timeout
     * @throws TimeoutException
     */
    public static void waitForCondition(BooleanSupplier condition, long retryInterval, long timeout) throws TimeoutException
    {
        try
        {
            long t0 = System.currentTimeMillis();
            
            while (!condition.getAsBoolean())
            {
                if (System.currentTimeMillis() > t0+timeout)
                    throw new TimeoutException();
                Thread.sleep(retryInterval);
            }
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new TimeoutException("Waiting thread interrupted");
        }
    }
    
}
