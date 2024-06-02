package com.expectlost;

import com.expectlost.annotation.VrpcApi;
import com.expectlost.channelHandler.handler.MethodCallHandler;
import com.expectlost.channelHandler.handler.VrpcRequestDecoder;
import com.expectlost.channelHandler.handler.VrpcResponseEncoder;
import com.expectlost.config.Configuration;
import com.expectlost.core.HeartBeatDetector;
import com.expectlost.discovery.Registry;
import com.expectlost.discovery.RegistryConfig;
import com.expectlost.loadbalancer.LoadBalancer;
import com.expectlost.loadbalancer.impl.*;
import com.expectlost.transport.message.VrpcRequest;
import com.sun.source.tree.Tree;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.sql.Array;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class VrpcBootstrap {

    //VrpcBootstrap是个单例
    private static final VrpcBootstrap vrpcBootstrap = new VrpcBootstrap();


    //全局配置中心
    private Configuration configuration;

    public static final ThreadLocal<VrpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    public static LoadBalancer LOAD_BALANCER = new RoundRobinLoadBalancer();
    //连接缓存
    public final static Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>();
    public final static TreeMap<Long, InetSocketAddress> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>();

    //维护已经发布并且暴露的服务列表 key >interface全限定名称 value -> serviceConfig
    public static final Map<String, ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>(16);

    //定义全局对外挂起的complateableFuture

    public final static Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);

    private VrpcBootstrap() {
        //构造引导程序 需要做初始化
        this.configuration = new Configuration();
        System.out.println(this.configuration);

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
         configuration.setAppName(appName);
        return this;
    }
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 用来配置注册中心
     *
     * @return this当前实例
     */
    public VrpcBootstrap registry(RegistryConfig registryConfig) {
        //使用 registryConfig获取一个注册中心
       configuration.setRegistryConfig(registryConfig);

        VrpcBootstrap.LOAD_BALANCER = new RoundRobinLoadBalancer();
        return this;
    }

    /**
     * 配置负载均衡策略
     * @param loadBalancer
     * @return
     */
    public VrpcBootstrap loadBalancer(LoadBalancer loadBalancer) {

        configuration.setLoadBalancer(loadBalancer);
        return this;
    }


    /**
     * 配置当前暴露的服务使用的协议
     *
     * @param protocalConfig 协议封装
     * @return 当前实例
     */
    public VrpcBootstrap protocal(ProtocalConfig protocalConfig) {
        configuration.setProtocalConfig(protocalConfig);
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
        configuration.getRegistryConfig().getRegistry().register(service);
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
            ChannelFuture channelFuture = serverBootstrap.bind(configuration.getPort()).sync();
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

        //开启存活探知
        HeartBeatDetector.detect(reference.getInterface().getName());

        //在此方法中我们是否可以拿到相关配置项 注册中心
        //配置 reference 将来调用get方法时方便生成 代理对象
        reference.setRegistry(configuration.getRegistryConfig().getRegistry());
        return this;
    }

    public VrpcBootstrap serialize(String serializeType) {
        if (log.isDebugEnabled()) {
            log.debug("配置序列化方式为【{}】", serializeType);
        }
        configuration.setSerializeType(serializeType);
        return this;
    }

    public VrpcBootstrap compress(String compressType) {
        if (log.isDebugEnabled()) {
            log.debug("配置压缩方式为【{}】", compressType);
        }
        configuration.setCompressType(compressType);
        return this;
    }


    public VrpcBootstrap scan(String packageName) {
        // 1、需要通过packageName获取其下的所有的类的权限定名称
        List<String> classNames = getAllClassNames(packageName);
        // 2、通过反射获取他的接口，构建具体实现
        List<Class<?>> classes = classNames.stream()
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).filter(clazz -> clazz.getAnnotation(VrpcApi.class) != null)
                .collect(Collectors.toList());

        for (Class<?> clazz : classes) {
            // 获取他的接口
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance = null;
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }


            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
                if (log.isDebugEnabled()){
                    log.debug("---->已经通过包扫描，将服务【{}】发布.",anInterface);
                }
                // 3、发布
                publish(serviceConfig);
            }

        }
        return this;
    }

    private List<String> getAllClassNames(String packageName) {
        // 1、通过packageName获得绝对路径
        // com.ydlclass.xxx.yyy -> E://xxx/xww/sss/com/ydlclass/xxx/yyy
        String basePath = packageName.replaceAll("\\.","/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if(url == null){
            throw new RuntimeException("包扫描时，发现路径不存在.");
        }
        String absolutePath = url.getPath();
        //
        List<String> classNames = new ArrayList<>();
        classNames = recursionFile(absolutePath,classNames,basePath);

        return classNames;
    }

    private List<String> recursionFile(String absolutePath, List<String> classNames,String basePath) {
        // 获取文件
        File file = new File(absolutePath);
        // 判断文件是否是文件夹
        if (file.isDirectory()){
            // 找到文件夹的所有的文件
            File[] children = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if(children == null || children.length == 0){
                return classNames;
            }
            for (File child : children) {
                if(child.isDirectory()){
                    // 递归调用
                    recursionFile(child.getAbsolutePath(),classNames,basePath);
                } else {
                    // 文件 --> 类的权限定名称
                    String className = getClassNameByAbsolutePath(child.getAbsolutePath(),basePath);
                    classNames.add(className);
                }
            }

        } else {
            // 文件 --> 类的权限定名称
            String className = getClassNameByAbsolutePath(absolutePath,basePath);
            classNames.add(className);
        }
        return classNames;
    }

    private String getClassNameByAbsolutePath(String absolutePath,String basePath) {
        // E:\project\ydlclass-yrpc\yrpc-framework\yrpc-core\target\classes\com\ydlclass\serialize\Serializer.class
        // com\ydlclass\serialize\Serializer.class --> com.ydlclass.serialize.Serializer
        String fileName = absolutePath
                .substring(absolutePath.indexOf(basePath.replaceAll("/","\\\\")))
                .replaceAll("\\\\",".");

        fileName = fileName.substring(0,fileName.indexOf(".class"));
        return fileName;
    }

    public static void main(String[] args) {
        List<String> strings = VrpcBootstrap.getInstance().getAllClassNames("com.expectlost");
        System.out.println(strings);
    }

    /**
     * TODO-------------服务调用方api-----------------
     */


}
