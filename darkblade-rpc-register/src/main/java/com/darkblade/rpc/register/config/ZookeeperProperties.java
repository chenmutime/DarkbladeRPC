package com.darkblade.rpc.register.config;

import lombok.Data;

@Data
public class ZookeeperProperties {

    private String host = "localhost";

    private int port = 2181;

    private int sessionTimeout = 3000;

}
