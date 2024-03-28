package com.expectlost;

import com.expectlost.discovery.RegistryConfig;
import com.expectlost.impl.HelloVrpcImpl;

public class ProviderApplication {
    public static void main(String[] args) {
        // 服务提供方，需要注册服务，启动服务
        // 1、封装要发布的服务
        ServiceConfig<HelloVrpc> service = new ServiceConfig<>();
        service.setInterface(HelloVrpc.class);
        service.setRef(new HelloVrpcImpl());
        // 2、定义注册中心

        // 3、通过启动引导程序，启动服务提供方
        //   （1） 配置 -- 应用的名称 -- 注册中心 -- 序列化协议 -- 压缩方式
        //   （2） 发布服务
       VrpcBootstrap.getInstance()
               .application("first-vrpc-provider")
               //TODO 配置注册中心
               .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
               .protocal(new ProtocalConfig("jdk"))
               //TODO 发布服务
               .publish(service)
               //TODO 启动服务
               .start();

    }
}
