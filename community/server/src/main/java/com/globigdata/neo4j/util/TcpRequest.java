package com.globigdata.neo4j.util;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by WangTao on 2016/4/18.
 */
public class TcpRequest implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 8703525861933814461L;
	private String clientId;
    private UUID requestId;
    private Long timestamp;
    private String moudleType;
    private String operateType;
    private String data;

    public TcpRequest(String clientId, UUID requestId, Long timestamp, String moudleType, String operateType, String data) {
        this.clientId = clientId;
        this.requestId = requestId;
        this.timestamp = timestamp;
        this.moudleType = moudleType;
        this.operateType = operateType;
        this.data = data;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMoudleType() {
        return moudleType;
    }

    public void setMoudleType(String moudleType) {
        this.moudleType = moudleType;
    }

    public String getOperateType() {
        return operateType;
    }

    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TcpRequest that = (TcpRequest) o;

        if (clientId != null ? !clientId.equals(that.clientId) : that.clientId != null) return false;
        if (requestId != null ? !requestId.equals(that.requestId) : that.requestId != null) return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;
        if (moudleType != that.moudleType) return false;
        if (operateType != that.operateType) return false;
        return !(data != null ? !data.equals(that.data) : that.data != null);

    }

    @Override
    public int hashCode() {
        int result = clientId != null ? clientId.hashCode() : 0;
        result = 31 * result + (requestId != null ? requestId.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (moudleType != null ? moudleType.hashCode() : 0);
        result = 31 * result + (operateType != null ? operateType.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TcpRequest{" +
                "clientId='" + clientId + '\'' +
                ", requestId=" + requestId +
                ", timestamp=" + timestamp +
                ", moudleType=" + moudleType +
                ", operateType=" + operateType +
                ", data='" + data + '\'' +
                '}';
    }
}
