package com.globigdata.neo4j.util;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.cypher.javacompat.internal.GraphDatabaseCypherService;
import org.neo4j.graphdb.Result;
import org.neo4j.kernel.GraphDatabaseQueryService;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.api.security.AccessMode;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.coreapi.InternalTransaction;
import org.neo4j.kernel.impl.coreapi.PropertyContainerLocker;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.kernel.impl.query.*;
import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;
import org.neo4j.server.database.CypherExecutor;
import org.neo4j.server.plugins.PluginManager;
import org.neo4j.server.rest.repr.*;
import org.neo4j.server.rest.repr.formats.JsonFormat;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
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

    public CypherService(TcpServer.Dependencies dependencies) {
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
            case EXECUTE:
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

        if (command == null) {
            return response;
        }

        if (!command.containsKey(QUERY_KEY)) {
            response.setStatus("error");
            response.setMsg("no query content!");
        }

        String query = (String) command.get(QUERY_KEY);
        Map<String, Object> params = null;
        try {
            params = (Map<String, Object>) (command.containsKey(PARAMS_KEY) && command.get(PARAMS_KEY) != null ?
                    command.get(PARAMS_KEY) :
                    new HashMap<String, Object>());
        } catch (ClassCastException e) {
            response.setStatus("error");
            response.setMsg("Parameters must be a JSON map " + e.getMessage());
        }

        if (params == null) {
            return response;
        }

        try {
            QueryExecutionEngine executionEngine = cypherExecutor.getExecutionEngine();
            QuerySession querySession = cypherExecutor.createTcpSession();

            /*
            /////////////////////////////////以下代码测试///////////////////////////////////////////
            //手动代码创建session
            final PropertyContainerLocker locker = new PropertyContainerLocker();
            GraphDatabaseCypherService service = (GraphDatabaseCypherService) executionEngine.queryService();

            //!!!!!!!!!!这个可能被移除，不行就使用反射获取
            GraphDatabaseFacade graphDatabaseFacade = service.getGraphDatabaseService();

            Field spiField = GraphDatabaseFacade.class.getDeclaredField("spi");
            spiField.setAccessible(true);
            GraphDatabaseFacade.SPI spi = (GraphDatabaseFacade.SPI) spiField.get(graphDatabaseFacade);

            try(InternalTransaction transaction = service.beginTransaction(KernelTransaction.Type.implicit, AccessMode.Static.FULL )) {
                TransactionalContext transactionalContext = new Neo4jTransactionalContext(service, transaction, spi.currentStatement(), locker);
                Result result = spi.executeQuery(query, params, QueryEngineProvider.embeddedSession(transactionalContext));

                CypherResultRepresentation cypherResultRepresentation = new CypherResultRepresentation(result, false, false);

                Response responsePojo = output.ok(cypherResultRepresentation);
                byte[] respBytes = (byte[]) responsePojo.getEntity();
                String respStr = new String(respBytes, "utf-8");
                log.info("=====================================================================");
                log.info("\n" + respStr);
            }

//            Result result = executionEngine.executeQuery(query, params, querySession);
                */

            Result result = executionEngine.executeQuery(query, params, querySession);

           // log.info("\n" + result.resultAsString());
//            log.info("colums = %s", result.columns().toString());
//
            CypherResultRepresentation cypherResultRepresentation = new CypherResultRepresentation( result, false, false );
            System.out.println(Thread.currentThread().toString());
            //Method serializeMethod = MappingRepresentation.class.getDeclaredMethod("serialize", new Class[]{RepresentationFormat.class, URI.class, ExtensionInjector.class});
//           String str = cypherResultRepresentation.defaultSerialize(defaultFormat, URI.create("http://localhost:7474/db/data/"), pluginManager);
//
            Response responsePojo = output.ok(cypherResultRepresentation);
            byte[] respBytes = (byte[])responsePojo.getEntity();
            String respStr = new String(respBytes, "utf-8");
            log.info("=====================================================================");
            log.info("\n" + respStr);
            //使用如下代码会造成返回结果中没有data
//            while (result.hasNext()) {
//                Map<String, Object> map = result.next();
//                for (String key : result.columns()) {
//                    log.info("%s -> %s", key, map.get(key));
//                }
//            }
//            log.info("\n" + result.resultAsString());
//



//          response.setStatus("success");
//          response.setData(respStr);
        } catch (Throwable e) {
            e.printStackTrace();
            response.setStatus("error");
            response.setMsg(e.getMessage());
        }
        return response;
    }
}
