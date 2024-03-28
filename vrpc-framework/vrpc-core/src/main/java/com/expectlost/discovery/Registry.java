package com.expectlost.discovery;

import com.expectlost.ServiceConfig;

import java.net.InetSocketAddress;

/**
 * 注册中心
 */
public interface Registry {
    /**
     * 注册服务
     * @param serviceConfig 服务的配置内容
     */
     void register(ServiceConfig<?> serviceConfig);

    /**
     * 从注册中心拉取一个可用的服务
     * @param serviceName 服务名
     * @return 服务地址
     */
     InetSocketAddress lookup(String serviceName);
}
