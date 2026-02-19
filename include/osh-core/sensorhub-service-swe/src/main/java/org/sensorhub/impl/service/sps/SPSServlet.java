/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.

******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sps;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;
import org.sensorhub.api.command.ICommandStatus;
import org.sensorhub.api.command.ICommandStreamInfo;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.datastore.command.CommandStatusFilter;
import org.sensorhub.api.datastore.command.CommandStreamFilter;
import org.sensorhub.api.datastore.command.CommandStreamKey;
import org.sensorhub.api.security.ISecurityManager;
import org.sensorhub.impl.service.ogc.OGCServiceConfig.CapabilitiesInfo;
import org.sensorhub.impl.service.swe.SWEServlet;
import org.sensorhub.impl.system.SystemDatabaseTransactionHandler;
import org.sensorhub.utils.DataComponentChecks;
import org.sensorhub.utils.SWEDataUtils;
import org.slf4j.Logger;
import org.vast.cdm.common.DataStreamWriter;
import org.vast.data.DataBlockList;
import org.vast.data.TextEncodingImpl;
import org.vast.ows.GetCapabilitiesRequest;
import org.vast.ows.OWSException;
import org.vast.ows.OWSExceptionReport;
import org.vast.ows.OWSRequest;
import org.vast.ows.sps.*;
import org.vast.ows.sps.StatusReport.RequestStatus;
import org.vast.ows.sps.StatusReport.TaskStatus;
import org.vast.ows.swe.DeleteSensorRequest;
import org.vast.ows.swe.DescribeSensorRequest;
import org.vast.ows.swe.SWESOfferingCapabilities;
import org.vast.ows.swe.UpdateSensorRequest;
import org.vast.sensorML.SMLUtils;
import org.vast.swe.SWEHelper;
import org.vast.util.DateTime;
import org.vast.util.TimeExtent;
import org.vast.xml.DOMHelper;
import org.w3c.dom.Element;


/**
 * <p>
 * Extension of OWSSServlet implementing Sensor Planning Service operations
 * </p>
 *
 * @author Alex Robin
 * @since Jan 15, 2015
 */
@SuppressWarnings("serial")
public class SPSServlet extends SWEServlet
{
    private static final String TASK_ID_PREFIX = "urn:sensorhub:sps:task:";
    private static final String FEASIBILITY_ID_PREFIX = "urn:sensorhub:sps:feas:";
    private static final char TEMPLATE_ID_SEPARATOR = '#';

    final transient SPSServiceConfig config;
    final transient SPSSecurity securityHandler;
    final transient CapabilitiesUpdater capsUpdater;
    final transient SPSServiceCapabilities capabilities = new SPSServiceCapabilities();
    
    final transient NavigableMap<String, SPSConnectorConfig> connectorConfigs;
    final transient SystemDatabaseTransactionHandler commandTxnHandler;
    final transient Map<String, TaskingSession> transmitSessionsMap = new ConcurrentHashMap<>();
    final transient Map<String, TaskingSession> receiveSessionsMap = new ConcurrentHashMap<>();

    final transient SMLUtils smlUtils = new SMLUtils(SMLUtils.V2_0);
    

    static class TaskingSession
    {
        String procUID;
        TimeExtent timeSlot;
        DataComponent taskingParams;
        DataEncoding encoding;
        ITask task;
    }


    public SPSServlet(SPSService service, SPSSecurity securityHandler, Logger log)
    {
        super(service, new SPSUtils(), log);
        
        this.config = service.getConfiguration();
        this.securityHandler = securityHandler;
        this.capsUpdater = new CapabilitiesUpdater();
        
        this.connectorConfigs = new TreeMap<>();
        for (var config: service.getConfiguration().customConnectors)
            connectorConfigs.put(config.systemUID, config);
        
        // I know the doc says otherwise but we need to use the federated DB for command transactions here
        // because we don't write to DB directly but rather send commands to systems that can be in other databases
        this.commandTxnHandler = new SystemDatabaseTransactionHandler(
            service.getParentHub().getEventBus(),
            readDatabase);
        
        generateCapabilities();
    }
    
    
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String userID = ISecurityManager.ANONYMOUS_USER;
        if (req.getRemoteUser() != null)
            userID = req.getRemoteUser();
        
        try
        {            
            // set current authentified user
            securityHandler.setCurrentUser(userID);
            
            // reject request early if SPS not authorized at all
            securityHandler.checkPermission(securityHandler.rootPerm);
            
            // otherwise process as classical HTTP request
            super.service(req, resp);
        }
        finally
        {
            securityHandler.clearCurrentUser();
        }
    }


    @Override
    protected OWSRequest parseRequest(HttpServletRequest req, HttpServletResponse resp, boolean isXmlRequest) throws OWSException
    {
        OWSRequest owsRequest = super.parseRequest(req, resp, isXmlRequest);

        // detect websocket request
        if (wsFactory.isUpgradeRequest(req, resp))
        {
            if (owsRequest instanceof ConnectTaskingRequest)
                owsRequest.getExtensions().put(EXT_WS, true);
            else
                throw new SPSException(INVALID_WS_REQ_MSG + owsRequest.getOperation() + " is not supported via this protocol");
        }

        return owsRequest;
    }


    /*
     * Overriden because we need additional info to parse tasking requests
     */
    @Override
    protected OWSRequest parseRequest(DOMHelper dom, Element requestElt) throws OWSException
    {
        // case of tasking request, need to get tasking params for the selected procedure
        if (isTaskingRequest(requestElt))
        {
            try
            {
                String procUID = dom.getElementValue(requestElt, "procedure");
                var taskingParams = getConnector(procUID).getTaskingParams();
                return new SPSUtils().readSpsRequest(dom, requestElt, taskingParams);
            }
            catch (IOException e)
            {
                throw new IllegalStateException(e);
            }
        }
        else
            return super.parseRequest(dom, requestElt);
    }


    protected boolean isTaskingRequest(Element requestElt)
    {
        String localName = requestElt.getLocalName();

        if (localName.equals("GetFeasibility"))
            return true;
        else if (localName.equals("Submit"))
            return true;
        else if (localName.equals("Update"))
            return true;
        else if (localName.equals("Reserve"))
            return true;

        return false;
    }


    /**
     * Generates the SPSServiceCapabilities object with info obtained from connector
     */
    protected void generateCapabilities()
    {
        // get main capabilities info from config
        CapabilitiesInfo serviceInfo = config.ogcCapabilitiesInfo;
        capabilities.getIdentification().setTitle(serviceInfo.title);
        capabilities.getIdentification().setDescription(serviceInfo.description);
        capabilities.setFees(serviceInfo.fees);
        capabilities.setAccessConstraints(serviceInfo.accessConstraints);
        capabilities.setServiceProvider(serviceInfo.serviceProvider);

        // supported operations
        String endpoint = service.getHttpServer().getPublicEndpointUrl(config.endPoint);
        capabilities.getGetServers().put("GetCapabilities", endpoint);
        capabilities.getGetServers().put("DescribeSensor", endpoint);
        capabilities.getPostServers().putAll(capabilities.getGetServers());
        capabilities.getPostServers().put("Submit", endpoint);
        capabilities.getPostServers().put("DirectTasking", endpoint);
        capabilities.getGetServers().put("ConnectTasking", endpoint);

        if (config.enableTransactional)
        {
            //capabilities.getProfiles().add(SOSServiceCapabilities.PROFILE_SENSOR_INSERTION);
            //capabilities.getProfiles().add(SOSServiceCapabilities.PROFILE_SENSOR_DELETION);/
            capabilities.getPostServers().put("InsertSensor", endpoint);
            capabilities.getPostServers().put("DeleteSensor", endpoint);
            capabilities.getPostServers().put("InsertTaskingTemplate", endpoint);
        }

        // generate profile list
        /*capabilities.getProfiles().add(SOSServiceCapabilities.PROFILE_RESULT_RETRIEVAL);
        if (config.enableTransactional)
        {
            capabilities.getProfiles().add(SOSServiceCapabilities.PROFILE_RESULT_INSERTION);
            capabilities.getProfiles().add(SOSServiceCapabilities.PROFILE_OBS_INSERTION);
        }*/
        
        // update offerings
        updateCapabilities();
    }


    @Override
    protected void handleRequest(OWSRequest request) throws IOException, OWSException
    {
        if (request instanceof GetCapabilitiesRequest)
            handleRequest((GetCapabilitiesRequest)request);
        else if (request instanceof DescribeSensorRequest)
            handleRequest((DescribeSensorRequest)request);
        else if (request instanceof DescribeTaskingRequest)
            handleRequest((DescribeTaskingRequest)request);
        else if (request instanceof GetStatusRequest)
            handleRequest((GetStatusRequest)request);
        else if (request instanceof GetFeasibilityRequest)
            handleRequest((GetFeasibilityRequest)request);
        else if (request instanceof SubmitRequest)
            handleRequest((SubmitRequest)request);
        else if (request instanceof UpdateRequest)
            handleRequest((UpdateRequest)request);
        else if (request instanceof CancelRequest)
            handleRequest((CancelRequest)request);
        else if (request instanceof ReserveRequest)
            handleRequest((ReserveRequest)request);
        else if (request instanceof ConfirmRequest)
            handleRequest((ConfirmRequest)request);
        else if (request instanceof DescribeResultAccessRequest)
            handleRequest((DescribeResultAccessRequest)request);
        else if (request instanceof DirectTaskingRequest)
            handleRequest((DirectTaskingRequest)request);
        else if (request instanceof ConnectTaskingRequest)
            handleRequest((ConnectTaskingRequest)request);

        // transactional operations
        else if (request instanceof InsertSensorRequest)
            handleRequest((InsertSensorRequest)request);
        else if (request instanceof InsertTaskingTemplateRequest)
            handleRequest((InsertTaskingTemplateRequest)request);

        else
            throw new SPSException(SPSException.unsupported_op_code, request.getOperation());
    }


    @Override
    protected void handleRequest(GetCapabilitiesRequest request) throws IOException, OWSException
    {
        // security check
        securityHandler.checkPermission(securityHandler.sps_read_caps);        
        super.handleRequest(request);
    }
    
    
    public SPSServiceCapabilities updateCapabilities()
    {
        capsUpdater.updateOfferings(this);
        getLogger().debug("Updating capabilities");
        return capabilities;
    }
    
    
    protected void handleRequest(DescribeSensorRequest request) throws IOException, OWSException
    {
        // security check
        securityHandler.checkPermission(securityHandler.sps_read_sensor);
        
        // check query parameters
        String procUID = request.getProcedureID();
        OWSExceptionReport report = new OWSExceptionReport();
        checkQueryProcedureFormat(procUID, request.getFormat(), report);
        report.process();
        
        // start async response
        AsyncContext asyncCtx = request.getHttpRequest().startAsync();
        CompletableFuture.runAsync(() -> {
            try
            {
                var connector = getConnector(procUID);
                
                var serializer = new ProcedureSerializerXml(this, request, asyncCtx);
                serializer.beforeRecords();
                
                var it = connector.getProcedureDescriptions(request.getTime()).iterator();
                while (it.hasNext())
                    serializer.writeRecord(it.next());
                
                serializer.afterRecords();
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


    protected void handleRequest(DescribeTaskingRequest request) throws IOException, OWSException
    {
        // security check
        securityHandler.checkPermission(securityHandler.sps_read_params);

        AsyncContext asyncCtx = request.getHttpRequest().startAsync();
        CompletableFuture.runAsync(() -> {
            try
            {
                String procUID = request.getProcedureID();
                var taskingParams = getConnector(procUID).getTaskingParams();
                var resp = new DescribeTaskingResponse();
                resp.setTaskingParameters(taskingParams);
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


    protected void handleRequest(GetStatusRequest request) throws IOException, OWSException
    {
        // security check
        securityHandler.checkPermission(securityHandler.sps_read_task);

        AsyncContext asyncCtx = request.getHttpRequest().startAsync();
        CompletableFuture.runAsync(() -> {
            try
            {
                var taskID = request.getTaskID(); 
                var status = getReadDatabase().getCommandStatusStore()
                    .select(new CommandStatusFilter.Builder()
                        .withCommands(BigId.fromString32(taskID))
                        .latestReport()
                        .build())
                    .findFirst().orElse(null);
                
                if (status == null)
                    throw new SPSException(SPSException.invalid_param_code, "task", taskID);
   
                GetStatusResponse gsResponse = new GetStatusResponse();
                gsResponse.setVersion("2.0.0");
                gsResponse.getReportList().add(toStatusReport(status));
   
                sendResponse(request, gsResponse);
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
    
    
    protected StatusReport toStatusReport(ICommandStatus status)
    {
        StatusReport sr = new StatusReport();
        sr.setTaskID(BigId.toString32(status.getCommandID()));
        sr.setLastUpdate(new DateTime(status.getReportTime().toEpochMilli()));
        if (status.getProgress() >= 0)
            sr.setPercentCompletion(status.getProgress());
        sr.setStatusMessage(status.getMessage());
        
        // map status codes
        switch (status.getStatusCode())
        {
            case PENDING:
                sr.setRequestStatus(RequestStatus.Pending);
                break;
            case ACCEPTED:
                sr.setRequestStatus(RequestStatus.Accepted);
                break;
            case REJECTED:
                sr.setRequestStatus(RequestStatus.Rejected);
                break;
            case SCHEDULED:
                sr.setRequestStatus(RequestStatus.Accepted);
                break;
            case UPDATED:
                sr.setRequestStatus(RequestStatus.Accepted);
                break;
            case CANCELED:
                sr.setRequestStatus(RequestStatus.Accepted);
                sr.setTaskStatus(TaskStatus.Cancelled);
                break;
            case EXECUTING:
                sr.setRequestStatus(RequestStatus.Accepted);
                sr.setTaskStatus(TaskStatus.InExecution);
                break;
            case FAILED:
                sr.setRequestStatus(RequestStatus.Accepted);
                sr.setTaskStatus(TaskStatus.Failed);
                break;
            case COMPLETED:
                sr.setRequestStatus(RequestStatus.Accepted);
                sr.setTaskStatus(TaskStatus.Completed);
                break;
        }
        
        return sr;
    }


    protected GetFeasibilityResponse handleRequest(GetFeasibilityRequest request) throws IOException, OWSException
    {
        throw new SPSException(SPSException.unsupported_op_code, request.getOperation());
    }


    protected void handleRequest(SubmitRequest request) throws IOException, OWSException
    {
        // security check
        securityHandler.checkPermission(securityHandler.sps_task_submit);
        
        AsyncContext asyncCtx = request.getHttpRequest().startAsync();
        CompletableFuture.runAsync(() -> {
            try
            {
                // retrieve connector instance
                String procUID = request.getProcedureID();
                ISPSConnector conn = getConnector(procUID);
                
                // validate task parameters
                request.validate();

                // send commands through connector
                try
                {
                    // send all commands and wait for last one to be processed
                    CompletableFuture<ICommandStatus> lastFuture = null;
                    
                    DataBlockList dataBlockList = (DataBlockList)request.getParameters().getData();
                    var it = dataBlockList.blockIterator();
                    if (it.hasNext())
                    {
                        var data = it.next();
                        conn.getTaskingParams(); // need to preload tasking params
                        lastFuture = conn.sendCommand(data, true);
                    }
                    
                    lastFuture.thenAccept(status -> {
                        try
                        {
                            var report = toStatusReport(status);
                            
                            // create response and add report
                            SubmitResponse sResponse = new SubmitResponse();
                            sResponse.setVersion("2.0");
                            report.setSensorID(procUID);
                            report.touch();
                            sResponse.setReport(report);
                            
                            // send response
                            sendResponse(request, sResponse);
                            asyncCtx.complete();
                        }
                        catch (IOException e)
                        {
                            throw new CompletionException(e);
                        }
                    })
                    .exceptionally(ex -> {
                        handleError(
                            (HttpServletRequest)asyncCtx.getRequest(),
                            (HttpServletResponse)asyncCtx.getResponse(),
                            request, ex);
                        return null;
                    });
                }
                catch (Exception e)
                {
                    throw new IOException("Cannot send command to procedure " + procUID, e);
                }
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


    protected synchronized void handleRequest(DirectTaskingRequest request) throws IOException, OWSException
    {
        // security check
        securityHandler.checkPermission(securityHandler.sps_task_direct);

        // retrieve tasking parameters
        String procUID = request.getProcedureID();
        var taskingParams = getConnector(procUID).getTaskingParams();

        // for now we don't support reserving a session for a specific time range
        if (!request.getTimeSlot().isNow())
            throw new SPSException(SPSException.invalid_param_code, "timeSlot", null, "Scheduling direct tasking session is not supported yet");

        // fail if a session already exists for this procedure
        for (TaskingSession session: receiveSessionsMap.values())
        {
            if (procUID.equals(session.procUID))
                throw new SPSException("A direct tasking session is already started");
        }

        // create task in DB
        ITask task = createNewTask(request);
        final String taskID = task.getID();

        // create session
        TaskingSession newSession = new TaskingSession();
        newSession.procUID = procUID;
        newSession.timeSlot = request.getTimeSlot();
        newSession.taskingParams = taskingParams;
        newSession.encoding = request.getEncoding();
        newSession.task = task;
        receiveSessionsMap.put(taskID, newSession);

        // add report and send response
        DirectTaskingResponse sResponse = new DirectTaskingResponse();
        sResponse.setVersion("2.0");
        task.getStatusReport().setRequestStatus(RequestStatus.Accepted);
        task.getStatusReport().setTaskStatus(TaskStatus.Reserved);
        task.getStatusReport().touch();
        sResponse.setReport(task.getStatusReport());

        sendResponse(request, sResponse);
    }


    // we dispatch request depending if it's for receiving or transmitting commands
    protected synchronized void handleRequest(ConnectTaskingRequest request) throws IOException, OWSException
    {
        String sessionID = request.getSessionID();
        TaskingSession session;

        // check if receive sessions
        session = receiveSessionsMap.get(sessionID);
        if (session != null)
        {
            startReceiveTaskingStream(session, request);
            return;
        }

        // check if transmit sessions
        session = transmitSessionsMap.get(sessionID);
        if (session != null)
        {
            startTransmitTaskingStream(session, request);
            return;
        }

        throw new SPSException(SPSException.invalid_param_code, "session", sessionID, "Invalid session ID");
    }


    /*
     * Start the websocket stream to receive commands from a remote sender
     */
    protected void startReceiveTaskingStream(TaskingSession session, ConnectTaskingRequest request) throws IOException, OWSException
    {
        try
        {
            // security check
            securityHandler.checkPermission(securityHandler.sps_task_direct);
            
            // retrieve connector
            var conn = getConnector(session.procUID);
            conn.startDirectTasking(session.taskingParams);

            // prepare parser for tasking stream
            var taskID = request.getSessionID();
            var parser = SWEHelper.createDataParser(session.encoding);
            parser.setDataComponents(session.taskingParams);

            // if it's a websocket request
            if (isWebSocketRequest(request))
            {
                SPSWebSocketIn ws = new SPSWebSocketIn(this, taskID, conn, parser, log);
                acceptWebSocket(request, ws);
            }

            // else it's persistent HTTP
            else
            {
                throw new IOException("Only WebSocket ConnectTasking requests are supported");
            }
        }
        finally
        {

        }
    }


    protected void startTransmitTaskingStream(TaskingSession session, ConnectTaskingRequest request) throws IOException, OWSException
    {
        // security check
        securityHandler.checkPermission(securityHandler.sps_connect_tasking);
        checkTransactionalSupport(request);
        
        // retrieve connector
        var conn = getConnector(session.procUID);

        // prepare writer
        final DataStreamWriter writer = SWEHelper.createDataWriter(session.encoding);
        writer.setDataComponents(session.taskingParams);

        // handle cases of websocket and persistent HTTP
        if (isWebSocketRequest(request))
        {
            SPSWebSocketOut ws = new SPSWebSocketOut(conn, writer, log);
            acceptWebSocket(request, ws);
        }
        else
        {
            throw new IOException("Only WebSocket ConnectTasking requests are supported");
        }
    }


    protected void handleRequest(UpdateRequest request) throws IOException, OWSException
    {
        throw new SPSException(SPSException.unsupported_op_code, request.getOperation());
    }


    protected void handleRequest(CancelRequest request) throws IOException, OWSException
    {
        throw new SPSException(SPSException.unsupported_op_code, request.getOperation());
    }


    protected void handleRequest(ReserveRequest request) throws IOException, OWSException
    {
        throw new SPSException(SPSException.unsupported_op_code, request.getOperation());
    }


    protected void handleRequest(ConfirmRequest request) throws IOException, OWSException
    {
        throw new SPSException(SPSException.unsupported_op_code, request.getOperation());
    }


    protected void handleRequest(DescribeResultAccessRequest request) throws IOException, OWSException
    {
        throw new SPSException(SPSException.unsupported_op_code, request.getOperation());
    }


    protected Task createNewTask(GetFeasibilityRequest request)
    {
        Task newTask = new Task();
        newTask.setStatusReport(new FeasibilityReport());
        newTask.setRequest(request);
        String taskID = FEASIBILITY_ID_PREFIX + UUID.randomUUID().toString();

        // initial status
        newTask.getStatusReport().setTaskID(taskID);
        newTask.getStatusReport().setTitle("Feasibility Study Report");
        newTask.getStatusReport().setSensorID(request.getProcedureID());
        newTask.getStatusReport().setRequestStatus(RequestStatus.Pending);

        // creation time
        newTask.setCreationTime(new DateTime());
        
        return newTask;
    }


    protected Task createNewTask(SubmitRequest request)
    {
        Task newTask = new Task();
        newTask.setRequest(request);
        String taskID = TASK_ID_PREFIX + UUID.randomUUID().toString();

        // initial status
        newTask.getStatusReport().setTaskID(taskID);
        newTask.getStatusReport().setTitle("Tasking Request Report");
        newTask.getStatusReport().setSensorID(request.getProcedureID());
        newTask.getStatusReport().setRequestStatus(RequestStatus.Pending);
        
        // creation time
        newTask.setCreationTime(new DateTime());
        
        return newTask;
    }


    protected Task createNewTask(DirectTaskingRequest request)
    {
        Task newTask = new Task();
        String taskID = TASK_ID_PREFIX + UUID.randomUUID().toString();

        // initial status
        newTask.getStatusReport().setTaskID(taskID);
        newTask.getStatusReport().setTitle("Direct Tasking Report");
        newTask.getStatusReport().setSensorID(request.getProcedureID());
        newTask.getStatusReport().setRequestStatus(RequestStatus.Pending);

        // creation time
        newTask.setCreationTime(new DateTime());

        return newTask;
    }


    //////////////////////////////
    // Transactional Operations //
    //////////////////////////////
    @Override
    protected void handleRequest(org.vast.ows.swe.InsertSensorRequest request) throws IOException, OWSException
    {
        securityHandler.checkPermission(securityHandler.sps_insert_sensor);
        super.handleRequest(request);
    }


    @Override
    protected void handleRequest(UpdateSensorRequest request) throws IOException, OWSException
    {
        securityHandler.checkPermission(securityHandler.sps_update_sensor);
        super.handleRequest(request);
    }


    @Override
    protected void handleRequest(DeleteSensorRequest request) throws IOException, OWSException
    {
        securityHandler.checkPermission(securityHandler.sps_delete_sensor);
        super.handleRequest(request);
    }


    protected void handleRequest(InsertTaskingTemplateRequest request) throws IOException, OWSException
    {
        checkTransactionalSupport(request);

        // security check
        securityHandler.checkPermission(securityHandler.sps_insert_sensor);
        
        // check query parameters
        String procUID = request.getProcedureID();
        OWSExceptionReport report = new OWSExceptionReport();
        checkQueryProcedure(procUID, report);
        report.process();
                
        var commandStruct = request.getTaskingParameters();
        var commandEncoding = new TextEncodingImpl();
        
        // start async response
        AsyncContext asyncCtx = request.getHttpRequest().startAsync();
        CompletableFuture.runAsync(() -> {
            try
            {
                // retrieve transaction helper
                var procHandler = transactionHandler.getSystemHandler(procUID);
                var sysID = procHandler.getSystemKey().getInternalID();
                
                // get existing command streams of this procedure
                var controlInputs = writeDatabase.getCommandStreamStore().selectEntries(new CommandStreamFilter.Builder()
                        .withSystems(sysID)
                        .withCurrentVersion()
                        .build())
                    .collect(Collectors.toMap(
                        csEntry -> csEntry.getValue().getControlInputName(),
                        csEntry -> csEntry));
                
                // generate or use existing param name
                String paramName = null;
                var existingParam = findCompatibleCommandStream(commandStruct, commandEncoding, controlInputs.values());
                if (existingParam != null)
                {
                    paramName = existingParam.getValue().getControlInputName();
                    getLogger().info("Found existing control input {} on procedure {}", paramName, procUID);
                }
                else
                {
                    paramName = generateParamName(commandStruct, controlInputs.size());
                    getLogger().info("Adding new control input {} to procedure {}", paramName, procUID);
                }
                
                // add or update command stream
                commandStruct.setName(paramName);
                procHandler.addOrUpdateCommandStream(paramName, commandStruct, commandEncoding);
                
                // create session if needed
                String templateID = generateTemplateID(procUID, paramName);
                if (!transmitSessionsMap.containsKey(templateID))
                {
                    // create session
                    TaskingSession newSession = new TaskingSession();
                    newSession.procUID = procUID;
                    newSession.taskingParams = commandStruct;
                    newSession.encoding = request.getEncoding();
                    transmitSessionsMap.put(templateID, newSession);
                }

                // build and send response
                InsertTaskingTemplateResponse resp = new InsertTaskingTemplateResponse();
                resp.setSessionID(templateID);
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
    
    
    protected Entry<CommandStreamKey, ICommandStreamInfo> findIdenticalCommandStream(DataComponent commandStruct, Collection<Entry<CommandStreamKey, ICommandStreamInfo>> paramList)
    {
        var newHc = DataComponentChecks.getStructEqualsHashCode(commandStruct);
        
        for (var param: paramList)
        {
            var recordStruct = param.getValue().getRecordStructure();
            var oldHc = DataComponentChecks.getStructEqualsHashCode(recordStruct);
            if (newHc.equals(oldHc))
                return param;
        }
        
        return null;
    }
    
    
    protected Entry<CommandStreamKey, ICommandStreamInfo> findCompatibleCommandStream(DataComponent commandStruct, DataEncoding resultEncoding, Collection<Entry<CommandStreamKey, ICommandStreamInfo>> paramList)
    {
        var newHc = DataComponentChecks.getStructCompatibilityHashCode(commandStruct);
        
        for (var param: paramList)
        {
            var recordStruct = param.getValue().getRecordStructure();
            var oldHc = DataComponentChecks.getStructCompatibilityHashCode(recordStruct);
            if (newHc.equals(oldHc))
                return param;
        }
        
        return null;
    }
    
    
    protected String generateParamName(DataComponent commandStruct, int numParams)
    {
        var id = commandStruct.getId();
        var label = commandStruct.getLabel();
        
        // use ID or label if provided in result template
        if (id != null)
            return SWEDataUtils.toNCName(id);
        else if (label != null)
            return SWEDataUtils.toNCName(label);
        
        // otherwise generate a param name with numeric index
        return String.format("command%02d", numParams+1);
    }
    
    
    protected final String generateTemplateID(String procUID, String paramName)
    {
        return procUID + TEMPLATE_ID_SEPARATOR + paramName;
    }


    protected ISPSConnector getConnector(String procUID) throws IOException, OWSException
    {
        if (!readDatabase.getSystemDescStore().contains(procUID))
            throw new SPSException(SPSException.invalid_param_code, "procedure", procUID);
        
        try
        {
            var config = connectorConfigs.get(procUID);

            // if no custom config, use default connector
            if (config == null)
                return getDefaultConnector(procUID);
            else
                return config.createConnector((SPSService)service);
        }
        catch (SensorHubException e)
        {
            throw new IOException("Cannot get tasking connector for procedure " + procUID, e);
        }
    }
    
    
    protected ISPSConnector getDefaultConnector(String procUID) throws IOException, OWSException
    {
        try
        {
            var defaultConfig = new SystemTaskingConnectorConfig();
            defaultConfig.systemUID = procUID;
            return defaultConfig.createConnector((SPSService)service);
        }
        catch (SensorHubException e)
        {
            throw new IOException("Cannot get default tasking connector", e);
        }
    }


    protected void checkQueryProcedureFormat(String procedureID, String format, OWSExceptionReport report) throws OWSException
    {
        // ok if default format can be used
        if (format == null)
            return;

        if (!SWESOfferingCapabilities.FORMAT_SML2.equals(format))
            report.add(new SPSException(SPSException.invalid_param_code, "procedureDescriptionFormat", format, "Procedure description format " + format + " is not available for procedure " + procedureID));
    }


    public SystemDatabaseTransactionHandler getTransactionHandler()
    {
        return transactionHandler;
    }
    
    
    protected SystemDatabaseTransactionHandler getSubmitTxnHandler()
    {
        return commandTxnHandler;
    }
    
    
    protected String getCurrentUser()
    {
        var user = securityHandler.getCurrentUser();
        return user != null ? user.getId() : ISecurityManager.ANONYMOUS_USER;
    }
    
    
    @Override
    public SPSServiceCapabilities getCapabilities()
    {
        return capabilities;
    }


    @Override
    protected String getServiceType()
    {
        return SPSUtils.SPS;
    }
}
