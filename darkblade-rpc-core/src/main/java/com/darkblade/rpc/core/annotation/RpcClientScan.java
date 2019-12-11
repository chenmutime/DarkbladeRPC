package com.darkblade.rpc.core.annotation;

import java.lang.annotation.*;

/**
 * 用于扫描注解了RpcClient的类
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface RpcClientScan {

    String basePackage() default "";

}
