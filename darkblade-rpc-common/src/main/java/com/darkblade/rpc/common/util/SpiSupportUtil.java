package com.darkblade.rpc.common.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

public class SpiSupportUtil<T> {

    public Map<String, T> loadSPI(Class clz) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(clz);
        Map<String, T> spiMap = new HashMap<>();
        Iterator<T> iterable = serviceLoader.iterator();
        while (iterable.hasNext()) {
            T obj = iterable.next();
            Class<?> objClass = obj.getClass();
            spiMap.put(objClass.getSimpleName(), obj);
        }
        return spiMap;
    }
}
