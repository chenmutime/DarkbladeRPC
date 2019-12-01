package com.darkblade.rpc.core.annotation;

import com.darkblade.rpc.core.config.AutoConfiguration;
import com.darkblade.rpc.core.bean.RpcClientRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({RpcClientRegister.class, AutoConfiguration.class})
public @interface EnableServerDiscovery {

}
