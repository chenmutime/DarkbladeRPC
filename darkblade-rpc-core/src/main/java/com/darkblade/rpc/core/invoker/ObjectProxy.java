package com.darkblade.rpc.core.invoker;

import com.darkblade.rpc.common.constant.StatusCodeConstant;
import com.darkblade.rpc.common.dto.RpcRequest;
import com.darkblade.rpc.common.dto.RpcResponse;
import com.darkblade.rpc.core.annotation.RpcClient;
import com.darkblade.rpc.core.server.ServiceMetadataManager;
import com.darkblade.rpc.core.exception.RemoteServerException;
import com.darkblade.rpc.core.context.RpcContext;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

public class ObjectProxy implements InvocationHandler {

    private Class<?> interfaceClass;

    private RpcClient rpcClient;

    public ObjectProxy(Class<?> interfaceClass, RpcClient rpcClient) {
        this.interfaceClass = interfaceClass;
        this.rpcClient = rpcClient;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        RpcRequest rpcRequest = this.wrapRequest(method, args);
        Optional<RpcResponse> rpcResponseOptional = this.executeRequest(1, this.rpcClient, rpcRequest);
        return rpcResponseOptional.isPresent() ? (rpcResponseOptional.get()).getBody() : null;
    }

    private Optional<RpcResponse> executeRequest(int currRetries, RpcClient annotation, RpcRequest rpcRequest) {
        int retries = annotation.retries();
        Optional<RpcContext> rpcFutureOptional = ServiceMetadataManager.sendRequest(annotation.serviceName(), rpcRequest);
        RpcResponse rpcResponse = null;
        if (rpcFutureOptional.isPresent()) {
            RpcContext rpcContext = rpcFutureOptional.get();
            try {
                rpcResponse = rpcContext.get(annotation.timeout(), annotation.timeUnit());
                if (null == rpcResponse) {
                    if (currRetries < retries) {
                        executeRequest(++currRetries, annotation, rpcRequest);
                    }
                    throw new RemoteServerException("response is null");
                }
                if (StatusCodeConstant.SUCCESS != rpcResponse.getCode()) {
                    throw new RemoteServerException(rpcResponse.getError());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (RemoteServerException e) {
                e.printStackTrace();
            }
        }
        return Optional.ofNullable(rpcResponse);
    }

    private RpcRequest wrapRequest(Method method, Object[] args) {
        RpcRequest nrpcRequest = new RpcRequest();
        nrpcRequest.setRequestId(UUID.randomUUID().toString());
        nrpcRequest.setCalssName(interfaceClass.getSimpleName());
        nrpcRequest.setMethodName(method.getName());
        nrpcRequest.setParameters(args);
        nrpcRequest.setParameterTypes(method.getParameterTypes());
        return nrpcRequest;
    }

}
