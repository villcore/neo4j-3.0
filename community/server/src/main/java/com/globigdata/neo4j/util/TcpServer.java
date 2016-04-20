package com.globigdata.neo4j.util;

import org.neo4j.register.Register;
import org.neo4j.server.plugins.Injectable;

import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by WangTao 2016/4/18.
 * 用于提供TCP服务的接口
 */
public interface TcpServer {

    static class Dependencies {
        private static final Map<Class, Object> CLASS_OBJECT_MAP = new HashMap<>();

        public static void add(Class<?> clazz, Object obj) {
            CLASS_OBJECT_MAP.put(clazz, obj);
        }

        @SuppressWarnings("unchecked")
        public static <T> T get(Class<T> clazz) {
            return (T) CLASS_OBJECT_MAP.get(clazz);
        }
    }
    void setAddress(String address);

    void setAddress(InetAddress address);

    void setPort(int port);

    void start() throws Exception;

    void stop();

    void addMoudle(TcpMoudleType moudleType, TcpServerMoudle moudle);

    void removeMoudle(TcpMoudleType moudleType);

    void setDependencies(TcpServer.Dependencies dependencies);
}
