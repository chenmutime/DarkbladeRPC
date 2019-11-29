package com.darkblade.rpc.core.proxy;

import com.darkblade.rpc.common.constant.StatusCodeConstant;
import com.darkblade.rpc.common.dto.NrpcRequest;
import com.darkblade.rpc.common.dto.NrpcResponse;
import com.darkblade.rpc.core.annotation.NrpcClient;
import com.darkblade.rpc.core.registry.ServiceManager;
import com.darkblade.rpc.core.exception.RemoteServerException;
import com.darkblade.rpc.core.future.RpcFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

public class ObjectProxy implements InvocationHandler {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Class<?> interfaceClass;

    public ObjectProxy(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (interfaceClass.isAnnotationPresent(NrpcClient.class)) {
            NrpcClient annotation = interfaceClass.getAnnotation(NrpcClient.class);
            logger.info("执行同步发送请求:{}", annotation.serviceName());
            NrpcRequest nrpcRequest = createRequest(method, args);
            NrpcResponse nrpcResponse = executeRequest(1, annotation, nrpcRequest);
            return nrpcResponse.getBody();
        }
        return null;
    }

    private NrpcResponse executeRequest(int currRetries, NrpcClient annotation, NrpcRequest nrpcRequest) throws Exception {
        int retries = annotation.retries();
        Optional<RpcFuture> rpcFutureOptional = ServiceManager.sendRequest(annotation.serviceName(), nrpcRequest);
        if(rpcFutureOptional.isPresent()) {
            RpcFuture rpcFuture = rpcFutureOptional.get();
            NrpcResponse response = rpcFuture.get(annotation.timeout(), annotation.timeUnit());
            if (null == response) {
                if (currRetries < retries) {
                    logger.info("重试请求服务端");
                    executeRequest(++currRetries, annotation, nrpcRequest);
                }
                throw new RemoteServerException("response is null");
            }
            if (StatusCodeConstant.SUCCESS != response.getCode()) {
                logger.error(response.getError());
                throw new RemoteServerException(response.getError());
            }
            return response;
        }
        return null;
    }

    private NrpcRequest createRequest(Method method, Object[] args) {
        NrpcRequest nrpcRequest = new NrpcRequest();
        nrpcRequest.setRequestId(UUID.randomUUID().toString());
        nrpcRequest.setCalssName(interfaceClass.getSimpleName());
        nrpcRequest.setMethodName(method.getName());
        nrpcRequest.setParameters(args);
        nrpcRequest.setParameterTypes(method.getParameterTypes());
        return nrpcRequest;
    }

}
