package com.expectlost.discovery.impl;

import com.expectlost.Constant;
import com.expectlost.ServiceConfig;
import com.expectlost.VrpcBootstrap;
import com.expectlost.discovery.AbstractRegistry;
import com.expectlost.exceptions.DiscoveryException;
import com.expectlost.exceptions.NetworkException;
import com.expectlost.utils.NetUtils;
import com.expectlost.utils.zookeeper.ZookeeperNode;
import com.expectlost.utils.zookeeper.ZookeeperUtils;
import com.expectlost.watch.UpAndDownWatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {

    private ZooKeeper zooKeeper;

    public ZookeeperRegistry()
    {
        this.zooKeeper=  ZookeeperUtils.createZookeeper();
    }
    public ZookeeperRegistry(String connectString,int timeout)
    {
    this.zooKeeper = ZookeeperUtils.createZookeeper(connectString,timeout);
    }

    @Override
    public void register(ServiceConfig<?> service) {

        //服务名称节点
        String parentNode = Constant.BASE_PROVIDERS_PATH+"/"+service.getInterface().getName();
        //节点类型
        CreateMode createMode = CreateMode.PERSISTENT;
        ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
        Boolean flag = ZookeeperUtils.createNode(zooKeeper, zookeeperNode,null, createMode);

        //创建本机临时节点 ip:port ,
        //服务提供方的端口 一般自定义设定 我们还需要一个 获取ip的方法
        //ip通常需要一个局域网ip 不是127.0.0.1 也不是ipv6
        //TODO 端口后续处理端口问题
        String node = parentNode+"/"+ NetUtils.getIp()+":"+ VrpcBootstrap.getInstance().PORT;
        if(!ZookeeperUtils.exists(zooKeeper,node,null)){
            ZookeeperNode zookeeperNode1 = new ZookeeperNode(node, null);
            ZookeeperUtils.createNode(zooKeeper,zookeeperNode1,null,CreateMode.EPHEMERAL);
        }
        if(log.isDebugEnabled()) {
            log.debug("服务【{}】已经注册",service.getInterface().getName());
        }
    }

    /**
     *
     * @param serviceName 服务名
     * @return 服务列表
     */
    @Override
    public List<InetSocketAddress> lookup(String serviceName) {
        //1.找到服务对应节点
        String serviceNode = Constant.BASE_PROVIDERS_PATH+"/"+serviceName;
        //2.从zk中获取他的子节点 192.168.xx.xx:1234
        List<String> children = ZookeeperUtils.getChildren(zooKeeper,serviceNode, new UpAndDownWatcher());
        List<InetSocketAddress> socketAddresses = children.stream().map(ipstr -> {
            String[] ip_port = ipstr.split(":");
            String ip = ip_port[0];
            int port = Integer.valueOf(ip_port[1]);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(ip, port);
            return inetSocketAddress;
        }).collect(Collectors.toList());

        if(socketAddresses.size()==0)
        {
            throw  new DiscoveryException("未发现可用节点");
        }

        return socketAddresses;
    }
}
