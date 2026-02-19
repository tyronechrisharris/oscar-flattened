/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.swe;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.eclipse.jetty.websocket.api.WebSocketBehavior;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.server.WebSocketServerFactory;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.sensorhub.api.ISensorHub;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.DataStoreException;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.impl.system.SystemDatabaseTransactionHandler;
import org.sensorhub.impl.system.SystemUtils;
import org.sensorhub.impl.system.wrapper.SystemWrapper;
import org.slf4j.Logger;
import org.vast.ows.GetCapabilitiesRequest;
import org.vast.ows.OWSException;
import org.vast.ows.OWSExceptionReport;
import org.vast.ows.OWSRequest;
import org.vast.ows.OWSServiceCapabilities;
import org.vast.ows.OWSUtils;
import org.vast.ows.server.OWSServlet;
import org.vast.ows.sos.InsertSensorResponse;
import org.vast.ows.sos.SOSException;
import org.vast.ows.sos.SOSUtils;
import org.vast.ows.sps.SPSException;
import org.vast.ows.swe.DeleteSensorRequest;
import org.vast.ows.swe.DeleteSensorResponse;
import org.vast.ows.swe.InsertSensorRequest;
import org.vast.ows.swe.UpdateSensorRequest;
import org.vast.ows.swe.UpdateSensorResponse;


/**
 * <p>
 * Base abstract servlet for SOS and SPS
 * </p>
 *
 * @author Alex Robin
 * @since Mar 31, 2021
 */
@SuppressWarnings("serial")
public abstract class SWEServlet extends OWSServlet
{
    public static final String INVALID_RESPONSE_FORMAT = "Unsupported response format: ";
    public static final long GET_CAPS_MIN_REFRESH_PERIOD = 200; // ms
    protected static final String INVALID_WS_REQ_MSG = "Invalid Websocket request: ";
    protected static final String DEFAULT_VERSION = "2.0.0";
    protected static final QName EXT_WS = new QName("websocket");
    static final String SOS_PREFIX = "sos";
    static final String SWES_PREFIX = "swe";
    static final String SOAP_PREFIX = "soap";

    protected final transient SWEService<?> service;
    protected final transient IObsSystemDatabase readDatabase;
    protected final transient IObsSystemDatabase writeDatabase;
    protected final transient SystemDatabaseTransactionHandler transactionHandler;
    
    protected final transient AtomicLong lastGetCapsRequest = new AtomicLong();
    protected transient WebSocketServletFactory wsFactory;
    
    
    protected SWEServlet(SWEService<?> service, OWSUtils owsUtils, Logger log)
    {
        super(owsUtils, log);
        
        this.service = service;
        this.readDatabase = service.getReadDatabase();
        this.writeDatabase = service.getWriteDatabase();
        this.transactionHandler = new SystemDatabaseTransactionHandler(
            service.getParentHub().getEventBus(),
            writeDatabase);
    }
    
    
    public abstract OWSServiceCapabilities updateCapabilities();
    public abstract OWSServiceCapabilities getCapabilities();


    protected void handleRequest(GetCapabilitiesRequest request) throws IOException, OWSException
    {
        // check that version 2.0.0 is supported by client
        if (!request.getAcceptedVersions().isEmpty())
        {
            if (!request.getAcceptedVersions().contains(DEFAULT_VERSION))
                throw new SPSException(SPSException.version_nego_failed_code, "AcceptVersions", null,
                        "Only version " + DEFAULT_VERSION + " is supported by this server");
        }

        // set selected version
        request.setVersion(DEFAULT_VERSION);

        // send async response
        var asyncCtx = request.getHttpRequest().startAsync();
        CompletableFuture.runAsync(() -> {
            
            try
            {
                // fence updater to throttle at max refresh rate
                var now = System.currentTimeMillis();
                while (true)
                {
                    long local = lastGetCapsRequest.get();
                    if (now >= local + GET_CAPS_MIN_REFRESH_PERIOD)
                    {
                        if (lastGetCapsRequest.compareAndSet(local, now))
                        {
                            var capabilities = updateCapabilities();
                            
                            // update operation URLs dynamically if base URL not set in config
                            if (service.getHttpServer().getServerBaseUrl().contains("://localhost"))
                            {
                                String endpointUrl = request.getHttpRequest().getRequestURL().toString();
                                capabilities.updateAllEndpointUrls(endpointUrl);
                            }
                            
                            break;
                        }
                    }
                    else
                        break;
                }            
            
                var os = asyncCtx.getResponse().getOutputStream();
                owsUtils.writeXMLResponse(os, getCapabilities(), request.getVersion(), request.getSoapVersion());
                os.flush();
                asyncCtx.complete();
            }
            catch (Exception e)
            {
                handleError(
                    (HttpServletRequest)asyncCtx.getRequest(),
                    (HttpServletResponse)asyncCtx.getResponse(),
                    request, e);
            }
        }, service.getThreadPool());
    }
    
    
    protected void handleRequest(InsertSensorRequest request) throws IOException, OWSException
    {
        checkTransactionalSupport(request);
        
        // check query parameters
        OWSExceptionReport report = new OWSExceptionReport();
        var smlProc = request.getProcedureDescription();
        TransactionUtils.checkSensorML(smlProc, report);
        report.process();

        // start async response
        AsyncContext asyncCtx = request.getHttpRequest().startAsync();
        CompletableFuture.runAsync(() -> {
            try
            {
                String sysUID = request.getProcedureDescription().getUniqueIdentifier();
                
                // add or replace description in DB
                try
                {
                    var procWrapper = new SystemWrapper(smlProc)
                        .hideOutputs()
                        .hideTaskableParams()
                        .defaultToValidFromNow();
                    
                    var procHandler = transactionHandler.addOrUpdateSystem(procWrapper);
                    getLogger().info("Registered new procedure {}", sysUID);

                    // also add datastreams if outputs are included in SML description
                    SystemUtils.addDatastreamsFromOutputs(procHandler, smlProc.getOutputList());
                    
                    // also add command streams if taskable params are included in SML description
                    SystemUtils.addCommandStreamsFromTaskableParams(procHandler, smlProc.getParameterList());
                    
                    // force capabilities update
                    updateCapabilities();
                }
                catch (DataStoreException e)
                {
                    getLogger().error("Error", e);
                    throw new SOSException(SOSException.invalid_param_code, "procedureDescription", null,
                        "Procedure " + sysUID + " is already registered on this server");
                }

                // build and send response
                InsertSensorResponse resp = new InsertSensorResponse();
                resp.setAssignedOffering(getOfferingID(sysUID));
                resp.setAssignedProcedureId(sysUID);
                sendResponse(request, resp);
                asyncCtx.complete();
            }
            catch (Exception e)
            {
                throw new CompletionException(e);
            }            
        }, service.getThreadPool())
        .exceptionally(ex -> {
            handleError(
                (HttpServletRequest)asyncCtx.getRequest(),
                (HttpServletResponse)asyncCtx.getResponse(),
                request, ex);
            return null;
        });
    }
    
    
    protected void handleRequest(UpdateSensorRequest request) throws IOException, OWSException
    {
        checkTransactionalSupport(request);        
        
        // check query parameters
        String sysUID = request.getProcedureId();
        var procDesc = request.getProcedureDescription();
        OWSExceptionReport report = new OWSExceptionReport();
        checkQueryProcedure(sysUID, report);
        TransactionUtils.checkSensorML(procDesc, report);
        report.process();

        // start async response
        AsyncContext asyncCtx = request.getHttpRequest().startAsync();
        CompletableFuture.runAsync(() -> {
            try
            {
                // version or replace description in DB
                try
                {
                    var procWrapper = new SystemWrapper(request.getProcedureDescription())
                        .hideOutputs()
                        .hideTaskableParams()
                        .defaultToValidFromNow();
                    
                    var procHandler = transactionHandler.getSystemHandler(sysUID);
                    procHandler.update(procWrapper);
                    getLogger().info("Updated procedure {}", sysUID);
                }
                catch (DataStoreException e)
                {
                    throw new IOException("Cannot update procedure", e);
                }
        
                // build and send response
                UpdateSensorResponse resp = new UpdateSensorResponse(SOSUtils.SOS);
                resp.setUpdatedProcedure(sysUID);
                sendResponse(request, resp);
                asyncCtx.complete();
            }
            catch (Exception e)
            {
                throw new CompletionException(e);
            }            
        }, service.getThreadPool())
        .exceptionally(ex -> {
            handleError(
                (HttpServletRequest)asyncCtx.getRequest(),
                (HttpServletResponse)asyncCtx.getResponse(),
                request, ex);
            return null;
        });
    }
    
    
    protected void handleRequest(DeleteSensorRequest request) throws IOException, OWSException
    {
        checkTransactionalSupport(request);
        
        // check query parameters
        String sysUID = request.getProcedureId();
        OWSExceptionReport report = new OWSExceptionReport();
        checkQueryProcedure(sysUID, report);
        report.process();
        
        // start async response
        AsyncContext asyncCtx = request.getHttpRequest().startAsync();
        CompletableFuture.runAsync(() -> {
            try
            {
                // delete complete procedure history + all datastreams and obs from DB
                try
                {
                    transactionHandler.getSystemHandler(sysUID).delete(true);
                    getLogger().info("Deleted procedure {}", sysUID);
                }
                catch (Exception e)
                {
                    throw new IOException("Cannot delete procedure " + sysUID, e);
                }
        
                // build and send response
                DeleteSensorResponse resp = new DeleteSensorResponse(SOSUtils.SOS);
                resp.setDeletedProcedure(sysUID);
                sendResponse(request, resp);
                asyncCtx.complete();
            }
            catch (Exception e)
            {
                throw new CompletionException(e);
            }            
        }, service.getThreadPool())
        .exceptionally(ex -> {
            handleError(
                (HttpServletRequest)asyncCtx.getRequest(),
                (HttpServletResponse)asyncCtx.getResponse(),
                request, ex);
            return null;
        });
    }


    protected void checkTransactionalSupport(OWSRequest request) throws OWSException
    {
        if (!service.getConfiguration().enableTransactional)
            throw new SPSException(SPSException.invalid_param_code, "request", request.getOperation(), request.getOperation() + " operation is not supported on this endpoint");
    }


    protected FeatureKey checkQueryProcedure(String sysUID, OWSExceptionReport report) throws SOSException
    {
        FeatureKey fk = null;
        if (sysUID == null || (fk = readDatabase.getSystemDescStore().getCurrentVersionKey(sysUID)) == null)
            report.add(new SOSException(SOSException.invalid_param_code, "procedure", sysUID, "Unknown procedure: " + sysUID));
        return fk;
    }


    protected void acceptWebSocket(final OWSRequest owsReq, final WebSocketListener socket) throws IOException
    {
        wsFactory.acceptWebSocket(new WebSocketCreator() {
            @Override
            public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp)
            {
                return socket;
            }
        }, owsReq.getHttpRequest(), owsReq.getHttpResponse());
    }
    
    
    public boolean isXmlMimeType(String format)
    {
        return OWSUtils.XML_MIME_TYPE.equals(format) ||
               OWSUtils.XML_MIME_TYPE2.equals(format);
    }


    public void startSoapEnvelope(OWSRequest request, XMLStreamWriter writer) throws XMLStreamException
    {
        String soapUri = request.getSoapVersion();
        if (soapUri != null)
        {
            writer.writeStartElement(SOAP_PREFIX, "Envelope", soapUri);
            writer.writeNamespace(SOAP_PREFIX, soapUri);
            writer.writeStartElement(SOAP_PREFIX, "Body", soapUri);
        }
    }


    public void endSoapEnvelope(OWSRequest request, XMLStreamWriter writer) throws XMLStreamException
    {
        String soapUri = request.getSoapVersion();
        if (soapUri != null)
        {
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }


    protected boolean isWebSocketRequest(OWSRequest request)
    {
        return request.getExtensions().containsKey(EXT_WS);
    }


    public String getProcedureUID(String offeringID)
    {
        // for now, assume offerings have same URI as procedures
        return offeringID;
    }


    public String getOfferingID(String systemUID)
    {
        // for now, assume offerings have same URI as procedures
        return systemUID;
    }
    

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        // create websocket factory
        try
        {
            WebSocketPolicy wsPolicy = new WebSocketPolicy(WebSocketBehavior.SERVER);
            wsFactory = new WebSocketServerFactory(getServletContext(), wsPolicy);
            wsFactory.start();
        }
        catch (Exception e)
        {
            throw new ServletException("Cannot initialize websocket factory", e);
        }
    }


    @Override
    public void destroy()
    {
        // stop websocket factory
        try
        {
            wsFactory.stop();
        }
        catch (Exception e)
        {
            log.error("Cannot stop websocket factory", e);
        }
    }
    

    public IObsSystemDatabase getReadDatabase()
    {
        return readDatabase;
    }


    public IObsSystemDatabase getWriteDatabase()
    {
        return writeDatabase;
    }


    @Override
    public String getDefaultVersion()
    {
        return DEFAULT_VERSION;
    }


    public ISensorHub getParentHub()
    {
        return service.getParentHub();
    }


    public Logger getLogger()
    {
        return log;
    }
}
