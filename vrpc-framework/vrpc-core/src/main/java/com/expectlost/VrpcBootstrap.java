package com.expectlost;

import lombok.extern.slf4j.Slf4j;

import java.lang.module.ResolvedModule;
import java.util.List;

@Slf4j
public class VrpcBootstrap {

    //VrpcBootstrap是个单例
    private static VrpcBootstrap vrpcBootstrap = new VrpcBootstrap();

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
        return this;
    }
    /**
     * 用来配置注册中心
     * @return this当前实例
     */
    public VrpcBootstrap registry(RegistryConfig registryConfig) {
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     * @param protocalConfig 协议封装
     * @return 当前实例
     */
    public VrpcBootstrap protocal(ProtocalConfig protocalConfig)
    {
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

        if(log.isDebugEnabled())
        {
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
