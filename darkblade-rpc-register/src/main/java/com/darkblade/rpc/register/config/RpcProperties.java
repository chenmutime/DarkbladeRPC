package com.darkblade.rpc.register.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rpc.server")
public class RpcProperties {

    private double limiter = 500;

    private int port = 10001;

    private String host = "localhost";

    private String serviceName = "rpc-server";

    private Zookeeper zookeeper = new Zookeeper();

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public double getLimiter() {
        return limiter;
    }

    public void setLimiter(double limiter) {
        this.limiter = limiter;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Zookeeper getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(Zookeeper zookeeper) {
        this.zookeeper = zookeeper;
    }

    public class Zookeeper {

        private String host = "localhost";

        private int port = 2181;

        private int sessionTimeout = 3000;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getSessionTimeout() {
            return sessionTimeout;
        }

        public void setSessionTimeout(int sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
        }
    }

}
