package com.expectlost.utils.zookeeper;

import com.expectlost.Constant;
import com.expectlost.exceptions.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;


@Slf4j
public class ZookeeperUtils {

    /**
     * 使用默认配置创建zookeeper实例
     *
     * @return Zookeeper实例
     */
    public static ZooKeeper createZookeeper() {
        String connectString = Constant.DEFAULT_ZK_CONNECT;
        int timeout = Constant.TIME_OUT;
        return createZookeeper(connectString, timeout);
    }

    public static ZooKeeper createZookeeper(String connectString, int timeout) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            final ZooKeeper zookeeper = new ZooKeeper(connectString, timeout, (event) -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("客户端连接成功");
                    countDownLatch.countDown();
                }
            });
            //先等待创建完成
            countDownLatch.await();
            return zookeeper;
        } catch (IOException | InterruptedException e) {
            log.error("创建zookeeper实例时发生异常", e);
            throw new ZookeeperException();
        }
    }

    /**
     * 创建一个节点的方法
     *
     * @param zooKeeper  zookeeper实例
     * @param node       zookeeper节点
     * @param watcher    watcher
     * @param createMode 节点模式
     * @return 节点创建结果
     */
    public static Boolean createNode(ZooKeeper zooKeeper,
                                     ZookeeperNode node,
                                     Watcher watcher,
                                     CreateMode createMode) {
        try {
            if (zooKeeper.exists(node.getNodePath(), null) == null) {
                String result = zooKeeper.create(node.getNodePath(),
                        node.getData(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        createMode);
                log.info("节点【{}】 成功创建。", result);
                return true;
            } else {
                if (log.isDebugEnabled()) {
                    log.info("节点【{}】 已经存在无需创建", node.getNodePath());
                }
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
            throw new ZookeeperException();
        }

        return false;
    }

    /**
     * 关闭zookeeper的方法
     * @param zooKeeper
     */
    public static void close(ZooKeeper zooKeeper)
    {
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            log.error("关闭zookeeper时发生了异常",e);
            e.printStackTrace();
        }
    }

    /**
     *  存在true 不存在false
     * @param zooKeeper
     * @param node
     * @param watcher
     * @return
     */
    public static Boolean exists(ZooKeeper zooKeeper,String node,Watcher watcher)
    {
        try {
            return zooKeeper.exists(node,watcher)!=null;
        } catch (KeeperException | InterruptedException e) {
            log.error("判断节点【{}】是否存在出现异常",node,e);
            e.printStackTrace();
            throw new ZookeeperException();
        }
    }
}
