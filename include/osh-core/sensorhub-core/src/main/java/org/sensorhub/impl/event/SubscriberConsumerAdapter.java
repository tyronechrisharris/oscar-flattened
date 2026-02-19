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
import java.util.function.Consumer;
import org.sensorhub.api.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.util.Asserts;


public class SubscriberConsumerAdapter<E extends Event> implements Subscriber<E>
{       
    private static final Logger log = LoggerFactory.getLogger(SubscriberConsumerAdapter.class);
    
    Consumer<? super Subscription> onSubscribe;
    Consumer<? super E> onNext;
    Consumer<Throwable> onError;
    Runnable onComplete;
    boolean enableFlowControl;
    
    
    public SubscriberConsumerAdapter(Consumer<? super Subscription> onSubscribe, Consumer<? super E> onNext, boolean enableFlowControl)
    {
        Asserts.checkNotNull(onSubscribe, "onSubscribe");
        Asserts.checkNotNull(onNext, "onNext");
        
        this.onSubscribe = onSubscribe;
        this.onNext = onNext;
        this.enableFlowControl = enableFlowControl;
    }
    
    
    public SubscriberConsumerAdapter(Consumer<? super Subscription> onSubscribe, Consumer<? super E> onNext, Consumer<Throwable> onError, boolean enableFlowControl)
    {
        this(onSubscribe, onNext, enableFlowControl);
        this.onError = onError;
    }
    
    
    public SubscriberConsumerAdapter(Consumer<? super Subscription> onSubscribe, Consumer<? super E> onNext, Consumer<Throwable> onError, Runnable onComplete, boolean enableFlowControl)
    {
        this(onSubscribe, onNext, onError, enableFlowControl);
        this.onComplete = onComplete;
    }
    
    
    @Override
    public void onSubscribe(Subscription sub)
    {
        if (!enableFlowControl)
            sub.request(Long.MAX_VALUE);
        onSubscribe.accept(sub);
    }
    
    
    @Override
    public void onNext(E e)
    {
        onNext.accept(e);
    }
    

    @Override
    public void onError(Throwable e)
    {
        if (onError != null)
            onError.accept(e);
        else
            log.error("Uncaught exception during registration or event dispatch", e);
    }
    
    
    @Override
    public void onComplete()
    {
        if (onComplete != null)
            onComplete.run();
    }
}
