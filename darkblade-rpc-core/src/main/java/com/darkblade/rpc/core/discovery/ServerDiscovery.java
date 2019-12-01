package com.darkblade.rpc.core.discovery;

import com.darkblade.rpc.core.config.ServerProperties;

public interface ServerDiscovery {

    void loadAllServices(ServerProperties serverProperties);

}
