package com.darkblade.rpc.common.dto;

import java.io.Serializable;

public class NrpcRequest implements Serializable {

    private String requestId;

    private String calssName;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] parameters;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getCalssName() {
        return calssName;
    }

    public void setCalssName(String calssName) {
        this.calssName = calssName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}
