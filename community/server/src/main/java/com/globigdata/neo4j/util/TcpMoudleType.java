package com.globigdata.neo4j.util;

/**
 * Created by WangTao 2016/4/18.
 * 定义Tcp模块枚举类型,用于解析请求类型
 */
public enum TcpMoudleType {

    CYPHER("cypher");

    private final String type;

    private TcpMoudleType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }
}
