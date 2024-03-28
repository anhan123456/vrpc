package com.expectlost.discovery.impl;

import com.expectlost.ServiceConfig;
import com.expectlost.discovery.AbstractRegistry;

import java.net.InetSocketAddress;

public class NacosRegistry extends AbstractRegistry {
    @Override
    public void register(ServiceConfig<?> serviceConfig) {

    }

    @Override
    public InetSocketAddress lookup(String serviceName) {
        return null;
    }
}
