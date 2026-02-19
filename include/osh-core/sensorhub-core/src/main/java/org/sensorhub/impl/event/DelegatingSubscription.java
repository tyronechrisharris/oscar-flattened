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

import java.util.concurrent.Flow.Subscription;
import org.vast.util.Asserts;


/**
 * <p>
 * Base class for all delegating subscription, useful to simplify implementation
 * of various types of subscriptions.
 * </p>
 *
 * @author Alex Robin
 * @date Apr 10, 2020
 */
public class DelegatingSubscription implements Subscription
{
    Subscription sub;
    
    
    public DelegatingSubscription(Subscription sub)
    {
        this.sub = Asserts.checkNotNull(sub, Subscription.class);
    }
    
    
    @Override
    public void request(long n)
    {
        sub.request(n);
    }


    @Override
    public void cancel()
    {
        sub.cancel();
    }

}
