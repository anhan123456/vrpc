package com.expectlost;

import com.expectlost.netty.MyWatcher;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

public class ZookeeperTest {

    ZooKeeper zookeeper;
    CountDownLatch countDownLatch = new CountDownLatch(1);

    @Before
    public void createZk()
    {
        //定义链接参数
//        String connectString = "127.0.0.1:2181";
        String connectString = "192.168.52.201:2181,192.168.52.202:2181,192.168.52.203:2181";
        //定义超时时间
        int timeout = 10000;

        try {
//            zookeeper = new ZooKeeper(
//                    connectString,
//                    timeout,
//                    new MyWatcher()
//            );
            //只有连接成功才方形
            zookeeper = new ZooKeeper(connectString,timeout,(event)->{
               if(event.getState()== Watcher.Event.KeeperState.SyncConnected)
               {
                   System.out.println("客户端连接成功");
                   countDownLatch.countDown();
               }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testCreatePNode()
    {
        try {
            //先等待创建完成
            countDownLatch.await();
           String result = zookeeper.create("/ydlclass",
                    "hello".getBytes(StandardCharsets.UTF_8),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
            System.out.println("result = " + result);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            try {
                if (zookeeper!=null)
                {
                    try {
                        zookeeper.close();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }catch (Exception e){}
        }
    }

    @Test
    public void testDeletePNode()
    {
        try {
            Stat exists = zookeeper.exists("/ydlclass", null);
            zookeeper.setData("/ydlclass","hi".getBytes(StandardCharsets.UTF_8),-1);
          //当前节点数据版本
            int version = exists.getVersion();
            //当前节点acl数据版本
            int aversion = exists.getAversion();
            //节点数据版本
            int cversion = exists.getCversion();
            System.out.println("version = " + version);
            System.out.println("aversion = " + aversion);
            System.out.println("cversion = " + cversion);

//            zookeeper.delete("/ydlclass",-1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }finally {
            try {
                zookeeper.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testWatcher()
    {
        try {
            Stat exists = zookeeper.exists("/ydlclass", true);
            while (true)
            {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }finally {
            try {
                zookeeper.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
