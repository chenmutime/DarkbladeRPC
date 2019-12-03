package com.darkblade.rpc.core.discovery;

import com.darkblade.rpc.core.config.ServerProperties;

import java.util.List;

public interface ServerDiscovery {

    void startup(ServerProperties serverProperties);

    List<String> serviceNames();

    String health();

    void close();
}
