package com.expectlost;

import com.expectlost.utils.NetUtils;
import com.expectlost.utils.zookeeper.ZookeeperNode;
import com.expectlost.utils.zookeeper.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

@Slf4j
public class VrpcBootstrap {

    //VrpcBootstrap是个单例
    private static final VrpcBootstrap vrpcBootstrap = new VrpcBootstrap();

    //定义相关基配置
    private String appName = "default";
    //定义注册中心配置
    private RegistryConfig registryConfig;
    //定义服务协议
    private ProtocalConfig protocalConfig;
    //端口
    private int port =8088;

    //维护一个zookeeper实例
    private ZooKeeper zooKeeper;


    private VrpcBootstrap() {
        //构造引导程序 需要做初始化
    }

    public static VrpcBootstrap getInstance(){
        return vrpcBootstrap;
    }
    /**
     * 用来定义应用的名字
     * @return this
     */
    public VrpcBootstrap application(String appName) {
        this.appName = appName;
        return this;
    }
    /**
     * 用来配置注册中心
     * @return this当前实例
     */
    public VrpcBootstrap registry(RegistryConfig registryConfig) {

        //TODO 这里维护一个zookeeper实例 但是 如果这样写会让zookeeper与整个工程耦合
        zooKeeper = ZookeeperUtils.createZookeeper();
        this.registryConfig = registryConfig;
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     * @param protocalConfig 协议封装
     * @return 当前实例
     */
    public VrpcBootstrap protocal(ProtocalConfig protocalConfig)
    {
        this.protocalConfig = protocalConfig;
        if(log.isDebugEnabled())
        {
            log.debug("当前工程使用了：{}协议进行序列化",protocalConfig.toString());
        }
        return this;
    }

    /**
     * TODO-------------服务提供方api-----------------
     */


    /**
     * 发布服务 将接口实现注册到服务中心
     * @param service 封装的需要发布的服务
     * @return
     */
    public VrpcBootstrap publish(ServiceConfig<?> service) {
        //服务名称节点
        String parentNode = Constant.BASE_PROVIDERS_PATH+"/"+service.getInterface().getName();
        //节点类型
        CreateMode createMode = CreateMode.PERSISTENT;
        ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
        Boolean flag = ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, createMode);

        //创建本机临时节点 ip:port ,
        //服务提供方的端口 一般自定义设定 我们还需要一个 获取ip的方法
        //ip通常需要一个局域网ip 不是127.0.0.1 也不是ipv6
        String node = parentNode+"/"+ NetUtils.getIp()+":"+port;
        if(!ZookeeperUtils.exists(zooKeeper,node,null)){
            ZookeeperNode zookeeperNode1 = new ZookeeperNode(node, null);
            ZookeeperUtils.createNode(zooKeeper,zookeeperNode1,null,CreateMode.EPHEMERAL);
        }
        if(log.isDebugEnabled()) {
            log.debug("服务{}已经注册",service.getInterface().getName());
        }
        return this;
    }

    /**
     * 批量发布
     * @param services
     * @return
     */
    public VrpcBootstrap publish(List<ServiceConfig> services) {


        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public VrpcBootstrap reference(ReferenceConfig<?> reference) {


        //在此方法中我们是否可以拿到相关配置项 注册中心
        //配置 reference 将来调用get方法时方便生成 代理对象
        return this;
    }


    /**
     * TODO-------------服务调用方api-----------------
     */


}
