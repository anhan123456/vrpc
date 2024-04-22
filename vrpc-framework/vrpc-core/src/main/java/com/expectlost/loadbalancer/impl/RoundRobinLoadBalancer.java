package com.expectlost.loadbalancer.impl;

import com.expectlost.VrpcBootstrap;
import com.expectlost.discovery.Registry;
import com.expectlost.exceptions.LoadBalancerException;
import com.expectlost.loadbalancer.AbstractLoadBalancer;
import com.expectlost.loadbalancer.LoadBalancer;
import com.expectlost.loadbalancer.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RoundRobinLoadBalancer extends AbstractLoadBalancer implements LoadBalancer {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new RoundRobinSelector(serviceList);
    }

    private static class RoundRobinSelector implements Selector {
        private List<InetSocketAddress> serviceList;
        private AtomicInteger index;

        public RoundRobinSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress getNext() {
            if (serviceList == null || serviceList.size() == 0) {
                log.error("进行负载均衡选取节点时 服务列表为空。");
                throw new LoadBalancerException();
            }
            if (index.get() == serviceList.size() - 1) {
                index.set(0);
                return serviceList.get(index.get());
            }
            index.incrementAndGet();
            InetSocketAddress address = serviceList.get(index.get());
            //游标后移一位
            return address;
        }

        @Override
        public void rebalance() {

        }
    }
}
