**使用手册**  
将如上几个模块下载后clean install到本地仓库。
添加darkblade-rpc-core和darkblade-core-zookeeper的依赖到你的客户端，例如：
```
<dependency>   
    <groupId>com.darkblade.rpc</groupId>  
    <artifactId>darkblade-rpc-core</artifactId>    
    <version>${com.darkblade.rpc.version}</version>    
    <exclusions>     
        <exclusion>       
            <groupId>org.slf4j</groupId>        
            <artifactId>slf4j-log4j12</artifactId>      
        </exclusion>
    </exclusions>
</dependency>
<dependency>   
    <groupId>com.darkblade.core</groupId> 
    <artifactId>darkblade-core-zookeeper</artifactId> 
    <version>${com.darkblade.rpc.version}</version>
</dependency>
```
添加darkblade-rpc-register和darkblade-register-zookeeper的依赖到你的项目服务端，例如：
```
<dependency>   
    <groupId>com.darkblade.rpc</groupId>  
    <artifactId>darkblade-rpc-register</artifactId>    
    <version>${com.darkblade.rpc.version}</version>    
    <exclusions>     
        <exclusion>       
            <groupId>org.slf4j</groupId>        
            <artifactId>slf4j-log4j12</artifactId>      
        </exclusion>
    </exclusions>
</dependency>
<dependency>   
    <groupId>com.darkblade.register</groupId> 
    <artifactId>darkblade-register-zookeeper</artifactId> 
    <version>${com.darkblade.rpc.version}</version>
</dependency>
```

**服务端**  
在服务端的启动类上添加@EnableServerRegister注解，表示当前节点注册到服务注册与发现中心。
接下来，写一个服务端业务代码，如下所示：
```
@RpcService
public class TestServiceImpl implements TestService {  
    @Override   
    public String userInfo(String username) {     
    return ">>>>>>" + username;   
    }
}
```
@RpcService表示将当前服务暴露出去。同事需要继承一个TestService接口，这个接口应该写在一个公共的模块里，因为客户端项目也需要用到它。至于为什么这么做，通过源码可知，客户端采用了基于JDK动态代理方式调用服务端业务代码块。

**客户端**  
在客户端的启动类上添加@EnableServerDiscovery注解，表示当前节点是一个客户端，并实时获取来自服务注册与发现中心的服务端节点。调用服务端业务代码示例如下：
```
@RpcClient(serviceName = "rpc-server")
public interface TestClientService extends TestService {

}
```
@RpcClient表示这是一个服务调用入口，serviceName表示服务名称，该名称默认是rpc-server，如果服务端变了，这里也需要做出相应变更，建议写在application.yml。注意，这里也继承了一个TestService，由于这个接口是定义在一个公共的模块，因此客户端和服务端都需要引入这同一个模块。

**服务注册与发现中心**  
为了能在后期将zookeeper随时替换成别的，例如nacos、consul之类，因此将其单独抽出。
darkblade-core-zookeeper：实现了服务发现功能
darkblade-register-zookeeper：实现了服务注册功能
它们分别实现了ServerDiscovery和ServerRegister接口，因此后续若开发别的服务中心模块，其核心类也必须实现这两个接口，并在META-INFO.services下添加SPI文件

**结尾**  
一些设计思想模仿了feign。
关于发送请求和zookeeper监听的部分功能抄了https://github.com/luxiaoxun/NettyRpc ，以后如果有了更好的主意，再替换成自己的东西吧
