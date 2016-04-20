package com.globigdata.neo4j.util;

import org.neo4j.server.database.InjectableProvider;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by WangTao on 2016/4/18.
 */
public class CypherServerMoudle implements TcpServerMoudle {

    private Service service;
    private TcpServer.Dependencies dependencies;

    public CypherServerMoudle() {
    }

    @Override
    public TcpMoudleType getMoudleType() {
        return TcpMoudleType.CYPHER;
    }

    @Override
    public Service getService() {
        return service;
    }

    @Override
    public void setDependencies(TcpServer.Dependencies dependencies) {
        this.dependencies = dependencies;
    }

    public void setService(Service service) {
        this.service = service;
    }

    @Override
    public void start() {
        service = new CypherService(this.dependencies);
    }

    @Override
    public void stop() {
        service = null;
    }
}
