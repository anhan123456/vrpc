package com.expectlost.loadbalancer.impl;

import com.expectlost.VrpcBootstrap;
import com.expectlost.loadbalancer.AbstractLoadBalancer;
import com.expectlost.loadbalancer.LoadBalancer;
import com.expectlost.loadbalancer.Selector;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
public class MinumumResponseTimeLoadBalancer extends AbstractLoadBalancer implements LoadBalancer {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new MinumumResponseTimeSelector(serviceList);
    }

    private static class MinumumResponseTimeSelector implements Selector {

        public MinumumResponseTimeSelector(List<InetSocketAddress> serviceList) {

        }

        @Override
        public InetSocketAddress getNext() {
            for (Map.Entry<Long, InetSocketAddress> longInetSocketAddressEntry : VrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.entrySet()) {
                System.out.println(longInetSocketAddressEntry.getValue() + ":" + longInetSocketAddressEntry.getKey());
            }
            Map.Entry<Long, InetSocketAddress> entry = VrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.firstEntry();
            if (entry != null) {
                return entry.getValue();
            }
            Channel channel = (Channel)VrpcBootstrap.CHANNEL_CACHE.values().toArray()[0];
            return (InetSocketAddress)channel.remoteAddress();


        }

    }
}
