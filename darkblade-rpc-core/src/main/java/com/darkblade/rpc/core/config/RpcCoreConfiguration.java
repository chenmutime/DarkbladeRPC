package com.darkblade.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@EnableConfigurationProperties
@ComponentScan(basePackages = {"com.darkblade.rpc.core"})
public class RpcCoreConfiguration {

    //    服务注册与发现中心选用
    protected String serviceCenter;
    //     序列化工具选用
    protected String serializer;
    //     动态代理方式，默认JDK动态代理
    protected Boolean proxyTargetClass;
}
