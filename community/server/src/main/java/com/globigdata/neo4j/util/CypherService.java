package com.globigdata.neo4j.util;

import com.sun.xml.internal.ws.org.objectweb.asm.MethodVisitor;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.Result;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.core.NodeProxy;
import org.neo4j.kernel.impl.query.QueryExecutionEngine;
import org.neo4j.kernel.impl.query.QuerySession;
import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;
import org.neo4j.server.database.CypherExecutor;
import org.neo4j.server.plugins.PluginManager;
import org.neo4j.server.rest.dbms.AuthorizedRequestWrapper;
import org.neo4j.server.rest.repr.*;
import org.neo4j.server.rest.repr.formats.JsonFormat;
import org.neo4j.udc.UsageData;
import org.neo4j.unsafe.impl.batchimport.input.Input;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by WangTao on 2016/4/18.
 */
public class CypherService implements Service {

    public static final Log log = LogUtil.getLog(CypherService.class);

    private static final String PARAMS_KEY = "params";
    private static final String QUERY_KEY = "query";

    private final CypherExecutor cypherExecutor;
    private final InputFormat input;
    private final OutputFormat output;

    private final DefaultFormat defaultFormat;
    private final PluginManager pluginManager;

    public CypherService(TcpServer.Dependencies dependencies)
    {
        this.cypherExecutor = dependencies.get(CypherExecutor.class);
//        this.input = dependencies.get(InputFormat.class);
//        this.output = dependencies.get(OutputFormat.class);
        this.input = new JsonFormat();


        Collection<MediaType> supported = new LinkedList<>();
        supported.add(MediaType.APPLICATION_JSON_TYPE);
        defaultFormat = new DefaultFormat(new JsonFormat(), supported, MediaType.APPLICATION_JSON_TYPE);

        Config serverConfig = dependencies.get(Config.class);
        LogProvider logProvider = dependencies.get(LogProvider.class);
        pluginManager = new PluginManager(serverConfig, logProvider);
        this.output = new OutputFormat(defaultFormat, URI.create("http://localhost:7474/db/data/"), pluginManager);
    }


    @Override
    public TcpResponse execute(OperateType operateType, TcpRequest request) {
        TcpResponse response = new TcpResponse(request.getRequestId(), request.getClientId(), System.currentTimeMillis(), "error", "", "null");
        switch (operateType) {
            case EXECUTE :
                response = cypher(request);
                break;
            default:
                break;
        }
        return response;
    }

    public TcpResponse cypher(TcpRequest request) {
        TcpResponse response = new TcpResponse(request.getRequestId(), request.getClientId(), System.currentTimeMillis(), "", "", "null");

        Map<String, Object> command = null;
        try {
            command = input.readMap(request.getData());
        } catch (BadInputException e) {
            response.setStatus("error");
            response.setMsg(e.getMessage());
        }

        if(command == null) {
            return response;
        }

        if( !command.containsKey(QUERY_KEY) ) {
            response.setStatus("error");
            response.setMsg("no query content!");
        }

        String query = (String) command.get( QUERY_KEY );
        Map<String, Object> params = null;
        try
        {
            params = (Map<String, Object>) (command.containsKey( PARAMS_KEY ) && command.get( PARAMS_KEY ) != null ?
                    command.get( PARAMS_KEY ) :
                    new HashMap<String, Object>());
        }
        catch ( ClassCastException e )
        {
            response.setStatus("error");
            response.setMsg("Parameters must be a JSON map " + e.getMessage());
        }

        if(params == null) {
            return response;
        }

        try
        {
            QueryExecutionEngine executionEngine = cypherExecutor.getExecutionEngine();
//            //TODO 暂时使用传入null值替换
          QuerySession querySession = cypherExecutor.createSession(null);
//            //querySession
//            //org.neo4j.cypher.internal.javacompat.ExecutionEngine
//            //log.info(executionEngine.getClass().getName());
//                    //HttpServletRequest httpRequest =
////            AuthorizedRequestWrapper authorizedRequestWrapper = new AuthorizedRequestWrapper("BASIC", "neo4j", new MockHttpRequest());
////
////            QuerySession querySession = cypherExecutor.createSession(authorizedRequestWrapper);
 ObjectMapper mapper = new ObjectMapper();

            Result result = executionEngine.executeQuery(query, params, querySession);

            while(result.hasNext()) {
                Map<String, Object> map = result.next();
                for(String key : result.columns()) {
                    log.info("%s -> %s", key, map.get(key));
                }
            }
            log.info("\n" + result.resultAsString());
//            log.info("colums = %s", result.columns().toString());
            boolean includePlan = result.getQueryExecutionType().requestedExecutionPlanDescription();
//
            CypherResultRepresentation cypherResultRepresentation = new CypherResultRepresentation( result, false, false );

           //Method serializeMethod = MappingRepresentation.class.getDeclaredMethod("serialize", new Class[]{RepresentationFormat.class, URI.class, ExtensionInjector.class});
//           String str = cypherResultRepresentation.defaultSerialize(defaultFormat, URI.create("http://localhost:7474/db/data/"), pluginManager);
            log.info(mapper.writeValueAsString(result.columns()));
            log.info("\n" + result.resultAsString());
//
           JsonFormat jsonFormat = new JsonFormat();
             Response responsePojo = output.ok(cypherResultRepresentation);
            byte[] respBytes = (byte[])responsePojo.getEntity();
            String respStr = new String(respBytes, "utf-8");
            log.info("=====================================================================");
            log.info("\n" + respStr);

//            response.setStatus("success");
//           response.setData(respStr);
        }
        catch ( Throwable e )
        {
            e.printStackTrace();
            response.setStatus("error");
            response.setMsg(e.getMessage());
        }
        return response;
    }
}
