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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Subscriber;
import org.sensorhub.api.data.ObsEvent;
import org.sensorhub.impl.service.swe.RecordTemplate;
import org.vast.ogc.gml.IFeature;
import org.vast.ogc.om.IObservation;
import org.vast.ogc.om.IProcedure;
import org.vast.ows.sos.GetFeatureOfInterestRequest;
import org.vast.ows.sos.GetObservationRequest;
import org.vast.ows.sos.GetResultRequest;
import org.vast.ows.sos.GetResultTemplateRequest;
import org.vast.ows.sos.SOSException;
import org.vast.ows.swe.DescribeSensorRequest;


/**
 * <p>
 * Interface for all asynchronous SOS data providers.<br/>
 * The {@link #init(SOSDataFilter)} method is expected to be called prior
 * to all other methods.<br/>
 * </p>
 *
 * @author Alex Robin
 * @date Apr 4, 2020
 */
public interface ISOSAsyncDataProvider
{

    /**
     * Retrieve observation result template for this provider
     * @param req SOS GetResultTemplate request object
     * @return A completable future with the template object as the result
     * @throws SOSException if the error should be reported to the SOS client (client-side error)
     * @throws IOException if the error should not be reported to the SOS client (internal server error)
     */
    public CompletableFuture<RecordTemplate> getResultTemplate(GetResultTemplateRequest req) throws SOSException, IOException;


    /**
     * Requests provider to send all selected observation results.<br/>
     * The subscription object provided to the caller (via the {@link Subscriber#onSubscribe(Subscription)}
     * method) will be an instance of {@link DataStreamSubscription}.
     * @param req SOS GetResult request object
     * @param consumer Consumer that will receive record objects from the provider
     * @throws SOSException if the error should be reported to the SOS client (client-side error)
     * @throws IOException if the error should not be reported to the SOS client (internal server error)
     */
    public void getResults(GetResultRequest req, Subscriber<ObsEvent> consumer) throws SOSException, IOException;


    /**
     * Requests provider to send all selected observations.
     * @param req SOS GetObservation request object
     * @param consumer Consumer that will receive observation objects from the provider
     * @throws SOSException if the error should be reported to the SOS client (client-side error)
     * @throws IOException if the error should not be reported to the SOS client (internal server error)
     */
    public void getObservations(GetObservationRequest req, Subscriber<IObservation> consumer) throws SOSException, IOException;


    /**
     * Requests provider to send all selected procedure descriptions.
     * @param req SOS DescribeSensor request object
     * @param consumer Consumer that will receive procedure objects from the provider
     * @throws SOSException if the error should be reported to the SOS client (client-side error)
     * @throws IOException if the error should not be reported to the SOS client (internal server error)
     */
    public void getProcedureDescriptions(DescribeSensorRequest req, Subscriber<IProcedure> consumer) throws SOSException, IOException;


    /**
     * Requests provider to send all selected features of interest.
     * @param req SOS GetFeatureOfInterest request object
     * @param consumer Consumer that will receive feature objects from the provider
     * @throws SOSException if the error should be reported to the SOS client (client-side error)
     * @throws IOException if the error should not be reported to the SOS client (internal server error)
     */
    public void getFeaturesOfInterest(GetFeatureOfInterestRequest req, Subscriber<IFeature> consumer) throws SOSException, IOException;


    /**
     * @return true if this provider aggregates data from several producers
     */
    public boolean hasMultipleProducers();
    
    
    /**
     * Gets the timeout after which the connection should be closed if no more records
     * are provided 
     * @return Recommended timeout in milliseconds
     */
    public long getTimeout();


    /**
     * Properly releases all resources accessed by provider
     * (for instance, when connection is ended by client)
     */
    public void close();

}
