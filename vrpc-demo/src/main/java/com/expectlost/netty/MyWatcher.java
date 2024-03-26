package com.expectlost.netty;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class MyWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {
        /**
         * 自定义Watcher
         */
        //判断 事件类型
        if(event.getType() == Event.EventType.None){
            if(event.getState() == Event.KeeperState.SyncConnected){
                System.out.println("zookeeper连接成功");
            } else if (event.getState() == Event.KeeperState.AuthFailed){
                System.out.println("zookeeper认证失败");
            } else if (event.getState() == Event.KeeperState.Disconnected){
                System.out.println("zookeeper断开连接");
            }

        } else if (event.getType() == Event.EventType.NodeCreated){
            System.out.println(event.getPath() + "被创建了");
        } else if (event.getType() == Event.EventType.NodeDeleted){
            System.out.println(event.getPath() + "被删除了了");
        } else if (event.getType() == Event.EventType.NodeDataChanged){
            System.out.println(event.getPath() + "节点的数据改变了");
        } else if (event.getType() == Event.EventType.NodeChildrenChanged){
            System.out.println(event.getPath() + "子节点发生了变化");
        }

    }
}
