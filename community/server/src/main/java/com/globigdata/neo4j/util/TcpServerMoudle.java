package com.globigdata.neo4j.util;

import org.neo4j.server.database.InjectableProvider;
import org.neo4j.server.modules.ServerModule;

import java.util.Collection;

/**
 * Created by WangTao 2016/4/18.
 * 定义Tcp服务模块接口
 */
public interface TcpServerMoudle extends ServerModule {

    public TcpMoudleType getMoudleType();

    public Service getService();

    public void setDependencies(TcpServer.Dependencies dependencies);

}
