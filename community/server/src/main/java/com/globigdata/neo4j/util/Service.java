package com.globigdata.neo4j.util;

/**
 * Created by WangTao on 2016/4/18.
 */
public interface Service {
    public TcpResponse execute(OperateType operateType, TcpRequest request);
}
