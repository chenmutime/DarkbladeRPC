package com.darkblade.rpc.common.util;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SpiSupportUtil<T> {

    public List<T> loadSPI(Class clz) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(clz);
        return StreamSupport.stream(serviceLoader.spliterator(), false).collect(Collectors.toList());
    }
}
