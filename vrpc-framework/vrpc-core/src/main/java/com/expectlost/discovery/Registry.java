package com.expectlost.discovery;

import com.expectlost.ServiceConfig;

/**
 * 注册中心
 */
public interface Registry {
    /**
     * 注册服务
     * @param serviceConfig 服务的配置内容
     */
     void register(ServiceConfig<?> serviceConfig);
}
