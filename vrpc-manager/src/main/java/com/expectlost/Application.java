package com.expectlost;

import com.expectlost.utils.zookeeper.ZookeeperNode;
import com.expectlost.utils.zookeeper.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.util.List;

/**
 * zookeeper注册中心管理页
 */
@Slf4j
public class Application {
    public static void main(String[] args) {
        //创建基础目录
        String connectString = Constant.DEFAULT_ZK_CONNECT;
        int timeout = Constant.TIME_OUT;
            ZooKeeper zookeeper = ZookeeperUtils.createZookeeper();
            String basePath = "/vrpc-metadata";
            ZookeeperNode baseNode = new ZookeeperNode(basePath,null);
            ZookeeperNode providerNode = new ZookeeperNode(basePath+"/providers",null);
            ZookeeperNode consumerNode = new ZookeeperNode(basePath+"/consumers",null);
            //创建节点
            List.of(baseNode,providerNode,consumerNode).forEach(node->{
                ZookeeperUtils.createNode(zookeeper,node,null,CreateMode.PERSISTENT);
            });
            ZookeeperUtils.close(zookeeper);
    }
}
