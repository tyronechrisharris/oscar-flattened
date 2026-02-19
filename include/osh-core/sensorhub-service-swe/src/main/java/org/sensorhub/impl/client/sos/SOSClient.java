/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.client.sos;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeException;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.sensorhub.api.common.SensorHubException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.cdm.common.DataStreamParser;
import org.vast.ogc.om.IObservation;
import org.vast.ogc.om.OMUtils;
import org.vast.ows.OWSExceptionReader;
import org.vast.ows.sos.GetObservationRequest;
import org.vast.ows.sos.GetResultRequest;
import org.vast.ows.sos.GetResultTemplateRequest;
import org.vast.ows.sos.GetResultTemplateResponse;
import org.vast.ows.sos.SOSUtils;
import org.vast.ows.swe.DescribeSensorRequest;
import org.vast.sensorML.SMLUtils;
import org.vast.swe.SWEHelper;
import org.vast.util.Asserts;
import org.vast.util.TimeExtent;
import org.vast.xml.DOMHelper;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import net.opengis.sensorml.v20.AbstractProcess;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;


/**
 * <p>
 * Implementation of an SOS client that connects to a remote SOS to download
 * real-time observations and make them available on the local node as data
 * events.<br/>
 * </p>
 *
 * @author Alex Robin
 * @since Aug 25, 2015
 */
public class SOSClient
{
	/**
	 * When not specified in the constructor, wait this amount of time (60 seconds) when establishing HTTP connections
	 * to the remote server.
	 */
	public static final long DEFAULT_CONNECT_TIMEOUT = 60_000L;
	
	/**
	 * When not specified in the constructor, limit the maximum number of reconnection attempts to this value. In
	 * this case, the value is "-1", indicating that there is no limit to the number of attempts.
	 */
	public static final int DEFAULT_MAX_RETRY_ATTEMPTS = -1;
	
	/**
	 * When not specified in the constructor, wait this amount of time after a failed connection before attempting to
	 * reconnect to the remote server. The value here is 1 seconds (1000 milliseconds).
	 */
	public static final long DEFAULT_RECONNECT_DELAY = 1_000L;
	
    protected static final Logger log = LoggerFactory.getLogger(SOSClient.class);

    SOSUtils sosUtils = new SOSUtils();
    GetResultRequest grRequest;
    WebSocketClient wsClient;
    DataComponent dataDescription;
    DataEncoding dataEncoding;
    boolean useWebsockets;
    
    /**
     * Background threads that perform asynchronous work for this client. Right now this is only used when streaming
     * observations to a caller.
     */
    ScheduledThreadPoolExecutor backgroundThreads;
    
    /**
     * Counts the number of times that we have attempted to reconnect to the remote server for streaming observations.
     * This is set to zero when streaming is started and incremented every time that the connection fails and we try to
     * reconnect.
     */
    int streamingRetryAttempts;
    
    /**
     * How long should we wait for an initial connection when streaming over plain HTTP, in milliseconds.
     */
    long connectTimeout;

    /**
     * The maximum number of times that we should attempt to reconnect to the remote server when streaming observations
     * and the connection fails. If the value is negative, then there is no limit to the number of reconnect attempts.
     * If the value is zero, do not attempt to reconnect at all.
     * 
     * Note that authentication/authorization failures result in an immediate stop rather than a retry.
     */
    int maxStreamingRetryAttempts;
    
    /**
     * Delay in milliseconds, after streaming observations have failed, before this class will attempt to reconnect to
     * the remote server. Some delay is necessary to avoid rapid reconnection attempts that might overload network or
     * CPU resources.
     */
    long delayBetweenStreamingRetryAttempts;
    
    /**
     * True if we are streaming observations to a listener right now.
     */
    volatile boolean streamingStarted;
    
    /**
     * Interface to be implemented by callers that want to receive streaming observation records.
     */
    public interface StreamingListener {
    	/**
    	 * Invoked when a new data block has been received from the server. This will be called in a background thread.
    	 */
        public void recordReceived(DataBlock data);
        
        /**
         * Invoked when streaming has stopped.
         */
        public void stopped(StreamingStopReason reason, Throwable cause);
    }
    
    /**
     * Enumeration of reasons why streaming has stopped.
     */
    public enum StreamingStopReason {
    	/**
    	 * Streaming was stopped at the callers request.
    	 */
    	REQUESTED,
    	
    	/**
    	 * Streaming was stopped due to an unexpected Java exception performing the HTTP request, or during invocation
    	 * of the caller-supplied handler.
    	 */
    	EXCEPTION,
    	
    	/**
    	 * Streaming was stopped because we exceeded the maximum number of retry attempts.
    	 */
    	EXCEEDED_MAX_RETRIES
    }


    public SOSClient(GetResultRequest request, boolean useWebsockets)
    {
    	this(request, useWebsockets, DEFAULT_CONNECT_TIMEOUT, DEFAULT_MAX_RETRY_ATTEMPTS, DEFAULT_RECONNECT_DELAY);
    }
    
    public SOSClient(GetResultRequest request, boolean useWebsockets, long connectTimeout, int maxStreamingRetryAttempts, long delayBetweenStreamingRetryAttempts)
    {
    	Asserts.checkNotNull(request, "GetResultRequest parameter must not be null");
    	Asserts.checkArgument(connectTimeout > 0, "Connect timeout must be positive");
    	Asserts.checkArgument(delayBetweenStreamingRetryAttempts > 0, "Reconnect delay must be positive");

    	this.grRequest = request;
        this.useWebsockets = useWebsockets;
        this.connectTimeout = connectTimeout;
        this.maxStreamingRetryAttempts = maxStreamingRetryAttempts;
        this.delayBetweenStreamingRetryAttempts = delayBetweenStreamingRetryAttempts;

        backgroundThreads = createNewExecutor();
    }

    public boolean isUseWebsockets() {
		return useWebsockets;
	}

	public int getStreamingRetryAttempts() {
		return streamingRetryAttempts;
	}

	public int getMaxStreamingRetryAttempts() {
		return maxStreamingRetryAttempts;
	}

	public long getDelayBetweenStreamingRetryAttempts() {
		return delayBetweenStreamingRetryAttempts;
	}

	public boolean isStreamingStarted() {
		return streamingStarted;
	}

	/**
     * Retrieves a description of the sensor from the remote server. Requests the description in the default format
     * (SensorML XML). The connection is performed synchronously.
     *
     * @param sensorUID
     * @return
     * @throws SensorHubException
     */
    public AbstractProcess getSensorDescription(String sensorUID) throws SensorHubException
    {
        return getSensorDescription(sensorUID, DescribeSensorRequest.DEFAULT_FORMAT);
    }
    
    /**
     * Retrieves a description of the sensor from the remote server, requesting the description in the provided format.
     * The connection is performed synchronously.
     * 
     * @param sensorUID
     * @param format
     * @return
     * @throws SensorHubException
     */
    public AbstractProcess getSensorDescription(String sensorUID, String format) throws SensorHubException
    {
        Asserts.checkNotNull(sensorUID, "sensorUID");
        Asserts.checkNotNull(format, "format");
        
        try
        {
            DescribeSensorRequest req = new DescribeSensorRequest();
            req.setGetServer(grRequest.getGetServer());
            req.setVersion(grRequest.getVersion());
            req.setProcedureID(sensorUID);
            req.setFormat(format);
            
            InputStream is = sosUtils.sendGetRequest(req).getInputStream();
            DOMHelper dom = new DOMHelper(new BufferedInputStream(is), false);
            OWSExceptionReader.checkException(dom, dom.getBaseElement());
            Element smlElt = dom.getElement("description/SensorDescription/data/*");
            
            SMLUtils smlUtils;
            if (format.equals(DescribeSensorRequest.FORMAT_SML_V1) ||
                format.equals(DescribeSensorRequest.FORMAT_SML_V1_01))
            {
                smlUtils = new SMLUtils(SMLUtils.V1_0);
                if ("SensorML".equals(smlElt.getLocalName()))
                    smlElt = dom.getElement(smlElt, "member/*");
            }
            else if (format.equals(DescribeSensorRequest.FORMAT_SML_V2) ||
                format.equals(DescribeSensorRequest.FORMAT_SML_V2_1))
            {
                smlUtils = new SMLUtils(SMLUtils.V2_0);
            }
            else
                throw new SensorHubException("Unsupported response format: " + format);
            
            AbstractProcess smlDesc = smlUtils.readProcess(dom, smlElt);
            log.debug("Retrieved sensor description for sensor {}", sensorUID);
            
            return smlDesc;
        }
        catch (Exception e)
        {
            throw new SensorHubException("Cannot fetch SensorML description for sensor " + sensorUID, e);
        }
    }
    
    /**
     * Calls the "GetResultTemplate" request on the remote server and uses the return value to initialize member
     * variables that will later be used for decoding streamed observations.
     * 
     * This must be called before {@link #startStream(StreamingListener)}.
     * 
     * Runs synchronously and blocks until the request completes.
     *
     * @throws SensorHubException
     */
    public void retrieveStreamDescription() throws SensorHubException
    {
        // create output definition
        try
        {
            GetResultTemplateRequest req = new GetResultTemplateRequest();
            req.setGetServer(grRequest.getGetServer());
            req.setVersion(grRequest.getVersion());
            req.setOffering(grRequest.getOffering());
            req.getObservables().addAll(grRequest.getObservables());
            GetResultTemplateResponse resp = sosUtils.<GetResultTemplateResponse> sendRequest(req, false);
            this.dataDescription = resp.getResultStructure();
            this.dataEncoding = resp.getResultEncoding();
            log.debug("Retrieved observation result template from {}", sosUtils.buildURLQuery(req));
        }
        catch (Exception e)
        {
            throw new SensorHubException("Error while getting observation result template", e);
        }
    }
    
    
    public Collection<IObservation> getObservations(String sysUID, TimeExtent timeRange) throws SensorHubException
    {
        try
        {
            GetObservationRequest req = new GetObservationRequest();
            req.setGetServer(grRequest.getGetServer());
            req.setVersion(grRequest.getVersion());
            req.setOffering(grRequest.getOffering());
            req.getObservables().addAll(grRequest.getObservables());
            req.getProcedures().add(sysUID);
            req.setTime(timeRange);
            
            // parse observations
            InputStream is = sosUtils.sendGetRequest(req).getInputStream();
            DOMHelper dom = new DOMHelper(new BufferedInputStream(is), false);
            OWSExceptionReader.checkException(dom, dom.getBaseElement());
            NodeList obsElts = dom.getElements("observationData/*");
            
            ArrayList<IObservation> obsList = new ArrayList<>();
            OMUtils omUtils = new OMUtils(OMUtils.V2_0);
            for (int i = 0; i < obsElts.getLength(); i++)
            {
                Element obsElt = (Element)obsElts.item(i);
                IObservation obs = omUtils.readObservation(dom, obsElt);
                obsList.add(obs);
            }
            
            log.debug("Retrieved observations from {}", sosUtils.buildURLQuery(req));
            return obsList;
        }
        catch (Exception e)
        {
            throw new SensorHubException("Error fetching observations", e);
        }
    }

	/**
	 * Starts streaming observations from the server and emitting events to the listener for each record that is
	 * received. Has no effect if we're already currently streaming. All callbacks occur in a background thread.
	 *
	 * @param listener Object whose methods will be called when records are received or the streaming has stopped.
	 */
    public synchronized void startStream(final StreamingListener listener) {
    	Asserts.checkNotNull(dataEncoding, "dataEncoding is null. retrieveStreamDescription must be called before starting streaming.");
    	Asserts.checkNotNull(dataDescription, "dataDescription is null. retrieveStreamDescription must be called before starting streaming.");
    	Asserts.checkNotNull(listener, "The listener provided to startStream(...) cannot be null");

    	if (streamingStarted) {
        	log.debug("startStream(...) called, but we're already started. Ignoring.");
            return;
        } else {
        	log.debug("startStream(...) called. useWebsockets: {}", useWebsockets);
        }
        
        // Prepare a parser to extract the data out of whatever we receive from the server.
        DataStreamParser parser = SWEHelper.createDataParser(dataEncoding);
        parser.setDataComponents(dataDescription);
        parser.setRenewDataBlock(true);

        streamingRetryAttempts = 0;
    	streamingStarted = true;
        if (useWebsockets) {
            connectWithWebSockets(parser, listener);
        } else {
            connectWithPersistentHttp(parser, listener);
        }
    }
    
    /**
     * Close the parser and eat any exceptions after logging them.
     */
    protected void closeParser(final DataStreamParser parser) {
		try {
			if (parser != null) {
				parser.close();
			}
		} catch (Exception e) {
			log.error("Unable to close SWE parser", e);
		}
    }

    /**
     * Run a background thread that does the (blocking) HTTP GetRecord request.
     */
    protected void connectWithPersistentHttp(final DataStreamParser parser, final StreamingListener listener) {
    	backgroundThreads.execute(() -> connectWithPersistentHttpAndCleanUp(parser, listener));
    }
    
    /**
     * Entry point for the background thread that keeps connecting via long polling HTTP GET requests.
     * This just delegates to the worker method below and ensures that the parser gets cleaned up.
     */
    protected void connectWithPersistentHttpAndCleanUp(final DataStreamParser parser, final StreamingListener listener) {
    	try {
    		persistenHttpRetryLoop(parser, listener);
    	} finally {
    		closeParser(parser);
    	}
    }

    /**
     * Loops forever, attempting to do HTTP GET GetRecord requests until streaming is stopped, a fatal error happens,
     * or the max retry attempts are exceeded.
     */
    protected void persistenHttpRetryLoop(final DataStreamParser parser, final StreamingListener listener) {
    	Exception lastException = null;

    	while (streamingStarted) {
    		// First try to open up the HTTP connection for the GetRecord request.
    		boolean connectionSucceeded = false;
    		try {
	            log.debug("Initiating GetRecord request");
	            grRequest.setConnectTimeOut((int) connectTimeout);
	            // The next line will block until the first bytes come back from the server, or the timeout expires.
	            HttpURLConnection conn = sosUtils.sendGetRequest(grRequest);
	            
	            // Some types of errors end up falling through the sendGetRequest call above. For example, any
	            // errors that return HTML, such as 401 and 403 errors. This happens because sendGetRequest calls
	            // tryParseExceptions, which won't find any error text at the right XPaths. So for any non-200 responses
	            // that fall through here, we can go ahead and let the user know (so that the SWEVirtualSensor can stop
	            // itself, for example).
	            int responseCode = conn.getResponseCode();
	            if ((responseCode / 100) != 2) {
	            	// Interrupt ourselves so that the "if (Thread.interrupted())" below will get out.
	            	listener.stopped(StreamingStopReason.EXCEPTION, new IOException("Server returned " + responseCode + " response code."));
	            	Thread.currentThread().interrupt();
	            } else {
		            InputStream is = new BufferedInputStream(conn.getInputStream());
		            parser.setInput(is);
		            connectionSucceeded = true;
	            }
    		} catch (Exception e) {
    			lastException = e;
    			log.error("Uncaught exception attempting to establish a connection for GetRecord request", e);
    		}

    		if (Thread.interrupted()) {
    			// If the thread has been interrupted, then we should just quit immediately because it means that
    			// the thread pool has been destroyed, which should only happen when stopStream is called.
    			log.debug("Thread was interrupted while attempting initial GetRecord. Assuming we've been stopped.");
    			return;
    		}
    		
    		// If the initial connection succeeded, read from it forever until it breaks or until it returns null.
        	if (connectionSucceeded) {
	            try {
	                while (streamingStarted) {
	                	if (Thread.interrupted()) {
	            			// Again, if the thread was interrupted, assume that the client was stopped and quit now.
	            			log.debug("Thread was interrupted while reading. Assuming we've been stopped.");
	            			return;
	            		}

	                	DataBlock data = parser.parseNextBlock();
	                	if (data == null) {
	                		log.debug("Null data block returned. Breaking out of loop to reconnect.");
	                		break;
	                	} else {
	                		log.debug("Received data block from server.");
	                		listener.recordReceived(data);
	                	}
	                }
	            } catch (Exception e) {
	            	lastException = e;
	            	log.error("Error while parsing SOS data stream", e);
	            }
        	}

        	// One last check for interrupted-ness to make sure we don't need to quit now.
        	if (Thread.interrupted()) {
    			log.debug("Thread was interrupted. Assuming we've been stopped.");
    			return;
    		}

        	// If we reach the end of this loop and we're still started, then we may need to restart.
    		if (streamingStarted) {
    			if ((maxStreamingRetryAttempts < 0) || (streamingRetryAttempts < maxStreamingRetryAttempts)) {
    				streamingRetryAttempts++;
					log.debug("Delaying {} milliseconds before reconnect number {}", delayBetweenStreamingRetryAttempts, streamingRetryAttempts);
    				try {
    					Thread.sleep(delayBetweenStreamingRetryAttempts);
    				} catch (InterruptedException ie) {
    					log.info("Interrupted while waiting to retry. Quitting.");
    					listener.stopped(StreamingStopReason.REQUESTED, null);
    					Thread.currentThread().interrupt();
    					break;
    				}
    			} else {
    				log.debug("Maximum retry attempts reached. Quitting.");
    				streamingStarted = false;
    				listener.stopped(StreamingStopReason.EXCEEDED_MAX_RETRIES, lastException);
    				break;
    			}
    		} else {
    			listener.stopped(StreamingStopReason.REQUESTED, null);
    		}
    	} // end of "while (streamingStarted)"
    }

    protected void connectWithWebSockets(final DataStreamParser parser, final StreamingListener listener) {
        String destUri = null;
        try {
            destUri = sosUtils.buildURLQuery(grRequest);
            destUri = destUri.replace("http://", "ws://")
                             .replace("https://", "wss://");
            SOSClientWebSocketListener webSocketListener = new SOSClientWebSocketListener(parser, listener);
            connectWithWebSockets(destUri, webSocketListener);
        } catch (Exception e) {
        	listener.stopped(StreamingStopReason.EXCEPTION, e);
        	closeParser(parser);
        	streamingStarted = false;
        }
    }
    
    private void connectWithWebSockets(String wsUriString, WebSocketListener webSocketListener) throws Exception {
        // init WS client with optional auth
        wsClient = new WebSocketClient();
        PasswordAuthentication auth = Authenticator.requestPasswordAuthentication(grRequest.getGetServer(), null, 0, null, null, null);
        if (auth != null) {
            wsClient.getHttpClient().getAuthenticationStore().addAuthenticationResult(
                    new BasicAuthentication.BasicResult(new URI(wsUriString), auth.getUserName(), new String(auth.getPassword())));
        }

        wsClient.start();
        URI wsUri = new URI(wsUriString);
        log.debug("Asynchronously connecting to {}", wsUriString);
        wsClient.connect(webSocketListener, wsUri);
    }

    private void scheduleWebSocketReconnect(final DataStreamParser parser, final StreamingListener listener) {
    	if (streamingStarted) {
	    	if ((maxStreamingRetryAttempts < 0) || (streamingRetryAttempts < maxStreamingRetryAttempts)) {
	    		log.debug("Scheduling websocket reconnect in {} ms", delayBetweenStreamingRetryAttempts);
				streamingRetryAttempts++;
				backgroundThreads.schedule(() -> connectWithWebSockets(parser, listener), delayBetweenStreamingRetryAttempts, TimeUnit.MILLISECONDS);
	    	} else {
	    		log.debug("Retry limit reached. Not scheduling websocket reconnect.");
	    		listener.stopped(StreamingStopReason.EXCEEDED_MAX_RETRIES, null);
	    		closeParser(parser);
	    	}
    	} else {
    		log.debug("Streaming is stopped. Not scheduling a reconnect.");
    	}
    }

    public synchronized void stopStream()
    {
    	if (streamingStarted) {
	        streamingStarted = false;
	        
	        if (wsClient != null)
	        {
	            try
	            {
	                wsClient.stop();
	            }
	            catch (Exception e)
	            {
	                log.trace("Cannot close websocket client", e);
	            }
	        }
	        // Kill off any existing threads as best we can.
	        // PENDING(CSD): The long polling HTTP client is not interrupted by this call.
	        // So when the request eventually times out after 60 seconds, the thread will
	        // either 
	        backgroundThreads.shutdownNow();
	        backgroundThreads = createNewExecutor();
    	}
    }

    protected ScheduledThreadPoolExecutor createNewExecutor() {
        // HTTP long polling needs 2 threads: one to do the work and another for scheduling restarts.
    	// The Jetty WebSocket client uses its own thread pool.
        return new ScheduledThreadPoolExecutor(2);
    }

    public DataComponent getRecordDescription()
    {
        return dataDescription;
    }


    public DataEncoding getRecommendedEncoding()
    {
        return dataEncoding;
    }
    
    private class SOSClientWebSocketListener extends WebSocketAdapter {
    	private final DataStreamParser parser;
    	private final StreamingListener listener;

    	public SOSClientWebSocketListener(final DataStreamParser parser, final StreamingListener listener) {
        	this.parser = parser;
        	this.listener = listener;
        }

        @Override
		public void onWebSocketConnect(Session sess) {
			super.onWebSocketConnect(sess);
			log.debug("WebSocket connected.");
		}

		@Override
        public void onWebSocketBinary(byte[] payload, int offset, int len) {
            try {
                // Skip if no payload
                if ((payload == null) || (payload.length == 0) || (len == 0)) {
                	log.debug("Received empty websocket payload. Skipping.");
                    return;
                } else {
                	log.debug("Received websocket payload of {} bytes", payload.length);
                }
                
                ByteArrayInputStream is = new ByteArrayInputStream(payload, offset, len);
                parser.setInput(is);
                DataBlock data = parser.parseNextBlock();
                listener.recordReceived(data);
            } catch (IOException e) {
                log.error("Error while parsing websocket packet", e);
                if (streamingStarted) {
                	scheduleWebSocketReconnect(parser, listener);
                }
            }
        }

        @Override
        public void onWebSocketClose(int statusCode, String reason) {
        	log.debug("WebSocket closed. Status: {}. Reason: {}", statusCode, reason);
            if (streamingStarted) {
            	scheduleWebSocketReconnect(parser, listener);
            }
        }

        @Override
        public void onWebSocketError(Throwable cause) {
            log.error("Error connecting to websocket", cause);
            boolean needRestart = true;

            if (cause instanceof UpgradeException) {
            	UpgradeException upgradeException = (UpgradeException) cause;
            	int responseCode = upgradeException.getResponseStatusCode();
            	if ((responseCode == HttpServletResponse.SC_UNAUTHORIZED) || (responseCode == HttpServletResponse.SC_FORBIDDEN)) {
            		needRestart = false;
                	listener.stopped(StreamingStopReason.EXCEPTION, new IOException("Server returned " + responseCode + " response code."));
            	}
            }
            
            if (streamingStarted && needRestart) {
            	scheduleWebSocketReconnect(parser, listener);
            }
        }            
    }
}
