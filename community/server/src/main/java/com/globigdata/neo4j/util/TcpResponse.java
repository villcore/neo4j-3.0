package com.globigdata.neo4j.util;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by WangTao on 2016/4/18.
 */
public class TcpResponse implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1613762924729822994L;
	private UUID responseId;
    private String clientId;
    private Long timestamp;
    private String status;
    private String msg;
    private String data;

    public TcpResponse(UUID responseId, String clientId, Long timestamp, String status, String msg, String data) {
        this.responseId = responseId;
        this.clientId = clientId;
        this.timestamp = timestamp;
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public UUID getResponseId() {
        return responseId;
    }

    public void setResponseId(UUID responseId) {
        this.responseId = responseId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
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

        TcpResponse that = (TcpResponse) o;

        if (responseId != null ? !responseId.equals(that.responseId) : that.responseId != null) return false;
        if (clientId != null ? !clientId.equals(that.clientId) : that.clientId != null) return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (msg != null ? !msg.equals(that.msg) : that.msg != null) return false;
        return !(data != null ? !data.equals(that.data) : that.data != null);

    }

    @Override
    public int hashCode() {
        int result = responseId != null ? responseId.hashCode() : 0;
        result = 31 * result + (clientId != null ? clientId.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (msg != null ? msg.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TcpResponse{" +
                "responseId=" + responseId +
                ", clientId='" + clientId + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                ", msg='" + msg + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
