package com.globigdata.neo4j.util;

import org.neo4j.consistency.checking.cache.CacheAccess;
import org.neo4j.logging.Log;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2016/4/18.
 */
public class SocketTcpServer implements TcpServer {

    private static final Log log = LogUtil.getLog(SocketTcpServer.class);

    private InetAddress address;
    private int port;

    private TcpServer.Dependencies dependencies;

    private final Map<TcpMoudleType, TcpServerMoudle> moudleMap = new ConcurrentHashMap<>();
    private final Map<String, Socket> socketMap = new ConcurrentHashMap<>();

    private ServerSocket serverSocket;

    @Override
    public void setAddress(String address) {
        try {
            this.address =InetAddress.getByName(address);
        }
        catch (UnknownHostException e) {
            log.error(e.getMessage());
            this.address = null;
        }
    }

    @Override
    public void setAddress(InetAddress address) {
        this.address = address;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void start() throws Exception {
//        for(TcpServerMoudle moudle : moudleMap.values()) {
//            moudle.start();
//        }
        //主线程接受连接
        serverSocket = new ServerSocket(port);
        new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("TcpServer start listene...");
                while(true) {
                    try {
                        Socket socket = serverSocket.accept();
                        log.info("client socket connected...");
                        new Thread(new ClientSocketTask(socket)).start();
                    }
                    catch (IOException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }).start();
        //单独为每一个连接到的socket开辟一个线程（仅供测试使用)
        //新建一个线程在30s之后进行模拟的请求
        String queryStr = "{\"query\" : \"CREATE (n:DISTRICT1:INSTANCE1:batch_insert_test_0 { props } ) RETURN n\",\"params\": {\"props\" : {\"prop1\":\"value1\",\"prop2\":\"value2\",\"id\":\"1\"} }}";
        TcpRequest request = new TcpRequest("local", UUID.randomUUID(), System.currentTimeMillis(), TcpMoudleType.CYPHER.getType(), OperateType.EXECUTE.getType(), queryStr);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int leftTime = 5;
                while(leftTime > 0) {
                    try {
                        Thread.sleep(1000);
                        log.info("left time to send mock tcp request is [%d]", leftTime);
                        leftTime--;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.info("send mock tcp request...");
                mockOnRequest(request);
            }
        }).start();
    }

    @Override
    public void stop() {
//        for(TcpServerMoudle moudle : moudleMap.values()) {
//            moudle.stop();
//        }
        for(Socket socket : socketMap.values()) {
            if(!socket.isClosed()) {
                try {
                    socket.close();
                }
                catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }


    @Override
    public void addMoudle(TcpMoudleType moudleType, TcpServerMoudle moudle) {
        this.moudleMap.put(moudleType, moudle);
    }

    @Override
    public void removeMoudle(TcpMoudleType moudleType) {
        TcpServerMoudle moudle = moudleMap.get(moudleType);
        if(moudle != null) {
            moudle.stop();
        }
    }

    @Override
    public void setDependencies(Dependencies dependencies) {
        this.dependencies = dependencies;
    }

    public void onRequest(Socket socket, TcpRequest request) {
        String clientId = request.getClientId();
        socketMap.putIfAbsent(clientId, socket);

        TcpMoudleType moudleType = TcpMoudleType.valueOf(request.getMoudleType().toUpperCase());
        OperateType operateType = OperateType.valueOf(request.getOperateType().toUpperCase());

        TcpServerMoudle moudle = moudleMap.get(moudleType);
        TcpResponse response = moudle.getService().execute(operateType, request);

        log.info(response.toString());
        onResponse(socketMap.get(clientId), response);
    }

    public void mockOnRequest(TcpRequest request) {
        String clientId = request.getClientId();

        TcpMoudleType moudleType = TcpMoudleType.valueOf(request.getMoudleType().toUpperCase());
        OperateType operateType = OperateType.valueOf(request.getOperateType().toUpperCase());

        TcpServerMoudle moudle = moudleMap.get(moudleType);
        TcpResponse response = moudle.getService().execute(operateType, request);

        log.info(response.toString());
    }

    public void onResponse(Socket socket, TcpResponse response) {
        writeObject(socket, response);
    }

    public void write(Socket socket, byte[] bytes) {
        try {
            OutputStream os = socket.getOutputStream();
            os.write(bytes);
            os.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
            removeSocket(socket);
        }
    }

    public void removeSocket(Socket socket) {
        for(Map.Entry<String, Socket> entry : socketMap.entrySet()) {
            if(entry.getValue() == socket) {
                socketMap.remove(entry.getKey());
            }
        }
    }

    public void writeObject(Socket socket, Object obj) {
        try{
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(obj);
            oos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
            removeSocket(socket);
        }
    }

    public <T> T readObject(Socket socket, Class<T> clazz) {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Object obj = ois.readObject();
            if(obj.getClass() == clazz) {
                return (T)obj;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            removeSocket(socket);
        }
        catch (ClassNotFoundException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public byte[] read(Socket socket) {
        byte[] bytes = new byte[1024];
        int pos = -1;
        try {
            InputStream in = socket.getInputStream();
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((pos = in.read(bytes)) > 0) {
                bos.write(bytes, 0, pos);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            log.error(e.getMessage());
            removeSocket(socket);
        }
        return null;
    }

    class ClientSocketTask implements Runnable {
        private Socket socket;
        public ClientSocketTask(Socket socket) {
            this.socket = socket;
        }
        @Override
        public void run() {
            while(true) {
                log.info("%s wait for read data...");
                TcpRequest request = readObject(socket, TcpRequest.class);
                log.info(request.toString());
                if(request != null) {
                    onRequest(socket, request);
                }
                }
            }
        }

}
