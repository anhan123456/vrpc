package com.expectlost;

import com.expectlost.discovery.Registry;
import com.expectlost.discovery.RegistryConfig;
import com.expectlost.exceptions.NetworkException;
import com.expectlost.proxy.handler.RpcConsumerInvocationHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ReferenceConfig<T> {

    private Class<T> interfaceRef;

    private Registry registry;


    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }


    /**
     * 代理设计模式 生成一个 api接口的代理对象，代理对象调用实际走invoke函数
     *
     * @return 代理对象
     */
    public T get() {
        //此处使用动态代理完成一些工作
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<T>[] classes = new Class[]{interfaceRef};
        //使用动态代理生成代理对象
        Object hello_proxy = Proxy.newProxyInstance(classLoader, classes, new RpcConsumerInvocationHandler(registry, interfaceRef));
        return (T) hello_proxy;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }
}
