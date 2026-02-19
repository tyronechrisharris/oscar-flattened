/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.event;

import java.util.concurrent.Flow.Subscriber;
import org.slf4j.Logger;


/**
 * <p>
 * Base class to simplify implementations of subscribers
 * </p>
 *
 * @author Alex Robin
 * @param <T> 
 * @date Feb 24, 2019
 */
public abstract class BaseSubscriber<T> implements Subscriber<T>
{
    String name;
    Logger log;
    
    
    public BaseSubscriber(String name, Logger log)
    {
        this.name = name;
        this.log = log;
    }
    
    
    @Override
    public void onComplete()
    {
        log.info("Subscriber {} will not receive anymore messages", name);
    }
    

    @Override
    public void onError(Throwable e)
    {
        log.error("Error received by subscriber {}", name, e);
    }

}
