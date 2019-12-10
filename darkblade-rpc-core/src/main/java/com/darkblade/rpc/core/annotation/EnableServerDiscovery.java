package com.darkblade.rpc.core.annotation;

import com.darkblade.rpc.core.config.RpcCoreConfiguration;
import com.darkblade.rpc.core.bean.RpcClientRegister;
import com.darkblade.rpc.core.discovery.DarkbladeClientBootstrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({RpcClientRegister.class, DarkbladeClientBootstrap.class, RpcCoreConfiguration.class})
public @interface EnableServerDiscovery {

}
