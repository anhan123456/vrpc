package com.expectlost.loadbalancer.impl;

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
public class LRUBalancer extends AbstractLoadBalancer implements LoadBalancer {




    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new LRUSelector(serviceList);
    }

    private static class LRUSelector implements Selector {
        private ConcurrentHashMap<InetSocketAddress, AtomicInteger> SERVICE_USE_COUNT = null;
        private List<InetSocketAddress> serviceList;
        private AtomicInteger index;


        public LRUSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.SERVICE_USE_COUNT = new ConcurrentHashMap<>();
            this.index = new AtomicInteger(0);
        }

        @Override
        public synchronized InetSocketAddress getNext() {
            if(serviceList ==null ||serviceList.size() ==0)
            {
                throw new LoadBalancerException("可用节点为空");
            }
            InetSocketAddress result_service = null;
            //当使用次数的map数组中大小和可用节点中大小不一样的情况就要获取不存在与map中的节点
            if(this.SERVICE_USE_COUNT.size()!=serviceList.size())
            {
                for (InetSocketAddress service : serviceList) {
                    if(!this.SERVICE_USE_COUNT.containsKey(service))
                    {
                        //不存在于map中
                        this.SERVICE_USE_COUNT.put(service,new AtomicInteger(1));
                        return service;
                    }
                }
            }else
            {
                //当全部service 均被使用过 就获取map中使用最少的service
                Map.Entry<InetSocketAddress, AtomicInteger> entry = getSocketWithMinUsage(this.SERVICE_USE_COUNT);
                entry.getValue().incrementAndGet();
                InetSocketAddress key = entry.getKey();
                this.SERVICE_USE_COUNT.put(key,entry.getValue());
                return key;
            }
            throw new LoadBalancerException("可用节点为空");
        }

        public static Map.Entry<InetSocketAddress, AtomicInteger> getSocketWithMinUsage(ConcurrentHashMap<InetSocketAddress, AtomicInteger> socketUsageMap) {
            InetSocketAddress minUsageSocket = null;
            int minUsage = Integer.MAX_VALUE;

            Map.Entry<InetSocketAddress, AtomicInteger> result = null;
            for (Map.Entry<InetSocketAddress, AtomicInteger> entry : socketUsageMap.entrySet()) {
                InetSocketAddress socket = entry.getKey();
                AtomicInteger usage = entry.getValue();

                if (usage.get() < minUsage) {
                    minUsage = usage.get();
                    minUsageSocket = socket;
                    result = entry;
                }
            }
            return result;
        }


    }
}
