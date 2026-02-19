/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.

******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sos;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Flow.Subscription;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import net.opengis.fes.v20.Conformance;
import net.opengis.fes.v20.FilterCapabilities;
import net.opengis.fes.v20.SpatialCapabilities;
import net.opengis.fes.v20.SpatialOperator;
import net.opengis.fes.v20.SpatialOperatorName;
import net.opengis.fes.v20.TemporalCapabilities;
import net.opengis.fes.v20.TemporalOperator;
import net.opengis.fes.v20.TemporalOperatorName;
import net.opengis.fes.v20.impl.FESFactory;
import net.opengis.swe.v20.BinaryBlock;
import net.opengis.swe.v20.BinaryEncoding;
import net.opengis.swe.v20.BinaryMember;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.api.datastore.obs.DataStreamFilter;
import org.sensorhub.api.datastore.obs.DataStreamKey;
import org.sensorhub.api.security.ISecurityManager;
import org.sensorhub.impl.module.ModuleRegistry;
import org.sensorhub.impl.service.ogc.OGCServiceConfig.CapabilitiesInfo;
import org.sensorhub.impl.service.swe.RecordTemplate;
import org.sensorhub.impl.service.swe.SWEServlet;
import org.sensorhub.impl.system.DataStreamTransactionHandler;
import org.sensorhub.utils.DataComponentChecks;
import org.sensorhub.utils.Lambdas;
import org.sensorhub.utils.SWEDataUtils;
import org.slf4j.Logger;
import org.vast.cdm.common.DataSource;
import org.vast.cdm.common.DataStreamParser;
import org.vast.ogc.gml.GeoJsonBindings;
import org.vast.ogc.om.IObservation;
import org.vast.ogc.om.SamplingPoint;
import org.vast.ows.GetCapabilitiesRequest;
import org.vast.ows.OWSException;
import org.vast.ows.OWSExceptionReport;
import org.vast.ows.OWSRequest;
import org.vast.ows.OWSUtils;
import org.vast.ows.sos.*;
import org.vast.ows.swe.DeleteSensorRequest;
import org.vast.ows.swe.DescribeSensorRequest;
import org.vast.ows.swe.SWESOfferingCapabilities;
import org.vast.ows.swe.UpdateSensorRequest;
import org.vast.swe.DataSourceDOM;
import org.vast.swe.SWEHelper;
import org.vast.util.ReaderException;
import org.vast.util.TimeExtent;


/**
 * <p>
 * Extension of SOSServlet deployed as a SensorHub service
 * </p>
 *
 * @author Alex Robin
 * @since Sep 7, 2013
 */
@SuppressWarnings("serial")
public class SOSServlet extends SWEServlet
{
    static final String DEFAULT_PROVIDER_KEY = "%%%_DEFAULT_";
    static final char TEMPLATE_ID_SEPARATOR = '#';
    static final Pattern TEMPLATE_ID_REGEX = Pattern.compile("(.+)" + TEMPLATE_ID_SEPARATOR + "(.+)");
    
    final transient SOSServiceConfig config;
    final transient SOSSecurity securityHandler;
    final transient CapabilitiesUpdater capsUpdater;
    final transient SOSServiceCapabilities capabilities = new SOSServiceCapabilities();
    final transient NavigableMap<String, SOSProviderConfig> providerConfigs;
    final transient Map<String, SOSCustomFormatConfig> customFormats = new HashMap<>();
    final transient Map<String, Class<ISOSAsyncResultSerializer>> customSerializers = new HashMap<>();


    protected SOSServlet(SOSService service, SOSSecurity securityHandler, Logger log) throws SensorHubException
    {
        super(service, new SOSUtils(), log);

        this.config = service.getConfiguration();
        this.securityHandler = securityHandler;
        this.capsUpdater = new CapabilitiesUpdater();
        
        this.providerConfigs = new TreeMap<>();
        for (var config: service.getConfiguration().customDataProviders)
            providerConfigs.put(config.systemUID, config);

        generateCapabilities();
    }


    /**
     * Generates the SOSServiceCapabilities object with info from data source
     */
    protected void generateCapabilities() throws SensorHubException
    {
        customFormats.clear();

        // get main capabilities info from config
        CapabilitiesInfo serviceInfo = config.ogcCapabilitiesInfo;
        capabilities.getSupportedVersions().add(DEFAULT_VERSION);
        capabilities.getIdentification().setTitle(serviceInfo.title);
        capabilities.getIdentification().setDescription(serviceInfo.description);
        capabilities.setFees(serviceInfo.fees);
        capabilities.setAccessConstraints(serviceInfo.accessConstraints);
        capabilities.setServiceProvider(serviceInfo.serviceProvider);

        // supported operations and extensions
        String endpoint = service.getHttpServer().getPublicEndpointUrl(config.endPoint);
        capabilities.getProfiles().add(SOSServiceCapabilities.PROFILE_RESULT_RETRIEVAL);
        capabilities.getProfiles().add(SOSServiceCapabilities.PROFILE_OMXML);
        capabilities.getGetServers().put("GetCapabilities", endpoint);
        capabilities.getGetServers().put("DescribeSensor", endpoint);
        capabilities.getGetServers().put("GetFeatureOfInterest", endpoint);
        capabilities.getGetServers().put("GetObservation", endpoint);
        capabilities.getGetServers().put("GetResult", endpoint);
        capabilities.getGetServers().put("GetResultTemplate", endpoint);
        capabilities.getPostServers().putAll(capabilities.getGetServers());

        if (config.enableTransactional)
        {
            capabilities.getProfiles().add(SOSServiceCapabilities.PROFILE_SENSOR_INSERTION);
            capabilities.getProfiles().add(SOSServiceCapabilities.PROFILE_SENSOR_DELETION);
            capabilities.getProfiles().add(SOSServiceCapabilities.PROFILE_OBS_INSERTION);
            capabilities.getProfiles().add(SOSServiceCapabilities.PROFILE_RESULT_INSERTION);
            capabilities.getPostServers().put("InsertSensor", endpoint);
            capabilities.getPostServers().put("DeleteSensor", endpoint);
            capabilities.getPostServers().put("InsertObservation", endpoint);
            capabilities.getPostServers().put("InsertResultTemplate", endpoint);
            capabilities.getPostServers().put("InsertResult", endpoint);
            capabilities.getGetServers().put("InsertResult", endpoint);

            // insertion capabilities
            SOSInsertionCapabilities insertCaps = new SOSInsertionCapabilities();
            insertCaps.getProcedureFormats().add(SWESOfferingCapabilities.FORMAT_SML2);
            insertCaps.getFoiTypes().add(SamplingPoint.TYPE);
            insertCaps.getObservationTypes().add(IObservation.OBS_TYPE_SCALAR);
            insertCaps.getObservationTypes().add(IObservation.OBS_TYPE_RECORD);
            insertCaps.getObservationTypes().add(IObservation.OBS_TYPE_ARRAY);
            insertCaps.getSupportedEncodings().add(SOSServiceCapabilities.SWE_ENCODING_TEXT);
            insertCaps.getSupportedEncodings().add(SOSServiceCapabilities.SWE_ENCODING_BINARY);
            capabilities.setInsertionCapabilities(insertCaps);
        }

        // filter capabilities
        FESFactory fac = new FESFactory();
        FilterCapabilities filterCaps = fac.newFilterCapabilities();
        capabilities.setFilterCapabilities(filterCaps);

        // conformance
        Conformance filterConform = filterCaps.getConformance();
        filterConform.addConstraint(fac.newConstraint("ImplementsQuery", Boolean.TRUE.toString()));
        filterConform.addConstraint(fac.newConstraint("ImplementsAdHocQuery", Boolean.FALSE.toString()));
        filterConform.addConstraint(fac.newConstraint("ImplementsFunctions", Boolean.FALSE.toString()));
        filterConform.addConstraint(fac.newConstraint("ImplementsResourceld", Boolean.FALSE.toString()));
        filterConform.addConstraint(fac.newConstraint("ImplementsMinStandardFilter", Boolean.FALSE.toString()));
        filterConform.addConstraint(fac.newConstraint("ImplementsStandardFilter", Boolean.FALSE.toString()));
        filterConform.addConstraint(fac.newConstraint("ImplementsMinSpatialFilter", Boolean.TRUE.toString()));
        filterConform.addConstraint(fac.newConstraint("ImplementsSpatialFilter", Boolean.FALSE.toString()));
        filterConform.addConstraint(fac.newConstraint("ImplementsMinTemporalFilter", Boolean.TRUE.toString()));
        filterConform.addConstraint(fac.newConstraint("ImplementsTemporalFilter", Boolean.FALSE.toString()));
        filterConform.addConstraint(fac.newConstraint("ImplementsVersionNav", Boolean.FALSE.toString()));
        filterConform.addConstraint(fac.newConstraint("ImplementsSorting", Boolean.FALSE.toString()));
        filterConform.addConstraint(fac.newConstraint("ImplementsExtendedOperators", Boolean.FALSE.toString()));
        filterConform.addConstraint(fac.newConstraint("ImplementsMinimumXPath", Boolean.FALSE.toString()));
        filterConform.addConstraint(fac.newConstraint("ImplementsSchemaElementFunc", Boolean.FALSE.toString()));

        // supported temporal filters
        TemporalCapabilities timeFilterCaps = fac.newTemporalCapabilities();
        timeFilterCaps.getTemporalOperands().add(new QName(null, "TimeInstant", "gml"));
        timeFilterCaps.getTemporalOperands().add(new QName(null, "TimePeriod", "gml"));
        TemporalOperator timeOp = fac.newTemporalOperator();
        timeOp.setName(TemporalOperatorName.DURING);
        timeFilterCaps.getTemporalOperators().add(timeOp);
        filterCaps.setTemporalCapabilities(timeFilterCaps);

        // supported spatial filters
        SpatialCapabilities spatialFilterCaps = fac.newSpatialCapabilities();
        spatialFilterCaps.getGeometryOperands().add(new QName(null, "Envelope", "gml"));
        SpatialOperator spatialOp = fac.newSpatialOperator();
        spatialOp.setName(SpatialOperatorName.BBOX);
        spatialFilterCaps.getSpatialOperators().add(spatialOp);
        filterCaps.setSpatialCapabilities(spatialFilterCaps);

        // build map of custom format serializers
        for (SOSCustomFormatConfig format: config.customFormats)
            customFormats.put(format.mimeType, format);
        
        // update offerings
        updateCapabilities();
    }


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        // set current authentified user
        String userID = ISecurityManager.ANONYMOUS_USER;
        if (req.getRemoteUser() != null)
            userID = req.getRemoteUser();

        try
        {
            // set current authentified user
            securityHandler.setCurrentUser(userID);
            
            // reject request early if SOS not authorized at all
            securityHandler.checkPermission(securityHandler.rootPerm);
            
            // check if we have an upgrade request for websockets
            if (wsFactory.isUpgradeRequest(req, resp))
            {
                // parse request
                try
                {
                    OWSRequest owsReq = this.parseRequest(req, resp, false);

                    if (owsReq != null)
                    {
                        owsReq.getExtensions().put(SOSProviderUtils.EXT_WS, true);

                        if (owsReq instanceof GetResultRequest)
                        {
                            acceptWebSocket(owsReq, new SOSWebSocketOut(this, owsReq, userID, log));
                        }
                        else if (owsReq instanceof InsertResultRequest)
                        {
                            this.handleRequest(owsReq);
                        }
                        else
                            throw new ServletException(INVALID_WS_REQ_MSG + owsReq.getOperation() + " is not supported via this protocol");
                    }
                }
                catch (Exception e)
                {
                    String errorMsg = "Error while processing Websocket request";
                    resp.sendError(400, errorMsg);
                    log.trace(errorMsg, e);
                }

                return;
            }

            // otherwise process as classical HTTP request            
            super.service(req, resp);
        }
        catch (Throwable e)
        {
            handleError(req, resp, null, e);
        }
        finally
        {
            securityHandler.clearCurrentUser();
        }
    }
    
    
    @Override
    public void handleRequest(OWSRequest request) throws IOException, OWSException
    {
        // core operations
        if (request instanceof GetCapabilitiesRequest)
            handleRequest((GetCapabilitiesRequest)request);
        else if (request instanceof DescribeSensorRequest)
            handleRequest((DescribeSensorRequest)request);
        else if (request instanceof GetFeatureOfInterestRequest)
            handleRequest((GetFeatureOfInterestRequest)request);
        else if (request instanceof GetObservationRequest)
            handleRequest((GetObservationRequest)request);
        
        // result retrieval
        else if (request instanceof GetResultRequest)
            handleRequest((GetResultRequest)request);
        else if (request instanceof GetResultTemplateRequest)
            handleRequest((GetResultTemplateRequest)request);
        
        // transactional methods
        else if (request instanceof InsertSensorRequest)
            handleRequest((InsertSensorRequest)request);
        else if (request instanceof UpdateSensorRequest)
            handleRequest((UpdateSensorRequest)request);
        else if (request instanceof DeleteSensorRequest)
            handleRequest((DeleteSensorRequest)request);
        else if (request instanceof InsertObservationRequest)
            handleRequest((InsertObservationRequest)request);
        else if (request instanceof InsertResultRequest)
            handleRequest((InsertResultRequest)request);
        else if (request instanceof InsertResultTemplateRequest)
            handleRequest((InsertResultTemplateRequest)request);
    }


    @Override
    protected void handleRequest(GetCapabilitiesRequest request) throws IOException, OWSException
    {
        // security check
        securityHandler.checkPermission(securityHandler.sos_read_caps);        
        super.handleRequest(request);
    }
    
    
    @Override
    public SOSServiceCapabilities updateCapabilities()
    {
        capsUpdater.updateOfferings(this);
        getLogger().debug("Updating capabilities");
        return capabilities;
    }


    protected void handleRequest(DescribeSensorRequest request) throws IOException, OWSException
    {
        // security check
        securityHandler.checkPermission(securityHandler.sos_read_sensor);
        
        // check query parameters
        String procUID = request.getProcedureID();
        OWSExceptionReport report = new OWSExceptionReport();
        checkQueryProcedure(procUID, report);
        checkQueryTime(request.getTime(), report);
        
        // choose serializer according to output format
        String format = request.getFormat();
        ISOSAsyncProcedureSerializer serializer = null;
        if (format == null || isXmlMimeType(format) || SWESOfferingCapabilities.FORMAT_SML2.equals(format))
            serializer = new ProcedureSerializerXml();
        else if (SWESOfferingCapabilities.FORMAT_SML2_JSON.equals(format) ||
            OWSUtils.JSON_MIME_TYPE.equals(format))
            serializer = new ProcedureSerializerJson();
        else
            report.add(new SOSException(SOSException.invalid_param_code, "procedureDescriptionFormat",
                format, INVALID_RESPONSE_FORMAT + format));
        report.process();
        
        // serializer should never be null here because report.process()
        // should throw an exception but just in case
        if (serializer != null)
        {
            // create data provider
            var dataProvider = getDataProvider(procUID, request);
    
            // start async response
            AsyncContext asyncCtx = request.getHttpRequest().startAsync();
            serializer.init(this, asyncCtx, request);
            dataProvider.getProcedureDescriptions(request, serializer);
        }
    }


    protected void handleRequest(GetObservationRequest request) throws IOException, OWSException
    {
        // security check
        securityHandler.checkPermission(securityHandler.sos_read_obs);

        // build procedure UID set
        // offerings have same URI as systems now so we can just merge everything
        OWSExceptionReport report = new OWSExceptionReport();
        Set<String> selectedProcedures = new HashSet<>();
        selectedProcedures.addAll(request.getProcedures());
        if (selectedProcedures.isEmpty())
            selectedProcedures.addAll(request.getOfferings());
        else if (!request.getOfferings().isEmpty())
            selectedProcedures.retainAll(request.getOfferings());
        checkQueryProcedures(selectedProcedures, report);
        
        // choose serializer according to output format
        String format = request.getFormat();
        ISOSAsyncObsSerializer serializer = null;
        if (format == null || isXmlMimeType(format))
            serializer = new ObsSerializerXml();
        else if (OWSUtils.JSON_MIME_TYPE.equals(format))
            serializer = new ObsSerializerJson();
        else
            report.add(new SOSException(SOSException.invalid_param_code, "responseFormat",
                format, INVALID_RESPONSE_FORMAT + format));
        report.process();
        
        // serializer should never be null here because report.process()
        // should throw an exception but just in case
        if (serializer != null)
        {
            // get all selected providers
            var dataProviders = getDataProviders(selectedProcedures, request);
            
            // start async response
            final AsyncContext asyncCtx = request.getHttpRequest().startAsync();
                    
            // retrieve and serialize obs collection from each provider in sequence
            serializer.init(this, asyncCtx, request);
            request.getProcedures().addAll(selectedProcedures);
            new GetObsMultiProviderSubscriber(dataProviders, request, serializer).start();
        }
    }



    protected void handleRequest(GetResultTemplateRequest request) throws IOException, OWSException
    {
        // security check
        securityHandler.checkPermission(securityHandler.sos_read_obs);
        
        // check query parameters
        OWSExceptionReport report = new OWSExceptionReport();
        checkQueryOffering(request.getOffering(), report);

        // choose serializer according to output format
        String format = request.getFormat();
        ISOSAsyncResultTemplateSerializer serializer;
        if (format == null || isXmlMimeType(format))
            serializer = new ResultTemplateSerializerXml();
        else if (OWSUtils.JSON_MIME_TYPE.equals(format))
            serializer = new ResultTemplateSerializerJson();
        else
        {
            serializer = null;
            report.add(new SOSException(SOSException.invalid_param_code, "responseFormat",
                format, INVALID_RESPONSE_FORMAT + format));
        }
        report.process(); // will throw exception it report contains one
        
        // serializer should never be null here because report.process()
        // should throw an exception but just in case
        if (serializer != null)
        {
            // get data provider
            var procUID = getProcedureUID(request.getOffering());
            ISOSAsyncDataProvider dataProvider = getDataProvider(procUID, request);
            
            // start async response
            final AsyncContext asyncCtx = request.getHttpRequest().startAsync();
            
            // fetch result template
            serializer.init(this, asyncCtx, request);
            dataProvider.getResultTemplate(request).thenAccept(resultTemplate -> {
                try
                {
                    // create simple subscription
                    serializer.onSubscribe(new Subscription() {
                        boolean done = false;
                        public void request(long n)
                        {
                            if (!done)
                            {
                                // send back result template synchronously
                                done = true;
                                serializer.onNext(resultTemplate);
                                serializer.onComplete();
                            }
                        }
    
                        @Override
                        public void cancel()
                        {
                        }
                    });
                }
                catch (Exception e)
                {
                    throw new CompletionException(e);
                }
            }).exceptionally(ex -> {
                handleError(
                    (HttpServletRequest)asyncCtx.getRequest(),
                    (HttpServletResponse)asyncCtx.getResponse(),
                    request, ex);
                return null;
            });
        }
    }


    protected void handleRequest(GetResultRequest request) throws IOException, OWSException
    {
        // security check
        securityHandler.checkPermission(securityHandler.sos_read_obs);
        
        // check query parameters
        OWSExceptionReport report = new OWSExceptionReport();
        checkQueryOffering(request.getOffering(), report);
        checkQueryTime(request.getTime(), report);
        report.process();

        // create data provider
        String procUID = getProcedureUID(request.getOffering());
        ISOSAsyncDataProvider dataProvider = getDataProvider(procUID, request);
        
        // create GetResultTemplate request
        var grt = new GetResultTemplateRequest();
        grt.setOffering(request.getOffering());
        grt.setObservables(request.getObservables());
        
        // start async or websocket response
        final AsyncContext asyncCtx;
        if (!SOSProviderUtils.isWebSocketRequest(request))
        {
            asyncCtx = request.getHttpRequest().startAsync();
            
            // disable async timeout to allow long-lived streaming connections 
            if (dataProvider instanceof StreamingDataProvider ||
                SOSProviderUtils.isReplayRequest(request))
                asyncCtx.setTimeout(0);
        }
        else
            asyncCtx = null;
        
        // fetch result template, then process record stream asychronously
        dataProvider.getResultTemplate(grt).thenAccept(resultTemplate -> {
            try
            {
                // choose serializer according to output format
                String format = request.getFormat();
                ISOSAsyncResultSerializer serializer;
                if (isXmlMimeType(format))
                    serializer = new ResultSerializerXml();
                else if (OWSUtils.TEXT_MIME_TYPE.equals(format))
                    serializer = new ResultSerializerText();
                else if (OWSUtils.JSON_MIME_TYPE.equals(format))
                    serializer = new ResultSerializerJson();
                else if (OWSUtils.BINARY_MIME_TYPE.equals(format))
                    serializer = new ResultSerializerBinary();
                else
                    serializer = getCustomFormatSerializer(request, resultTemplate);
                
                // if no specific format was requested or auto-detected, use default
                if (serializer == null)
                    serializer = getDefaultSerializer(request, resultTemplate);

                // subscribe and stream results asynchronously
                serializer.init(this, asyncCtx, request, resultTemplate);
                dataProvider.getResults(request, serializer);
            }
            catch (Exception e)
            {
                throw new CompletionException(e);
            }
        }).exceptionally(ex -> {
            handleError(
                (HttpServletRequest)asyncCtx.getRequest(),
                (HttpServletResponse)asyncCtx.getResponse(),
                request, ex);
            return null;
        });
    }


    protected void handleRequest(final GetFeatureOfInterestRequest request) throws IOException, OWSException
    {
        // security check
        securityHandler.checkPermission(securityHandler.sos_read_foi);
        
        // check query parameters
        OWSExceptionReport report = new OWSExceptionReport();
        checkQueryProcedures(request.getProcedures(), report);
        
        // choose serializer according to output format
        String format = request.getFormat();
        ISOSAsyncFeatureSerializer serializer = null;
        if (format == null || isXmlMimeType(format))
            serializer = new FeatureSerializerGml();
        else if (GeoJsonBindings.MIME_TYPE.equals(format) || OWSUtils.JSON_MIME_TYPE.equals(format))
            serializer = new FeatureSerializerGeoJson();
        else
            report.add(new SOSException(SOSException.invalid_param_code, "responseFormat",
                format, INVALID_RESPONSE_FORMAT + format));
        report.process();
        
        // serializer should never be null here because report.process()
        // should throw an exception but just in case
        if (serializer != null)
        {
            // create data provider
            ISOSAsyncDataProvider dataProvider = getDefaultDataProvider(request);
            
            // start async response
            AsyncContext asyncCtx = request.getHttpRequest().startAsync();
            serializer.init(this, asyncCtx, request);
            dataProvider.getFeaturesOfInterest(request, serializer);
        }
    }


    @Override
    protected void handleRequest(org.vast.ows.swe.InsertSensorRequest request) throws IOException, OWSException
    {
        securityHandler.checkPermission(securityHandler.sos_insert_sensor);
        super.handleRequest(request);
    }


    @Override
    protected void handleRequest(UpdateSensorRequest request) throws IOException, OWSException
    {
        securityHandler.checkPermission(securityHandler.sos_update_sensor);
        super.handleRequest(request);
    }


    @Override
    protected void handleRequest(DeleteSensorRequest request) throws IOException, OWSException
    {
        securityHandler.checkPermission(securityHandler.sos_delete_sensor);
        super.handleRequest(request);
    }


    protected void handleRequest(InsertObservationRequest request) throws IOException, OWSException
    {
        checkTransactionalSupport(request);

        // security check
        securityHandler.checkPermission(securityHandler.sos_insert_obs);

        // TODO send new observation
        throw new SOSException(SOSException.invalid_request_code, null, null,
            "InsertObservation not supported yet. Please use InsertResult.");

        // build and send response
        /*InsertObservationResponse resp = new InsertObservationResponse();
        sendResponse(request, resp);*/
    }


    protected void handleRequest(InsertResultTemplateRequest request) throws IOException, OWSException
    {
        checkTransactionalSupport(request);

        // security check
        securityHandler.checkPermission(securityHandler.sos_insert_sensor);
        
        // check query parameters
        String procUID = getProcedureUID(request.getOffering());
        OWSExceptionReport report = new OWSExceptionReport();
        checkQueryProcedure(procUID, report);
        report.process();
        
        var resultStruct = request.getResultStructure();
        var resultEncoding = request.getResultEncoding();
        
        // start async response
        AsyncContext asyncCtx = request.getHttpRequest().startAsync();
        CompletableFuture.runAsync(() -> {
            try
            {
                // retrieve transaction helper
                var procHandler = transactionHandler.getSystemHandler(procUID);
                var sysID = procHandler.getSystemKey().getInternalID();
                
                // get existing datastreams of this procedure
                var outputs = writeDatabase.getDataStreamStore().selectEntries(new DataStreamFilter.Builder()
                        .withSystems(sysID)
                        .withCurrentVersion()
                        .build())
                    .collect(Collectors.toMap(
                        dsEntry -> dsEntry.getValue().getOutputName(),
                        dsEntry -> dsEntry));
                
                // generate or use existing output name
                String outputName = null;
                var existingOutput = findCompatibleDatastream(resultStruct, resultEncoding, outputs.values());
                if (existingOutput != null)
                {
                    outputName = existingOutput.getValue().getOutputName();
                    getLogger().info("Found existing output {} on procedure {}", outputName, procUID);
                }
                else
                {
                    outputName = generateOutputName(resultStruct, outputs.size());
                    getLogger().info("Adding new output {} to procedure {}", outputName, procUID);
                }
                
                // add or update datastream
                resultStruct.setName(outputName);
                procHandler.addOrUpdateDataStream(outputName, resultStruct, resultEncoding);
                
                // build and send response
                String templateID = generateTemplateID(procUID, outputName);
                InsertResultTemplateResponse resp = new InsertResultTemplateResponse();
                resp.setAcceptedTemplateId(templateID);
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


    protected void handleRequest(InsertResultRequest request) throws IOException, OWSException
    {
        checkTransactionalSupport(request);
        
        // security check
        securityHandler.checkPermission(securityHandler.sos_insert_obs);
        
        // retrieve datastream info
        var templateID = request.getTemplateId();
        var dataStreamHandler = getDataStreamFromTemplateID(templateID);
        
        var dsInfo = dataStreamHandler.getDataStreamInfo();
        var dataStruct = dsInfo.getRecordStructure();
        var encoding = dsInfo.getRecordEncoding();
        DataStreamParser parser = null;
        
        try
        {
            InputStream resultStream;
            
            // select data source (either inline XML or in POST body for KVP)
            DataSource dataSrc = request.getResultDataSource();
            if (dataSrc instanceof DataSourceDOM) // inline XML
            {
                encoding = SWEHelper.ensureXmlCompatible(encoding);
                resultStream = dataSrc.getDataStream();
            }
            else // POST body
            {
                resultStream = new BufferedInputStream(request.getHttpRequest().getInputStream());
            }

            // create parser
            parser = SWEHelper.createDataParser(encoding);
            parser.setDataComponents(dataStruct);
            parser.setInput(resultStream);

            // if websocket, parse records in the callback
            if (SOSProviderUtils.isWebSocketRequest(request))
            {
                WebSocketListener socket = new SOSWebSocketIn(parser, dataStreamHandler::addObs, log);
                this.acceptWebSocket(request, socket);
            }
            else
            {
                // parse each record and send it to consumer
                DataBlock nextBlock = null;
                while ((nextBlock = parser.parseNextBlock()) != null)
                    dataStreamHandler.addObs(nextBlock);

                // build and send response
                InsertResultResponse resp = new InsertResultResponse();
                sendResponse(request, resp);
            }
        }
        catch (ReaderException e)
        {
            throw new SOSException("Error in SWE encoded data", e);
        }
        finally
        {
            if (parser != null)
                parser.close();
        }
    }
    
    
    protected Entry<DataStreamKey, IDataStreamInfo> findIdenticalDatastream(DataComponent resultStruct, DataEncoding resultEncoding, Collection<Entry<DataStreamKey, IDataStreamInfo>> outputList)
    {
        var newHc = DataComponentChecks.getStructEqualsHashCode(resultStruct);
        var newEncHc = DataComponentChecks.getEncodingEqualsHashCode(resultEncoding);
        
        for (var output: outputList)
        {
            var recordStruct = output.getValue().getRecordStructure();
            var recordEncoding = output.getValue().getRecordEncoding();
            
            var oldHc = DataComponentChecks.getStructEqualsHashCode(recordStruct);
            if (newHc.equals(oldHc) && newEncHc.equals(DataComponentChecks.getEncodingEqualsHashCode(recordEncoding)))
                return output;
        }
        
        return null;
    }
    
    
    protected Entry<DataStreamKey, IDataStreamInfo> findCompatibleDatastream(DataComponent resultStruct, DataEncoding resultEncoding, Collection<Entry<DataStreamKey, IDataStreamInfo>> outputList)
    {
        var newHc = DataComponentChecks.getStructCompatibilityHashCode(resultStruct);
        //var newEncHc = DataComponentChecks.getEncodingEqualsHashCode(resultEncoding);
        
        for (var output: outputList)
        {
            var recordStruct = output.getValue().getRecordStructure();
            //var recordEncoding = output.getValue().getRecordEncoding();
            
            var oldHc = DataComponentChecks.getStructCompatibilityHashCode(recordStruct);
            if (newHc.equals(oldHc))// && newEncHc.equals(DataComponentChecks.getEncodingEqualsHashCode(recordEncoding)))
                return output;
        }
        
        return null;
    }
    
    
    protected String generateOutputName(DataComponent resultStructure, int numOutputs)
    {
        var id = resultStructure.getId();
        var label = resultStructure.getLabel();
        
        // use ID or label if provided in result template
        if (id != null)
            return SWEDataUtils.toNCName(id);
        else if (label != null)
            return SWEDataUtils.toNCName(label);
        
        // otherwise generate an output name with numeric index
        return String.format("output%02d", numOutputs+1);
    }
    
    
    protected final String generateTemplateID(String procUID, String outputName)
    {
        return procUID + TEMPLATE_ID_SEPARATOR + outputName;
    }
    
    
    protected final DataStreamTransactionHandler getDataStreamFromTemplateID(String templateID) throws SOSException
    {
        try
        {
            Matcher m;
            if (templateID == null || !(m = TEMPLATE_ID_REGEX.matcher(templateID)).matches())
                throw new SOSException("");
            
            var procUID = m.group(1);
            var outputName = m.group(2);
            
            var dsHandler = transactionHandler.getDataStreamHandler(procUID, outputName);
            if (dsHandler == null)
                throw new SOSException("");
            
            return dsHandler;
        }
        catch (SOSException e)
        {
            throw new SOSException(SOSException.invalid_param_code, "template", templateID, "Unknown template ID: " + templateID);
        }
    }
    
    
    protected void checkQueryOffering(String offeringID, OWSExceptionReport report) throws SOSException
    {
        var procUID = getProcedureUID(offeringID);
        if (procUID == null || !readDatabase.getSystemDescStore().contains(procUID))
            report.add(new SOSException(SOSException.invalid_param_code, "offering", offeringID, "Unknown offering: " + offeringID));
    }


    protected void checkQueryOfferings(Set<String> offerings, OWSExceptionReport report) throws SOSException
    {
        for (String offeringID: offerings)
            checkQueryOffering(offeringID, report);
    }


    protected void checkQueryProcedures(Set<String> procedures, OWSExceptionReport report) throws SOSException
    {
        for (String procUID: procedures)
            checkQueryProcedure(procUID, report);
    }
    
    
    protected void checkQueryTime(TimeExtent requestTime, OWSExceptionReport report) throws SOSException
    {
        // reject null time period
        if (requestTime == null)
            return;
        
        // reject if startTime > stopTime
        if (requestTime.begin().isAfter(requestTime.end()))
            report.add(new SOSException("The requested period must begin before it ends"));            
    }


    protected ISOSAsyncDataProvider getDataProvider(String procUID, OWSRequest request) throws IOException, OWSException
    {
        try
        {
            SOSProviderConfig config = providerConfigs.get(procUID);

            // if no custom config, use default provider
            if (config == null)
                return getDefaultDataProvider(request);
            else
                return config.createProvider((SOSService)service, request);
        }
        catch (SensorHubException e)
        {
            throw new IOException("Cannot get provider for procedure " + procUID, e);
        }
    }


    protected Map<String, ISOSAsyncDataProvider> getDataProviders(Set<String> procUIDs, OWSRequest request) throws IOException, OWSException
    {
        var providerMap = new LinkedHashMap<String, ISOSAsyncDataProvider>();
        
        for (String procUID: procUIDs)
        {
            try
            {
                if (providerConfigs.containsKey(procUID))
                {
                    SOSProviderConfig config = providerConfigs.get(procUID);
                    var customDataProvider = config.createProvider((SOSService)service, request);
                    providerMap.put(procUID, customDataProvider);
                }
                
                else if (!providerMap.containsKey(DEFAULT_PROVIDER_KEY))
                    providerMap.put(DEFAULT_PROVIDER_KEY, getDefaultDataProvider(request));
            }
            catch (Exception e)
            {
                throw new IOException("Cannot get provider for procedure " + procUID, e);
            }
        }
        
        // if no offering or procedure was specified select all available
        // procedures from default provider
        if (procUIDs.isEmpty())
            providerMap.put(DEFAULT_PROVIDER_KEY, getDefaultDataProvider(request));
        
        return providerMap;
    }
    
    
    protected ISOSAsyncDataProvider getDefaultDataProvider(OWSRequest request) throws IOException, OWSException
    {
        try
        {
            var defaultConfig = new SystemDataProviderConfig();
            defaultConfig.liveDataTimeout = config.defaultLiveTimeout;
            return defaultConfig.createProvider((SOSService)service, request);
        }
        catch (SensorHubException e)
        {
            throw new IOException("Cannot get default provider", e);
        }
    }


    protected void checkTransactionalSupport(OWSRequest request) throws SOSException
    {
        if (!config.enableTransactional || writeDatabase == null)
            throw new SOSException(SOSException.invalid_param_code, "request", request.getOperation(), request.getOperation() + " operation is not supported on this endpoint");
    }


    protected void addProducerID(DataComponent dataStruct, String foiUriPrefix)
    {
        /*SWEHelper fac = new SWEHelper();

        // use category component if prefix was detected
        // or text component otherwise
        DataComponent producerIdField;
        if (foiUriPrefix != null && foiUriPrefix.length() > 2)
            producerIdField = fac.newCategory(SWEConstants.DEF_PROCEDURE_ID, PROCEDURE_ID_LABEL, null, foiUriPrefix);
        else
            producerIdField = fac.newText(SWEConstants.DEF_PROCEDURE_ID, PROCEDURE_ID_LABEL, null);

        // insert FOI component in data record after time stamp
        String producerIdName = "procedureID";
        if (dataStruct instanceof DataRecord)
        {
            OgcPropertyList<DataComponent> fields = ((DataRecord) dataStruct).getFieldList();
            producerIdField.setName(producerIdName);
            if (!fields.isEmpty() && fields.get(0) instanceof Time)
                fields.add(1, producerIdField);
            else
                fields.add(0, producerIdField);
        }
        else
        {
            DataRecord rec = fac.newDataRecord();
            rec.addField(producerIdName, producerIdField);
            rec.addField("data", dataStruct);
        }*/
    }


    /*
     * Check if request comes from a compatible browser
     */
    protected boolean isHttpRequestFromBrowser(OWSRequest request)
    {
        // don't do multipart with websockets
        if (SOSProviderUtils.isWebSocketRequest(request))
            return false;
        
        HttpServletRequest httpRequest = request.getHttpRequest();
        if (httpRequest == null)
            return false;

        String userAgent = httpRequest.getHeader("User-Agent");
        if (userAgent == null)
            return false;

        if (userAgent.contains("Firefox") ||
            userAgent.contains("Chrome") ||
            userAgent.contains("Safari"))
            return true;

        return false;
    }
    
    
    protected ISOSAsyncResultSerializer getDefaultSerializer(GetResultRequest request,  RecordTemplate resultTemplate) throws SOSException
    {
        // select serializer depending on encoding type
        if (resultTemplate.getDataEncoding() instanceof BinaryEncoding)
            return new ResultSerializerBinary();
        else
            return new ResultSerializerText();
    }


    protected ISOSAsyncResultSerializer getCustomFormatSerializer(GetResultRequest request,  RecordTemplate resultTemplate) throws SOSException
    {
        String format = request.getFormat();

        // auto select video format in some common cases
        if (format == null)
        {
            DataEncoding resultEncoding = resultTemplate.getDataEncoding();
            if (resultEncoding instanceof BinaryEncoding)
            {
                List<BinaryMember> mbrList = ((BinaryEncoding)resultEncoding).getMemberList();
                BinaryBlock videoFrameSpec = null;

                // try to find binary block encoding def in list
                for (BinaryMember spec: mbrList)
                {
                    if (spec instanceof BinaryBlock)
                    {
                        videoFrameSpec = (BinaryBlock)spec;
                        break;
                    }
                }

                if (videoFrameSpec != null)
                {
                    var codec = videoFrameSpec.getCompression();
                    if (isHttpRequestFromBrowser(request) && "H264".equalsIgnoreCase(codec))// || "H265".equalsIgnoreCase(codec)))
                        format = "video/mp4";

                    else if (isHttpRequestFromBrowser(request) && "JPEG".equalsIgnoreCase(codec))
                        format = "video/x-motion-jpeg";
                }
            }
            
            if (format == null)
                return null;
        }

        // try to find a matching implementation for selected format
        SOSCustomFormatConfig formatConfig;
        if (format != null && (formatConfig = customFormats.get(format)) != null)
        {
            try
            {
                var clazz = customSerializers.computeIfAbsent(format, Lambdas.checked(k -> {
                    ModuleRegistry moduleReg = service.getParentHub().getModuleRegistry();
                    return moduleReg.<ISOSAsyncResultSerializer>findClass(formatConfig.className);
                }));
                
                return clazz.getDeclaredConstructor().newInstance();
            }
            catch (Exception e)
            {
                log.error("Error while initializing custom serializer for " + formatConfig.mimeType + " serializer", e);
            }
        }

        throw new SOSException(SOSException.invalid_param_code, "format",
            format, INVALID_RESPONSE_FORMAT + format);
    }
    
    
    @Override
    public SOSServiceCapabilities getCapabilities()
    {
        return capabilities;
    }
    
    
    @Override
    protected String getServiceType()
    {
        return SOSUtils.SOS;
    }
}
