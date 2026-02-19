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

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import org.sensorhub.impl.event.DelegatingSubscriber;
import org.vast.ogc.om.IObservation;
import org.vast.ows.sos.GetObservationRequest;
import org.vast.ows.sos.SOSException;


public class GetObsMultiProviderSubscriber extends DelegatingSubscriber<IObservation>
{
    Map<String, ISOSAsyncDataProvider> dataProvidersMap;
    Iterator<Entry<String, ISOSAsyncDataProvider>> dataProvidersEntryIt;
    GetObservationRequest request;
    Set<String> defaultProviderProcedures;
    ProviderSubscription wrappedSub;
    
    
    static class ProviderSubscription implements Subscription
    {
        Subscription currentProviderSub;
        
        @Override
        public void request(long n)
        {
            currentProviderSub.request(n);           
        }

        @Override
        public void cancel()
        {
            currentProviderSub.cancel();
        }                
    }
    
    
    public GetObsMultiProviderSubscriber(Map<String, ISOSAsyncDataProvider> dataProvidersMap, GetObservationRequest request, Subscriber<IObservation> subscriber)
    {
        super(subscriber);
        this.dataProvidersMap = dataProvidersMap;
        this.dataProvidersEntryIt = dataProvidersMap.entrySet().iterator();
        this.request = request;
        
        // extract procedures mapped to default provider
        defaultProviderProcedures = new HashSet<>();
        defaultProviderProcedures.addAll(request.getProcedures());
        defaultProviderProcedures.removeAll(dataProvidersMap.keySet());
    }
    
    
    @Override
    public void onSubscribe(Subscription subscription)
    {                
        // for the 1st provider, we wrap the subscription
        // and call the serializer onSubscribe()
        if (wrappedSub == null)
        {
            wrappedSub = new ProviderSubscription();
            wrappedSub.currentProviderSub = subscription;
            super.onSubscribe(wrappedSub);
        }
        
        // for the next provider, we just update the
        // wrapped subscription and we request more data
        else
        {
            wrappedSub.currentProviderSub = subscription;
            wrappedSub.request(1);
        }
    }

    @Override
    public void onComplete()
    {
        try
        {
            // if more providers are queued, we go to next provider
            if (dataProvidersEntryIt.hasNext())
                callNextDataProvider();
            
            // otherwise we call onComplete on the serializer
            else
                super.onComplete();
        }
        catch (Exception e)
        {
            onError(e);
        }
    }
    
    
    protected void callNextDataProvider() throws SOSException, IOException
    {
        var nextProviderEntry = dataProvidersEntryIt.next();
        
        // generate correct procedure list for this provider
        request.getProcedures().clear();                        
        if (SOSServlet.DEFAULT_PROVIDER_KEY.equals(nextProviderEntry.getKey()))
            request.getProcedures().addAll(defaultProviderProcedures);            
        else
            request.getProcedures().add(nextProviderEntry.getKey());
        
        var nextProvider = nextProviderEntry.getValue();
        nextProvider.getObservations(request, this);
    }
    
    
    public void start() throws SOSException, IOException
    {
        callNextDataProvider();
    }
}
