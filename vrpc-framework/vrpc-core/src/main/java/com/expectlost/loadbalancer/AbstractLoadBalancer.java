package com.expectlost.loadbalancer;

import com.expectlost.VrpcBootstrap;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractLoadBalancer implements LoadBalancer {
    private Selector selector;
    private Map<String, Selector> cache = new ConcurrentHashMap<>(8);

    @Override
    public synchronized void reLoadBalance(String serviceName, List<InetSocketAddress> addresses) {
        cache.put(serviceName,getSelector(addresses));
    }

    @Override
    public InetSocketAddress selectServiceAddress(String serviceName) {
        Selector selector = cache.get(serviceName);
        if (selector == null) {
            List<InetSocketAddress> serviceList = VrpcBootstrap.getInstance().getRegistry().lookup(serviceName);
            selector = getSelector(serviceList);
            cache.put(serviceName, selector);
        }
        return selector.getNext();
    }
    /**
     * 子类实现扩展
     * @param serviceList
     * @return 负载均衡选择器
     */
    protected abstract Selector getSelector(List<InetSocketAddress> serviceList);


}
