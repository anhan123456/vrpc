package com.expectlost.watch;

import com.expectlost.NettyBootstrapInitializer;
import com.expectlost.VrpcBootstrap;
import com.expectlost.discovery.Registry;
import com.expectlost.loadbalancer.LoadBalancer;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * 动态感知 watcher
 */
@Slf4j
public class UpAndDownWatcher  implements Watcher {

    @Override
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged)
        {
           if (log.isDebugEnabled())
           {
               log.debug("检测到服务【{}】下节点列表发生变化",watchedEvent.getPath());
           }
           String serviceName = getServiceName(watchedEvent.getPath());
            Registry registry = VrpcBootstrap.getInstance().getRegistry();
            List<InetSocketAddress> addresses = registry.lookup(serviceName);
            for (InetSocketAddress address : addresses) {
                if (!VrpcBootstrap.CHANNEL_CACHE.containsKey(address))
                {
                    Channel channel = null;
                    try {
                        channel = NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    VrpcBootstrap.CHANNEL_CACHE.put(address,channel);

                }
            }
            //处理下线
            for (Map.Entry<InetSocketAddress, Channel> entry : VrpcBootstrap.CHANNEL_CACHE.entrySet()) {
                if(!addresses.contains(entry.getKey()))
                {
                    VrpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                }
            }

            LoadBalancer loadBalancer = VrpcBootstrap.LOAD_BALANCER;
            loadBalancer.reLoadBalance(serviceName,addresses);

        }
    }

    private String getServiceName(String path) {

        String [] split = path.split("/");
        return split[split.length-1];
    }
}
