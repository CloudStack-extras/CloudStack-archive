/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.cloud.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.SocketHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.log4j.Logger;

import com.cloud.api.response.ApiResponseSerializer;
import com.cloud.api.response.ExceptionResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.async.AsyncJob;
import com.cloud.async.AsyncJobManager;
import com.cloud.async.AsyncJobVO;
import com.cloud.cluster.StackMaid;
import com.cloud.configuration.Config;
import com.cloud.configuration.ConfigurationVO;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.domain.Domain;
import com.cloud.domain.DomainVO;
import com.cloud.event.EventUtils;
import com.cloud.exception.CloudAuthenticationException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.server.ManagementServer;
import com.cloud.user.Account;
import com.cloud.user.AccountService;
import com.cloud.user.User;
import com.cloud.user.UserAccount;
import com.cloud.user.UserContext;
import com.cloud.utils.Pair;
import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.encoding.Base64;

public class ApiServer implements HttpRequestHandler {
    private static final Logger s_logger = Logger.getLogger(ApiServer.class.getName());
    private static final Logger s_accessLogger = Logger.getLogger("apiserver." + ApiServer.class.getName());

    public static final short ADMIN_COMMAND = 1;
    public static final short DOMAIN_ADMIN_COMMAND = 4;
    public static final short RESOURCE_DOMAIN_ADMIN_COMMAND = 2;
    public static final short USER_COMMAND = 8;
    public static boolean encodeApiResponse = false;
    private Properties _apiCommands = null;
    private ApiDispatcher _dispatcher;
    private ManagementServer _ms = null;
    private AccountService _accountMgr = null;
    private AsyncJobManager _asyncMgr = null;
    private Account _systemAccount = null;
    private User _systemUser = null;

    private static int _workerCount = 0;

    private static ApiServer s_instance = null;
    private static List<String> s_userCommands = null;
    private static List<String> s_resellerCommands = null; // AKA domain-admin
    private static List<String> s_adminCommands = null;
    private static List<String> s_resourceDomainAdminCommands = null;
    private static List<String> s_allCommands = null;
    private static final DateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    
    private static ExecutorService _executor = new ThreadPoolExecutor(10, 150, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("ApiServer"));

    static {
        s_userCommands = new ArrayList<String>();
        s_resellerCommands = new ArrayList<String>();
        s_adminCommands = new ArrayList<String>();
        s_resourceDomainAdminCommands = new ArrayList<String>();
        s_allCommands = new ArrayList<String>();
    }

    private ApiServer() {
    }

    public static void initApiServer(String[] apiConfig) {
        if (s_instance == null) {
            s_instance = new ApiServer();
            s_instance.init(apiConfig);
            s_instance.createCache();
        }
    }

    public static ApiServer getInstance() {
        // initApiServer();
        return s_instance;
    }

    public Properties get_apiCommands() {
        return _apiCommands;
    }

    public void init(String[] apiConfig) {
        try {
            BaseCmd.setComponents(new ApiResponseHelper());
            BaseListCmd.configure();
            _apiCommands = new Properties();
            Properties preProcessedCommands = new Properties();
            if (apiConfig != null) {
                for (String configFile : apiConfig) {
                    File commandsFile = PropertiesUtil.findConfigFile(configFile);
                    preProcessedCommands.load(new FileInputStream(commandsFile));
                }
                for (Object key : preProcessedCommands.keySet()) {
                    String preProcessedCommand = preProcessedCommands.getProperty((String) key);
                    String[] commandParts = preProcessedCommand.split(";");
                    _apiCommands.put(key, commandParts[0]);
                    if (commandParts.length > 1) {
                        try {
                            short cmdPermissions = Short.parseShort(commandParts[1]);
                            if ((cmdPermissions & ADMIN_COMMAND) != 0) {
                                s_adminCommands.add((String) key);
                            }
                            if ((cmdPermissions & RESOURCE_DOMAIN_ADMIN_COMMAND) != 0) {
                                s_resourceDomainAdminCommands.add((String) key);
                            }
                            if ((cmdPermissions & DOMAIN_ADMIN_COMMAND) != 0) {
                                s_resellerCommands.add((String) key);
                            }
                            if ((cmdPermissions & USER_COMMAND) != 0) {
                                s_userCommands.add((String) key);
                            }
                        } catch (NumberFormatException nfe) {
                            s_logger.info("Malformed command.properties permissions value, key = " + key + ", value = " + preProcessedCommand);
                        }
                    }
                }

                s_allCommands.addAll(s_adminCommands);
                s_allCommands.addAll(s_resourceDomainAdminCommands);
                s_allCommands.addAll(s_userCommands);
                s_allCommands.addAll(s_resellerCommands);
            }
        } catch (FileNotFoundException fnfex) {
            s_logger.error("Unable to find properites file", fnfex);
        } catch (IOException ioex) {
            s_logger.error("Exception loading properties file", ioex);
        }

        _ms = (ManagementServer) ComponentLocator.getComponent(ManagementServer.Name);
        ComponentLocator locator = ComponentLocator.getLocator(ManagementServer.Name);
        _accountMgr = locator.getManager(AccountService.class);
        _asyncMgr = locator.getManager(AsyncJobManager.class);
        _systemAccount = _accountMgr.getSystemAccount();
        _systemUser = _accountMgr.getSystemUser();
        _dispatcher = ApiDispatcher.getInstance();

        int apiPort = 8096; // default port
        ConfigurationDao configDao = locator.getDao(ConfigurationDao.class);
        SearchCriteria<ConfigurationVO> sc = configDao.createSearchCriteria();
        sc.addAnd("name", SearchCriteria.Op.EQ, "integration.api.port");
        List<ConfigurationVO> values = configDao.search(sc, null);
        if ((values != null) && (values.size() > 0)) {
            ConfigurationVO apiPortConfig = values.get(0);
            apiPort = Integer.parseInt(apiPortConfig.getValue());
        }
        
        encodeApiResponse = Boolean.valueOf(configDao.getValue(Config.EncodeApiResponse.key()));

        ListenerThread listenerThread = new ListenerThread(this, apiPort);
        listenerThread.start();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        // get some information for the access log...
        StringBuffer sb = new StringBuffer();
        HttpServerConnection connObj = (HttpServerConnection) context.getAttribute("http.connection");
        if (connObj instanceof SocketHttpServerConnection) {
            InetAddress remoteAddr = ((SocketHttpServerConnection) connObj).getRemoteAddress();
            sb.append(remoteAddr.toString() + " -- ");
        }
        sb.append(request.getRequestLine());

        try {
            String uri = request.getRequestLine().getUri();
            int requestParamsStartIndex = uri.indexOf('?');
            if (requestParamsStartIndex >= 0) {
                uri = uri.substring(requestParamsStartIndex + 1);
            }

            String[] paramArray = uri.split("&");
            if (paramArray.length < 1) {
                s_logger.info("no parameters received for request: " + uri + ", aborting...");
                return;
            }

            Map parameterMap = new HashMap<String, String[]>();

            String responseType = BaseCmd.RESPONSE_TYPE_XML;
            for (String paramEntry : paramArray) {
                String[] paramValue = paramEntry.split("=");
                if (paramValue.length != 2) {
                    s_logger.info("malformed parameter: " + paramEntry + ", skipping");
                    continue;
                }
                if ("response".equalsIgnoreCase(paramValue[0])) {
                    responseType = paramValue[1];
                } else {
                    // according to the servlet spec, the parameter map should be in the form (name=String, value=String[]), so
                    // parameter values will be stored in an array
                    parameterMap.put(/* name */paramValue[0], /* value */new String[] { paramValue[1] });
                }
            }
            try {
                // always trust commands from API port, user context will always be UID_SYSTEM/ACCOUNT_ID_SYSTEM
                UserContext.registerContext(_systemUser.getId(), _systemAccount, null, true);
                sb.insert(0, "(userId=" + User.UID_SYSTEM + " accountId=" + Account.ACCOUNT_ID_SYSTEM + " sessionId=" + null + ") ");
                String responseText = handleRequest(parameterMap, true, responseType, sb);
                sb.append(" 200 " + ((responseText == null) ? 0 : responseText.length()));

                writeResponse(response, responseText, HttpStatus.SC_OK, responseType, null);
            } catch (ServerApiException se) {
                String responseText = getSerializedApiError(se.getErrorCode(), se.getDescription(), parameterMap, responseType);
                writeResponse(response, responseText, se.getErrorCode(), responseType, se.getDescription());
                sb.append(" " + se.getErrorCode() + " " + se.getDescription());
            } catch (RuntimeException e) {
                // log runtime exception like NullPointerException to help identify the source easier
                s_logger.error("Unhandled exception, ", e);
                throw e;
            }
        } finally {
            s_accessLogger.info(sb.toString());
            UserContext.unregisterContext();
        }
    }

    @SuppressWarnings("rawtypes")
    public String handleRequest(Map params, boolean decode, String responseType, StringBuffer auditTrailSb) throws ServerApiException {
        String response = null;
        String[] command = null;
        try {
            command = (String[]) params.get("command");
            if (command == null) {
                s_logger.error("invalid request, no command sent");
                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("dumping request parameters");
                    for (Object key : params.keySet()) {
                        String keyStr = (String) key;
                        String[] value = (String[]) params.get(key);
                        s_logger.trace("   key: " + keyStr + ", value: " + ((value == null) ? "'null'" : value[0]));
                    }
                }
                throw new ServerApiException(BaseCmd.UNSUPPORTED_ACTION_ERROR, "Invalid request, no command sent");
            } else {
                Map<String, String> paramMap = new HashMap<String, String>();
                Set keys = params.keySet();
                Iterator keysIter = keys.iterator();
                while (keysIter.hasNext()) {
                    String key = (String) keysIter.next();
                    if ("command".equalsIgnoreCase(key)) {
                        continue;
                    }
                    String[] value = (String[]) params.get(key);

                    String decodedValue = null;
                    if (decode) {
                        try {
                            decodedValue = URLDecoder.decode(value[0], "UTF-8");
                        } catch (UnsupportedEncodingException usex) {
                            s_logger.warn(key + " could not be decoded, value = " + value[0]);
                            throw new ServerApiException(BaseCmd.PARAM_ERROR, key + " could not be decoded, received value " + value[0]);
                        } catch (IllegalArgumentException iae) {
                            s_logger.warn(key + " could not be decoded, value = " + value[0]);
                            throw new ServerApiException(BaseCmd.PARAM_ERROR, key + " could not be decoded, received value " + value[0] + " which contains illegal characters eg.%");
                        }
                    } else {
                        decodedValue = value[0];
                    }
                    paramMap.put(key, decodedValue);
                }
                String cmdClassName = _apiCommands.getProperty(command[0]);
                if (cmdClassName != null) {
                    Class<?> cmdClass = Class.forName(cmdClassName);
                    BaseCmd cmdObj = (BaseCmd) cmdClass.newInstance();
                    cmdObj.setFullUrlParams(paramMap);
                    cmdObj.setResponseType(responseType);
                    // This is where the command is either serialized, or directly dispatched
                    response = queueCommand(cmdObj, paramMap);
                    buildAuditTrail(auditTrailSb, command[0], response);
                } else {
                    if (!command[0].equalsIgnoreCase("login") && !command[0].equalsIgnoreCase("logout")) {
                        String errorString = "Unknown API command: " + ((command == null) ? "null" : command[0]);
                        s_logger.warn(errorString);
                        auditTrailSb.append(" " + errorString);
                        throw new ServerApiException(BaseCmd.UNSUPPORTED_ACTION_ERROR, errorString);
                    }
                }
            }
        } catch (Exception ex) {
            if (ex instanceof InvalidParameterValueException) {
                throw new ServerApiException(BaseCmd.PARAM_ERROR, ex.getMessage());
            } else if (ex instanceof PermissionDeniedException) {
                throw new ServerApiException(BaseCmd.ACCOUNT_ERROR, ex.getMessage());
            } else if (ex instanceof ServerApiException) {
                throw (ServerApiException) ex;
            } else {
                s_logger.error("unhandled exception executing api command: " + ((command == null) ? "null" : command[0]), ex);
                throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Internal server error, unable to execute request.");
            }
        }
        return response;
    }

    private String queueCommand(BaseCmd cmdObj, Map<String, String> params) {
        UserContext ctx = UserContext.current();
        Long callerUserId = ctx.getCallerUserId();
        Account caller = ctx.getCaller();
        if (cmdObj instanceof BaseAsyncCmd) {
            Long objectId = null;
            if (cmdObj instanceof BaseAsyncCreateCmd) {
                BaseAsyncCreateCmd createCmd = (BaseAsyncCreateCmd) cmdObj;
                _dispatcher.dispatchCreateCmd(createCmd, params);
                objectId = createCmd.getEntityId();
                params.put("id", objectId.toString());
            } else {
                ApiDispatcher.setupParameters(cmdObj, params);
            }

            BaseAsyncCmd asyncCmd = (BaseAsyncCmd) cmdObj;

            if (callerUserId != null) {
                params.put("ctxUserId", callerUserId.toString());
            }
            if (caller != null) {
                params.put("ctxAccountId", String.valueOf(caller.getId()));
            }

            long startEventId = ctx.getStartEventId();
            asyncCmd.setStartEventId(startEventId);

            // save the scheduled event
            Long eventId = EventUtils.saveScheduledEvent((callerUserId == null) ? User.UID_SYSTEM : callerUserId, asyncCmd.getEntityOwnerId(), asyncCmd.getEventType(), asyncCmd.getEventDescription(),
                    startEventId);
            if (startEventId == 0) {
                // There was no create event before, set current event id as start eventId
                startEventId = eventId;
            }

            params.put("ctxStartEventId", String.valueOf(startEventId));

            ctx.setAccountId(asyncCmd.getEntityOwnerId());

            AsyncJobVO job = new AsyncJobVO();
            job.setInstanceId((objectId == null) ? asyncCmd.getInstanceId() : objectId);
            job.setInstanceType(asyncCmd.getInstanceType());
            job.setUserId(callerUserId);
            job.setAccountId(caller.getId());

            job.setCmd(cmdObj.getClass().getName());
            job.setCmdInfo(ApiGsonHelper.getBuilder().create().toJson(params));

            long jobId = _asyncMgr.submitAsyncJob(job);

            if (jobId == 0L) {
                String errorMsg = "Unable to schedule async job for command " + job.getCmd();
                s_logger.warn(errorMsg);
                throw new ServerApiException(BaseCmd.INTERNAL_ERROR, errorMsg);
            }

            if (objectId != null) {
                return ((BaseAsyncCreateCmd) asyncCmd).getResponse(jobId, objectId);
            }
            return ApiResponseSerializer.toSerializedString(asyncCmd.getResponse(jobId), asyncCmd.getResponseType());
        } else {
            _dispatcher.dispatch(cmdObj, params);

            // if the command is of the listXXXCommand, we will need to also return the
            // the job id and status if possible
            if (cmdObj instanceof BaseListCmd) {
                buildAsyncListResponse((BaseListCmd) cmdObj, caller);
            }
            return ApiResponseSerializer.toSerializedString((ResponseObject) cmdObj.getResponseObject(), cmdObj.getResponseType());
        }
    }

    private void buildAsyncListResponse(BaseListCmd command, Account account) {
        List<ResponseObject> responses = ((ListResponse) command.getResponseObject()).getResponses();
        if (responses != null && responses.size() > 0) {
            List<? extends AsyncJob> jobs = null;

            // list all jobs for ROOT admin
            if (account.getType() == Account.ACCOUNT_TYPE_ADMIN) {
                jobs = _asyncMgr.findInstancePendingAsyncJobs(command.getInstanceType(), null);
            } else {
                jobs = _asyncMgr.findInstancePendingAsyncJobs(command.getInstanceType(), account.getId());
            }

            if (jobs.size() == 0) {
                return;
            }

            // Using maps might possibly be more efficient if the set is large enough but for now, we'll just do a
            // comparison of two lists. Either way, there shouldn't be too many async jobs active for the account.
            for (AsyncJob job : jobs) {
                if (job.getInstanceId() == null) {
                    continue;
                }
                for (ResponseObject response : responses) {
                    if (response.getObjectId() != null && job.getInstanceId().longValue() == response.getObjectId().longValue()) {
                        response.setJobId(job.getId());
                        response.setJobStatus(job.getStatus());
                    }
                }
            }
        }
    }

    private void buildAuditTrail(StringBuffer auditTrailSb, String command, String result) {
        if (result == null) {
            return;
        }
        auditTrailSb.append(" " + HttpServletResponse.SC_OK + " ");
        auditTrailSb.append(result);
        /*
         * if (command.equals("queryAsyncJobResult")){ //For this command we need to also log job status and job resultcode for
         * (Pair<String,Object> pair : resultValues){ String key = pair.first(); if (key.equals("jobstatus")){
         * auditTrailSb.append(" "); auditTrailSb.append(key); auditTrailSb.append("="); auditTrailSb.append(pair.second());
         * }else if (key.equals("jobresultcode")){ auditTrailSb.append(" "); auditTrailSb.append(key); auditTrailSb.append("=");
         * auditTrailSb.append(pair.second()); } } }else { for (Pair<String,Object> pair : resultValues){ if
         * (pair.first().equals("jobid")){ // Its an async job so report the jobid auditTrailSb.append(" ");
         * auditTrailSb.append(pair.first()); auditTrailSb.append("="); auditTrailSb.append(pair.second()); } } }
         */
    }

    private static boolean isCommandAvailable(String commandName) {
        boolean isCommandAvailable = false;
        isCommandAvailable = s_allCommands.contains(commandName);
        return isCommandAvailable;
    }

    public boolean verifyRequest(Map<String, Object[]> requestParameters, Long userId) throws ServerApiException {
        try {
            String apiKey = null;
            String secretKey = null;
            String signature = null;
            String unsignedRequest = null;

            String[] command = (String[]) requestParameters.get("command");
            if (command == null) {
                s_logger.info("missing command, ignoring request...");
                return false;
            }

            String commandName = command[0];

            // if userId not null, that mean that user is logged in
            if (userId != null) {
                Long accountId = ApiDBUtils.findUserById(userId).getAccountId();
                Account userAccount = _ms.findAccountById(accountId);
                short accountType = userAccount.getType();

                if (!isCommandAvailable(accountType, commandName)) {
                    s_logger.warn("The given command:" + commandName + " does not exist");
                    throw new ServerApiException(BaseCmd.UNSUPPORTED_ACTION_ERROR, "The given command:" + commandName + " does not exist");
                }
                return true;
            } else {
                // check against every available command to see if the command exists or not
                if (!isCommandAvailable(commandName) && !commandName.equals("login") && !commandName.equals("logout")) {
                    s_logger.warn("The given command:" + commandName + " does not exist");
                    throw new ServerApiException(BaseCmd.UNSUPPORTED_ACTION_ERROR, "The given command:" + commandName + " does not exist");
                }
            }

            // - build a request string with sorted params, make sure it's all lowercase
            // - sign the request, verify the signature is the same
            List<String> parameterNames = new ArrayList<String>();

            for (Object paramNameObj : requestParameters.keySet()) {
                parameterNames.add((String) paramNameObj); // put the name in a list that we'll sort later
            }

            Collections.sort(parameterNames);

            String signatureVersion = null;
            String expires = null;
            
            for (String paramName : parameterNames) {
                // parameters come as name/value pairs in the form String/String[]
                String paramValue = ((String[]) requestParameters.get(paramName))[0];

                if ("signature".equalsIgnoreCase(paramName)) {
                    signature = paramValue;
                } else {
                    if ("apikey".equalsIgnoreCase(paramName)) {
                        apiKey = paramValue;
                    }
                    else if ("signatureversion".equalsIgnoreCase(paramName)) {
                    	signatureVersion = paramValue;
                    } else if ("expires".equalsIgnoreCase(paramName)) {
                    	expires = paramValue;
                    }
                    
                    if (unsignedRequest == null) {
                        unsignedRequest = paramName + "=" + URLEncoder.encode(paramValue, "UTF-8").replaceAll("\\+", "%20");
                    } else {
                        unsignedRequest = unsignedRequest + "&" + paramName + "=" + URLEncoder.encode(paramValue, "UTF-8").replaceAll("\\+", "%20");
                    }
                }
            }

            // if api/secret key are passed to the parameters
            if ((signature == null) || (apiKey == null)) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.info("expired session, missing signature, or missing apiKey -- ignoring request...sig: " + signature + ", apiKey: " + apiKey);
                }
                return false; // no signature, bad request
            }

            Date expiresTS = null;
            if("3".equals(signatureVersion)){
            	// New signature authentication. Check for expire parameter and its validity
            	if(expires == null){
            		s_logger.info("missing Expires parameter -- ignoring request...sig: " + signature + ", apiKey: " + apiKey);
            		return false;
            	}
            	synchronized (_dateFormat) {
            		expiresTS = _dateFormat.parse(expires);
            	}
            	Date now = new Date(System.currentTimeMillis());
            	if(expiresTS.before(now)){
            		s_logger.info("Request expired -- ignoring ...sig: " + signature + ", apiKey: " + apiKey);
            		return false;
            	}
            	if(_cache.isKeyInCache(signature)){
            		s_logger.info("Duplicate signature -- ignoring ...sig: " + signature + ", apiKey: " + apiKey);
            		return false;
            	}
            }
            
            Transaction txn = Transaction.open(Transaction.CLOUD_DB);
            txn.close();
            User user = null;
            // verify there is a user with this api key
            Pair<User, Account> userAcctPair = _ms.findUserByApiKey(apiKey);
            if (userAcctPair == null) {
                s_logger.info("apiKey does not map to a valid user -- ignoring request, apiKey: " + apiKey);
                return false;
            }

            user = userAcctPair.first();
            Account account = userAcctPair.second();

            if (user.getState() != Account.State.enabled || !account.getState().equals(Account.State.enabled)) {
                s_logger.info("disabled or locked user accessing the api, userid = " + user.getId() + "; name = " + user.getUsername() + "; state: " + user.getState() + "; accountState: "
                        + account.getState());
                return false;
            }

            UserContext.updateContext(user.getId(), account, null);

            if (!isCommandAvailable(account.getType(), commandName)) {
                s_logger.warn("The given command:" + commandName + " does not exist");
                throw new ServerApiException(BaseCmd.UNSUPPORTED_ACTION_ERROR, "The given command:" + commandName + " does not exist");
            }

            // verify secret key exists
            secretKey = user.getSecretKey();
            if (secretKey == null) {
                s_logger.info("User does not have a secret key associated with the account -- ignoring request, username: " + user.getUsername());
                return false;
            }

            unsignedRequest = unsignedRequest.toLowerCase();

            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
            mac.init(keySpec);
            mac.update(unsignedRequest.getBytes());
            byte[] encryptedBytes = mac.doFinal();
            String computedSignature = Base64.encodeBytes(encryptedBytes);
            boolean equalSig = signature.equals(computedSignature);
            if (!equalSig) {
                s_logger.info("User signature: " + signature + " is not equaled to computed signature: " + computedSignature);
            } else {
            	if("3".equals(signatureVersion)){
            		//Add signature along with its time to live calculated based on expires timestamp
            		_cache.put(new Element(signature, "", false, 0, (int)(expiresTS.getTime() - System.currentTimeMillis())/1000));
            	}
            }
            return equalSig;
        } catch (Exception ex) {
            if (ex instanceof ServerApiException && ((ServerApiException) ex).getErrorCode() == BaseCmd.UNSUPPORTED_ACTION_ERROR) {
                throw (ServerApiException) ex;
            }
            s_logger.error("unable to verifty request signature", ex);
        }
        return false;
    }

    public void loginUser(HttpSession session, String username, String password, Long domainId, String domainPath, Map<String, Object[]> requestParameters) throws CloudAuthenticationException {
        // We will always use domainId first. If that does not exist, we will use domain name. If THAT doesn't exist
        // we will default to ROOT
        if (domainId == null) {
            if (domainPath == null || domainPath.trim().length() == 0) {
                domainId = DomainVO.ROOT_DOMAIN;
            } else {
                Domain domainObj = _ms.findDomainByPath(domainPath);
                if (domainObj != null) {
                    domainId = domainObj.getId();
                } else { // if an unknown path is passed in, fail the login call
                    throw new CloudAuthenticationException("Unable to find the domain from the path " + domainPath);
                }
            }
        }

        UserAccount userAcct = _ms.authenticateUser(username, password, domainId, requestParameters);
        if (userAcct != null) {
            String timezone = userAcct.getTimezone();
            float offsetInHrs = 0f;
            if (timezone != null) {
                TimeZone t = TimeZone.getTimeZone(timezone);
                s_logger.info("Current user logged in under " + timezone + " timezone");

                java.util.Date date = new java.util.Date();
                long longDate = date.getTime();
                float offsetInMs = (t.getOffset(longDate));
                offsetInHrs = offsetInMs / (1000 * 60 * 60);
                s_logger.info("Timezone offset from UTC is: " + offsetInHrs);
            }

            Account account = _ms.findAccountById(userAcct.getAccountId());

            // set the userId and account object for everyone
            session.setAttribute("userid", userAcct.getId());
            session.setAttribute("username", userAcct.getUsername());
            session.setAttribute("firstname", userAcct.getFirstname());
            session.setAttribute("lastname", userAcct.getLastname());
            session.setAttribute("accountobj", account);
            session.setAttribute("account", account.getAccountName());
            session.setAttribute("domainid", account.getDomainId());
            session.setAttribute("type", Short.valueOf(account.getType()).toString());
            session.setAttribute("registrationtoken", userAcct.getRegistrationToken());
            session.setAttribute("registered", new Boolean(userAcct.isRegistered()).toString());

            if (timezone != null) {
                session.setAttribute("timezone", timezone);
                session.setAttribute("timezoneoffset", Float.valueOf(offsetInHrs).toString());
            }

            // (bug 5483) generate a session key that the user must submit on every request to prevent CSRF, add that
            // to the login response so that session-based authenticators know to send the key back
            SecureRandom sesssionKeyRandom = new SecureRandom();
            byte sessionKeyBytes[] = new byte[20];
            sesssionKeyRandom.nextBytes(sessionKeyBytes);
            String sessionKey = Base64.encodeBytes(sessionKeyBytes);
            session.setAttribute("sessionkey", sessionKey);

            return;
        }
        throw new CloudAuthenticationException("Unable to find user " + username + " in domain " + domainId);
    }

    public void logoutUser(long userId) {
        _ms.logoutUser(Long.valueOf(userId));
        return;
    }

    public boolean verifyUser(Long userId) {
        User user = _ms.findUserById(userId);
        Account account = null;
        if (user != null) {
            account = _ms.findAccountById(user.getAccountId());
        }

        if ((user == null) || (user.getRemoved() != null) || !user.getState().equals(Account.State.enabled) || (account == null) || !account.getState().equals(Account.State.enabled)) {
            s_logger.warn("Deleted/Disabled/Locked user with id=" + userId + " attempting to access public API");
            return false;
        }
        return true;
    }

    public static boolean isCommandAvailable(short accountType, String commandName) {
        boolean isCommandAvailable = false;
        switch (accountType) {
        case Account.ACCOUNT_TYPE_ADMIN:
            isCommandAvailable = s_adminCommands.contains(commandName);
            break;
        case Account.ACCOUNT_TYPE_DOMAIN_ADMIN:
            isCommandAvailable = s_resellerCommands.contains(commandName);
            break;
        case Account.ACCOUNT_TYPE_RESOURCE_DOMAIN_ADMIN:
            isCommandAvailable = s_resourceDomainAdminCommands.contains(commandName);
            break;
        case Account.ACCOUNT_TYPE_NORMAL:
            isCommandAvailable = s_userCommands.contains(commandName);
            break;
        }
        return isCommandAvailable;
    }

    // FIXME: rather than isError, we might was to pass in the status code to give more flexibility
    private void writeResponse(HttpResponse resp, final String responseText, final int statusCode, String responseType, String reasonPhrase) {
        try {
            resp.setStatusCode(statusCode);
            resp.setReasonPhrase(reasonPhrase);

            BasicHttpEntity body = new BasicHttpEntity();
            if (BaseCmd.RESPONSE_TYPE_JSON.equalsIgnoreCase(responseType)) {
                // JSON response
                body.setContentType("text/javascript");
                if (responseText == null) {
                    body.setContent(new ByteArrayInputStream("{ \"error\" : { \"description\" : \"Internal Server Error\" } }".getBytes("UTF-8")));
                }
            } else {
                body.setContentType("text/xml");
                if (responseText == null) {
                    body.setContent(new ByteArrayInputStream("<error>Internal Server Error</error>".getBytes("UTF-8")));
                }
            }

            if (responseText != null) {
                body.setContent(new ByteArrayInputStream(responseText.getBytes("UTF-8")));
            }
            resp.setEntity(body);
        } catch (Exception ex) {
            s_logger.error("error!", ex);
        }
    }

    // FIXME: the following two threads are copied from
    // http://svn.apache.org/repos/asf/httpcomponents/httpcore/trunk/httpcore/src/examples/org/apache/http/examples/ElementalHttpServer.java
    // we have to cite a license if we are using this code directly, so we need to add the appropriate citation or modify the
    // code to be very specific to our needs
    static class ListenerThread extends Thread {
        private HttpService _httpService = null;
        private ServerSocket _serverSocket = null;
        private HttpParams _params = null;

        public ListenerThread(ApiServer requestHandler, int port) {
            try {
                _serverSocket = new ServerSocket(port);
            } catch (IOException ioex) {
                s_logger.error("error initializing api server", ioex);
                return;
            }

            _params = new BasicHttpParams();
            _params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 30000).setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                    .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false).setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                    .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");

            // Set up the HTTP protocol processor
            BasicHttpProcessor httpproc = new BasicHttpProcessor();
            httpproc.addInterceptor(new ResponseDate());
            httpproc.addInterceptor(new ResponseServer());
            httpproc.addInterceptor(new ResponseContent());
            httpproc.addInterceptor(new ResponseConnControl());

            // Set up request handlers
            HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
            reqistry.register("*", requestHandler);

            // Set up the HTTP service
            _httpService = new HttpService(httpproc, new NoConnectionReuseStrategy(), new DefaultHttpResponseFactory());
            _httpService.setParams(_params);
            _httpService.setHandlerResolver(reqistry);
        }

        @Override
        public void run() {
            s_logger.info("ApiServer listening on port " + _serverSocket.getLocalPort());
            while (!Thread.interrupted()) {
                try {
                    // Set up HTTP connection
                    Socket socket = _serverSocket.accept();
                    DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                    conn.bind(socket, _params);

                    // Execute a new worker task to handle the request
                    _executor.execute(new WorkerTask(_httpService, conn, _workerCount++));
                } catch (InterruptedIOException ex) {
                    break;
                } catch (IOException e) {
                    s_logger.error("I/O error initializing connection thread", e);
                    break;
                }
            }
        }
    }

    static class WorkerTask implements Runnable {
        private final HttpService _httpService;
        private final HttpServerConnection _conn;

        public WorkerTask(final HttpService httpService, final HttpServerConnection conn, final int count) {
            _httpService = httpService;
            _conn = conn;
        }

        @Override
        public void run() {
            HttpContext context = new BasicHttpContext(null);
            try {
                while (!Thread.interrupted() && _conn.isOpen()) {
                    try {
                        _httpService.handleRequest(_conn, context);
                        _conn.close();
                    } finally {
                        StackMaid.current().exitCleanup();
                    }
                }
            } catch (ConnectionClosedException ex) {
                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("ApiServer:  Client closed connection");
                }
            } catch (IOException ex) {
                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("ApiServer:  IOException - " + ex);
                }
            } catch (HttpException ex) {
                s_logger.warn("ApiServer:  Unrecoverable HTTP protocol violation" + ex);
            } finally {
                try {
                    _conn.shutdown();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public String getSerializedApiError(int errorCode, String errorText, Map<String, Object[]> apiCommandParams, String responseType) {
        String responseName = null;
        String cmdClassName = null;

        String responseText = null;

        try {
            if (errorCode == BaseCmd.UNSUPPORTED_ACTION_ERROR || apiCommandParams == null || apiCommandParams.isEmpty()) {
                responseName = "errorresponse";
            } else {
                String cmdName = ((String[]) apiCommandParams.get("command"))[0];
                cmdClassName = _apiCommands.getProperty(cmdName);
                if (cmdClassName != null) {
                    Class<?> claz = Class.forName(cmdClassName);
                    responseName = ((BaseCmd) claz.newInstance()).getCommandName();
                } else {
                    responseName = "errorresponse";
                }
            }

            ExceptionResponse apiResponse = new ExceptionResponse();
            apiResponse.setErrorCode(errorCode);
            apiResponse.setErrorText(errorText);
            apiResponse.setResponseName(responseName);
            responseText = ApiResponseSerializer.toSerializedString(apiResponse, responseType);

        } catch (Exception e) {
            s_logger.error("Exception responding to http request", e);
        }
        return responseText;
    }

    protected Cache _cache;
    protected void createCache() {
    	final CacheManager cm = CacheManager.create();
    	//ToDo: Make following values configurable
    	final int maxElements = 100;
    	final int live = 300;
    	final int idle = 300;
    	_cache = new Cache("signaturesCache", maxElements, false, false, live, idle);
    	cm.addCache(_cache);
    	s_logger.info("Cache created: " + _cache.toString());
    }
}
