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
            this.address = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
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
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        log.info("client socket connected...");
                        new ClientSocketTask(socket).start();
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }).start();
    }

    @Override
    public void stop() {
//        for(TcpServerMoudle moudle : moudleMap.values()) {
//            moudle.stop();
//        }
        for (Socket socket : socketMap.values()) {
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
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
        if (moudle != null) {
            moudle.stop();
        }
    }

    @Override
    public void setDependencies(Dependencies dependencies) {
        this.dependencies = dependencies;
    }

    public TcpResponse onRequest(Socket socket, TcpRequest request) {
        String clientId = request.getClientId();
        socketMap.putIfAbsent(clientId, socket);

        TcpMoudleType moudleType = TcpMoudleType.valueOf(request.getMoudleType().toUpperCase());
        OperateType operateType = OperateType.valueOf(request.getOperateType().toUpperCase());

        TcpServerMoudle moudle = moudleMap.get(moudleType);
        TcpResponse response = moudle.getService().execute(operateType, request);

        return response;
    }

    public void onResponse(Socket socket, TcpResponse response) throws IOException {
        writeObject(socket, response);
    }

    public void write(Socket socket, byte[] bytes) throws IOException{
            OutputStream os = socket.getOutputStream();
            os.write(bytes);
            os.flush();
    }

    public void removeSocket(Socket socket) {
        for (Map.Entry<String, Socket> entry : socketMap.entrySet()) {
            if (entry.getValue() == socket) {
                socketMap.remove(entry.getKey());
            }
        }

        if (socket != null) {
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    public void writeObject(Socket socket, Object obj) throws IOException{
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(obj);
            oos.flush();
    }

    public <T> T readObject(Socket socket, Class<T> clazz) throws ClassNotFoundException, IOException {
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        Object obj = ois.readObject();
        if (obj != null && obj.getClass() == clazz) {
            return (T) obj;
        }
        return null;
    }

    public String getClient(Socket socket) {
        String client = null;
        for (Map.Entry<String, Socket> entry : socketMap.entrySet()) {
            if (entry.getValue() == socket) {
                client = entry.getKey();
            }
        }
        return client;
    }

//    public byte[] read(Socket socket) {
//        byte[] bytes = new byte[1024];
//        int pos = -1;
//        try {
//            InputStream in = socket.getInputStream();
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            while ((pos = in.read(bytes)) > 0) {
//                bos.write(bytes, 0, pos);
//            }
//            return bos.toByteArray();
//        } catch (IOException e) {
//            log.error(e.getMessage());
//            removeSocket(socket);
//        }
//        return null;
//    }

    class ClientSocketTask implements Runnable {
        private Socket socket;
        private volatile boolean isRun;

        public ClientSocketTask(Socket socket) {
            this.socket = socket;
            isRun = false;
        }

        public void start() {
            this.isRun = true;
            new Thread(this).start();
        }

        public void stop() {
            this.isRun = false;
        }

        @Override
        public void run() {
            while (isRun) {
                log.info("%s wait for read data...");
                try {
                    TcpRequest request = readObject(socket, TcpRequest.class);
                    if (request != null) {
                        TcpResponse response = onRequest(socket, request);
                        onResponse(socket, response);
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("{%s} client with {%s} address connection closed...", getClient(socket), socket.getInetAddress().toString());
                    this.stop();
                    removeSocket(socket);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    log.error(e.getMessage());
                    this.stop();
                    removeSocket(socket);
                }
            }
        }

    }
}
