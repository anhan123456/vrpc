package com.expectlost.loadbalancer.impl;

import com.expectlost.exceptions.LoadBalancerException;
import com.expectlost.loadbalancer.AbstractLoadBalancer;
import com.expectlost.loadbalancer.LoadBalancer;
import com.expectlost.loadbalancer.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RandomBalancer extends AbstractLoadBalancer implements LoadBalancer {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new RandomSelector(serviceList);
    }

    private static class RandomSelector implements Selector {
        private List<InetSocketAddress> serviceList;

        public RandomSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
        }

        @Override
        public InetSocketAddress getNext() {
            Random random = new Random();
            int i = random.nextInt(this.serviceList.size());
            return this.serviceList.get(i);
        }

        @Override
        public void rebalance() {

        }
    }
}
