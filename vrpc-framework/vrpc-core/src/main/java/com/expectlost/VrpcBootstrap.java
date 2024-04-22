package com.expectlost;

import com.expectlost.channelHandler.handler.MethodCallHandler;
import com.expectlost.channelHandler.handler.VrpcRequestDecoder;
import com.expectlost.channelHandler.handler.VrpcResponseEncoder;
import com.expectlost.discovery.Registry;
import com.expectlost.discovery.RegistryConfig;
import com.expectlost.loadbalancer.LoadBalancer;
import com.expectlost.loadbalancer.impl.RoundRobinLoadBalancer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class VrpcBootstrap {

    public static final int  PORT = 8091;
    //VrpcBootstrap是个单例
    private static final VrpcBootstrap vrpcBootstrap = new VrpcBootstrap();

    //定义相关基配置
    private String appName = "default";
    //定义注册中心配置
    private RegistryConfig registryConfig;
    //定义服务协议
    private ProtocalConfig protocalConfig;
    //端口
    public static final IdGenerator ID_GENERATOR = new IdGenerator(1,2);
    public static String SERIALIZE_TYPE = "jdk";
    public static String COMPRESS_TYPE = "gzip";

    private Registry registry;
    public  static LoadBalancer LOAD_BALANCER = new RoundRobinLoadBalancer();
    //连接缓存
    public final static Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>();

    //维护已经发布并且暴露的服务列表 key >interface全限定名称 value -> serviceConfig
    public static final Map<String, ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>(16);

    //定义全局对外挂起的complateableFuture

    public final static Map<Long, CompletableFuture<Object>>PENDING_REQUEST = new ConcurrentHashMap<>(128);
    private VrpcBootstrap() {
        //构造引导程序 需要做初始化
    }

    public static VrpcBootstrap getInstance() {
        return vrpcBootstrap;
    }

    /**
     * 用来定义应用的名字
     *
     * @return this
     */
    public VrpcBootstrap application(String appName) {
        this.appName = appName;
        return this;
    }

    /**
     * 用来配置注册中心
     *
     * @return this当前实例
     */
    public VrpcBootstrap registry(RegistryConfig registryConfig) {
        //使用 registryConfig获取一个注册中心
        this.registry = registryConfig.getRegistry();

        VrpcBootstrap.LOAD_BALANCER = new RoundRobinLoadBalancer();
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     *
     * @param protocalConfig 协议封装
     * @return 当前实例
     */
    public VrpcBootstrap protocal(ProtocalConfig protocalConfig) {
        this.protocalConfig = protocalConfig;
        if (log.isDebugEnabled()) {
            log.debug("当前工程使用了：{}协议进行序列化", protocalConfig.toString());
        }
        return this;
    }

    /**
     * TODO-------------服务提供方api-----------------
     */


    /**
     * 发布服务 将接口实现注册到服务中心
     *
     * @param service 封装的需要发布的服务
     * @return
     */
    public VrpcBootstrap publish(ServiceConfig<?> service) {
        //抽象注册中心概念 使用注册中心的一个实现完成注册
        registry.register(service);
        SERVICE_LIST.put(service.getInterface().getName(), service);

        return this;
    }

    /**
     * 批量发布
     *
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

        //TODO 1 创建eventLoop 老板只负责处理请求 之后会将请求分发至worker
        //TODO 工人是老板的5倍
        EventLoopGroup boss = new NioEventLoopGroup(2);
        EventLoopGroup worker = new NioEventLoopGroup(5);

        try {
            //TODO 创建服务器引导器
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap = serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //TODO 使用处理器
                            socketChannel.pipeline()
                                    .addLast(new LoggingHandler())
                                    .addLast(new VrpcRequestDecoder())
                                    .addLast(new MethodCallHandler())
                                    .addLast(new VrpcResponseEncoder());
                        }
                    });

            //TODO 绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(PORT).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {

        } finally {
            /**
             * 优雅关闭
             */
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public VrpcBootstrap reference(ReferenceConfig<?> reference) {


        //在此方法中我们是否可以拿到相关配置项 注册中心
        //配置 reference 将来调用get方法时方便生成 代理对象
        reference.setRegistry(registry);
        return this;
    }

    public VrpcBootstrap serialize(String serializeType) {
        if(log.isDebugEnabled())
        {
            log.debug("配置序列化方式为【{}】",serializeType);
        }
        SERIALIZE_TYPE = serializeType;
        return this;
    }
    public VrpcBootstrap compress(String compressType) {
        if(log.isDebugEnabled())
        {
            log.debug("配置压缩方式为【{}】",compressType);
        }
        COMPRESS_TYPE = compressType;
        return this;
    }

    public Registry getRegistry() {
        return registry;
    }

    /**
     * TODO-------------服务调用方api-----------------
     */


}
