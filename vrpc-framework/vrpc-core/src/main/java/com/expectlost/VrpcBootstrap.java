package com.expectlost;

import com.expectlost.discovery.Registry;
import com.expectlost.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

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

    //TODO 待处理
    private Registry registry;


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
        //使用 registryConfig获取一个注册中心
        this.registry = registryConfig.getRegistry();
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
        //抽象注册中心概念 使用注册中心的一个实现完成注册
        registry.register(service);

        return this;
    }

    /**
     * 批量发布
     * @param services
     * @return
     */
    public VrpcBootstrap publish(List<ServiceConfig> services) {
        for (ServiceConfig service : services) {
            this.publish(service);
        }
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
