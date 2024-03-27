package com.expectlost.discovery.impl;

import com.expectlost.Constant;
import com.expectlost.ServiceConfig;
import com.expectlost.discovery.AbstractRegistry;
import com.expectlost.utils.NetUtils;
import com.expectlost.utils.zookeeper.ZookeeperNode;
import com.expectlost.utils.zookeeper.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

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
        Boolean flag = ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, createMode);

        //创建本机临时节点 ip:port ,
        //服务提供方的端口 一般自定义设定 我们还需要一个 获取ip的方法
        //ip通常需要一个局域网ip 不是127.0.0.1 也不是ipv6
        //TODO 端口后续处理端口问题
        String node = parentNode+"/"+ NetUtils.getIp()+":"+8088;
        if(!ZookeeperUtils.exists(zooKeeper,node,null)){
            ZookeeperNode zookeeperNode1 = new ZookeeperNode(node, null);
            ZookeeperUtils.createNode(zooKeeper,zookeeperNode1,null,CreateMode.EPHEMERAL);
        }
        if(log.isDebugEnabled()) {
            log.debug("服务{}已经注册",service.getInterface().getName());
        }
    }
}
