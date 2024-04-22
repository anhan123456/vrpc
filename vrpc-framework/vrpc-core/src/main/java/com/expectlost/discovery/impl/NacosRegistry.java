package com.expectlost.discovery.impl;

import com.expectlost.ServiceConfig;
import com.expectlost.discovery.AbstractRegistry;

import java.net.InetSocketAddress;
import java.util.List;

public class NacosRegistry extends AbstractRegistry {
    @Override
    public void register(ServiceConfig<?> serviceConfig) {

    }

    @Override
    public List<InetSocketAddress> lookup(String serviceName) {
        return null;
    }
}
