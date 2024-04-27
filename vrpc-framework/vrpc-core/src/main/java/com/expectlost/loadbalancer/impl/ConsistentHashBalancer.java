package com.expectlost.loadbalancer.impl;

import com.expectlost.VrpcBootstrap;
import com.expectlost.exceptions.LoadBalancerException;
import com.expectlost.loadbalancer.AbstractLoadBalancer;
import com.expectlost.loadbalancer.LoadBalancer;
import com.expectlost.loadbalancer.Selector;
import com.expectlost.transport.message.VrpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ConsistentHashBalancer extends AbstractLoadBalancer implements LoadBalancer {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new ConsistentHashSelector(serviceList,128);
    }

    /**
     * 一致性hash具体算法实现
     */
    private static class ConsistentHashSelector implements Selector {
        //hash环用来存储服务器节点
        private SortedMap<Integer, InetSocketAddress> circle = new TreeMap<>();
        //引入虚拟节点个数

        private int virtualNodes;

        public ConsistentHashSelector(List<InetSocketAddress> serviceList,int virtualNodes) {
            this.virtualNodes = virtualNodes;
            //节点转化
            for (InetSocketAddress inetSocketAddress : serviceList) {
                //将节点添加到hash环中
                addNodeToCircle(inetSocketAddress);
            }
        }

        /**
         * 将每个节点挂载到hash环上 
         * @param inetSocketAddress
         */
        private void addNodeToCircle(InetSocketAddress inetSocketAddress) {
            //为每一个节点生成匹配的虚拟节点进行挂载
            for (int i = 0; i < virtualNodes; i++) {
                int hash  = hash(inetSocketAddress.toString()+"#"+i);
                //挂载到hash环中
                circle.put(hash,inetSocketAddress);
                if(log.isDebugEnabled())
                {
                    log.debug("add node to circle : {}",hash);
                }
            }
        }
        private void removeNodeFromCircle(InetSocketAddress inetSocketAddress) {
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString()+"#"+i);
                circle.remove(hash);
            }
        }

        /**
         * 获取可用节点
         * @return
         */
        @Override
        public InetSocketAddress getNext() {
            //拿到请求
            VrpcRequest vrpcRequest = VrpcBootstrap.REQUEST_THREAD_LOCAL.get();
            //对请求的id做hash字符串默认的hash
            int hash  = hash(vrpcRequest.getRequestPayload().toString());

            //判断hash 能否落在某个服务器上 和服务器hash相同

            if(!circle.containsKey(hash))
            {
                //寻找离我最近的节点
                SortedMap<Integer, InetSocketAddress> tailMap = circle.tailMap(hash);

                //如果hash 比所有的节点都大 则选择第一个节点
                hash = tailMap.isEmpty()?circle.firstKey():tailMap.firstKey();

            }

            return circle.get(hash);
        }


        /**
         * 具体的hash 算法
         * @param s
         * @return
         */
        private int hash(String s) {
            try {
                // Get an instance of MD5 hash function
                MessageDigest md = MessageDigest.getInstance("MD5");
                // Compute the hash value of the input string
                byte[] bytes = md.digest(s.getBytes());
                // Convert the hash value to an integer
                int hash = ((int) bytes[0] & 0xFF) |
                        (((int) bytes[1] & 0xFF) << 8) |
                        (((int) bytes[2] & 0xFF) << 16) |
                        (((int) bytes[3] & 0xFF) << 24);
                return hash;
            } catch (NoSuchAlgorithmException e) {
                // Handle the case where MD5 algorithm is not available
                // You can log an error message or throw an exception based on your requirement
                e.printStackTrace();
                return 0;
            }
        }
        private String toBinary(int i )
        {
            String binaryString = Integer.toBinaryString(i);
            int index = 32 - binaryString.length();
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j <index ; j++) {
                stringBuilder.append(0);
            }
            stringBuilder.append(binaryString);
            return stringBuilder.toString();
        }

        @Override
        public void rebalance() {

        }
    }
}
