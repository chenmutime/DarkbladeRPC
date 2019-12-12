package com.darkblade.rpc.core.discovery;

import java.util.List;

public interface ServerDiscovery {

    void startup();

    List<String> serviceNames();

    String health();

    void close();
}
