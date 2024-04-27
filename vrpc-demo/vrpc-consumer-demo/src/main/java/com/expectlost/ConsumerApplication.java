package com.expectlost;

import com.expectlost.discovery.RegistryConfig;
import com.expectlost.serialize.impl.JdkSerializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) {
        //获取代理对象 使用ReferenceConfig进行封装
        //reference 中一定有生成代理的模版方法get()
        ReferenceConfig<HelloVrpc> reference = new ReferenceConfig<>();
        reference.setInterface(HelloVrpc.class);

        VrpcBootstrap.getInstance()
                .application("first-" +
                        "rpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("jdk")
                .compress("gzip")
                .reference(reference);

        HelloVrpc helloVrpc = reference.get();
        for (int i = 0; i < 10; i++) {
            String sayHi = helloVrpc.sayHi("1111111");
            String sayHi2 = helloVrpc.sayHi("2222222");
            log.info("sayHi-->{}",sayHi);
        }

    }
}
