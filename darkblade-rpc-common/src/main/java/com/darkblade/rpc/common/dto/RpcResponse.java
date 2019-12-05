package com.darkblade.rpc.common.dto;

import java.io.Serializable;

public class RpcResponse implements Serializable {

    private String requestId;

    private int code;

    private String error;

    private Object body;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "NrpcResponse{" +
                "requestId='" + requestId + '\'' +
                ", code=" + code +
                ", error='" + error + '\'' +
                ", body=" + body +
                '}';
    }
}
