package com.darkblade.rpc.core.registry;

import com.darkblade.rpc.core.annotation.NrpcClient;
import com.darkblade.rpc.core.annotation.NrpcClients;
import com.darkblade.rpc.core.proxy.ObjectProxy;
import com.darkblade.rpc.core.proxy.ProxyBuidler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class NrpcClientRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware, EnvironmentAware {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private DefaultListableBeanFactory beanFactory;

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        logger.info("正在扫描注解了NrpcClient的类");
        ClassPathScanningCandidateComponentProvider scanner = this.getScanner();
        Map<String, Object> attrs = metadata.getAnnotationAttributes(NrpcClients.class.getName(), true);
        String besePackage = (String) attrs.get("basePackage");
//        添加只抓取注释了NrpcClient的过滤器
        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(NrpcClient.class);
        scanner.addIncludeFilter(annotationTypeFilter);
//          抓取所有注解了@ComPonent的类
        Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(besePackage);
        for (BeanDefinition beanDefinition : candidateComponents) {
            String beanClassName = beanDefinition.getBeanClassName();
            try {
//                接口类
                Class interfaceClass = Class.forName(beanClassName);
//                生成代理类
                Optional<Object> objectOptional = ProxyBuidler.build(interfaceClass);
                if (objectOptional.isPresent()) {
                    Object objectProxyClass = objectOptional.get();
                    BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(objectProxyClass.getClass());
//                    由于代理类objectProxyClass需要构造传参，如果不传，注册bean将会失败
//                    可能你会疑问，为什么beanDefinitionBuilder.addConstructorArgValue传入的参数是ObjectProxy而不是interfaceClass
//                    那是因为objectProxyClass是一个代理类，并非ObjectProxy对象，其内部的构造参数类型就是InvocationHandler
                    beanDefinitionBuilder.addConstructorArgValue(new ObjectProxy(interfaceClass));
                    AbstractBeanDefinition proxyBeanDefinition = beanDefinitionBuilder.getBeanDefinition();
                    beanFactory.registerBeanDefinition(beanClassName, proxyBeanDefinition);
                }
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage());
            }
        }

    }


    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                boolean isCandidate = false;
                if (beanDefinition.getMetadata().isIndependent() && !beanDefinition.getMetadata().isAnnotation()) {
                    isCandidate = true;
                }

                return isCandidate;
            }
        };
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }
}
