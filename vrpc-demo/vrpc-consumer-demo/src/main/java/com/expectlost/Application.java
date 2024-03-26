package com.expectlost;

public class Application {
    public static void main(String[] args) {
        //获取代理对象 使用ReferenceConfig进行封装
        //reference 中一定有生成代理的模版方法get()
        ReferenceConfig<HelloVrpc> reference = new ReferenceConfig<>();
        reference.setInterface(HelloVrpc.class);


        VrpcBootstrap.getInstance()
                .application("first-vrpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .reference(reference);

        HelloVrpc helloVrpc = reference.get();
        helloVrpc.sayHi("你好");
    }
}
