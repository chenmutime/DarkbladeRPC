package com.darkblade.rpc.core.invoker;

import com.darkblade.rpc.common.constant.StatusCodeConstant;
import com.darkblade.rpc.common.dto.RpcRequest;
import com.darkblade.rpc.common.dto.RpcResponse;
import com.darkblade.rpc.core.annotation.RpcClient;
import com.darkblade.rpc.core.server.ServiceMetadataManager;
import com.darkblade.rpc.core.exception.RemoteServerException;
import com.darkblade.rpc.core.context.RpcContext;
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
        if (interfaceClass.isAnnotationPresent(RpcClient.class)) {
            RpcClient annotation = interfaceClass.getAnnotation(RpcClient.class);
            logger.info("执行同步发送请求:{}", annotation.serviceName());
            RpcRequest nrpcRequest = wrapRequest(method, args);
            RpcResponse nrpcResponse = executeRequest(1, annotation, nrpcRequest);
            return nrpcResponse.getBody();
        }
        return null;
    }

    private RpcResponse executeRequest(int currRetries, RpcClient annotation, RpcRequest rpcRequest) throws Exception {
        int retries = annotation.retries();
        Optional<RpcContext> rpcFutureOptional = ServiceMetadataManager.sendRequest(annotation.serviceName(), rpcRequest);
        if(rpcFutureOptional.isPresent()) {
            RpcContext rpcContext = rpcFutureOptional.get();
            RpcResponse response = rpcContext.get(annotation.timeout(), annotation.timeUnit());
            if (null == response) {
                if (currRetries < retries) {
                    logger.info("重试请求服务端");
                    executeRequest(++currRetries, annotation, rpcRequest);
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
