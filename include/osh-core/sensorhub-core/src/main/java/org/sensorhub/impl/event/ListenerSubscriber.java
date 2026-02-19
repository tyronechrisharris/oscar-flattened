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
import java.util.concurrent.Flow.Subscription;
import org.sensorhub.api.event.Event;
import org.sensorhub.api.event.IEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * Adapts an {@link IEventListener} to the {@link Subscriber} interface
 * </p>
 *
 * @author Alex Robin
 * @date Feb 21, 2019
 */
public class ListenerSubscriber implements Subscriber<Event>
{
    private static final Logger log = LoggerFactory.getLogger(ListenerSubscriber.class);
    
    IEventListener listener;
    Subscription subscription;
    boolean canceled;
    
    
    public ListenerSubscriber(IEventListener listener)
    {
        this.listener = listener;
    }
    
    
    @Override
    public void onComplete()
    {
    }
    

    @Override
    public void onError(Throwable e)
    {
        log.error("Uncaught exception during registration or event dispatch", e);
    }
    

    @Override
    public void onNext(Event e)
    {
        if (!canceled)
            listener.handleEvent(e);
    }
    

    @Override
    public void onSubscribe(Subscription subscription)
    {
        this.subscription = subscription;
        subscription.request(Long.MAX_VALUE);
    }
    
    
    public void cancel()
    {
        canceled = true;
        subscription.cancel();
    }
}
