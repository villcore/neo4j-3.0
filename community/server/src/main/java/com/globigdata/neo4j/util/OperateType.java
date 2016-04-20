package com.globigdata.neo4j.util;

/**
 * Created by WangTao on 2016/4/18.
 * 定义请求服务的具体操作
 */
public enum OperateType {

    EXECUTE("execute");

    private String type;

    private OperateType (String type) {
        this.type = type;
    }

    public String getType () {
        return this.type;
    }

    @Override
    public String toString() {
        return type;
    }
}
