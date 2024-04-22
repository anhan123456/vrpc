package com.expectlost.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

public interface LoadBalancer {
    /**
     * 根据服务名获取可用服务
     * @param serviceName 服务名称
     * @return
     */
   InetSocketAddress selectServiceAddress(String serviceName);
}