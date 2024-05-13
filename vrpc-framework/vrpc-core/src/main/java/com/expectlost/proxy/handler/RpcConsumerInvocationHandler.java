package com.expectlost.proxy.handler;

import com.expectlost.IdGenerator;
import com.expectlost.NettyBootstrapInitializer;
import com.expectlost.VrpcBootstrap;
import com.expectlost.compress.CompressorFactory;
import com.expectlost.discovery.Registry;
import com.expectlost.enumeration.RequestType;
import com.expectlost.exceptions.DiscoveryException;
import com.expectlost.exceptions.NetworkException;
import com.expectlost.serialize.SerializerFactory;
import com.expectlost.transport.message.RequestPayload;
import com.expectlost.transport.message.VrpcRequest;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 客户端基础逻辑 每一个代理对象远程调用过程
 */
@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {
    private Registry registry;
    private Class<?> interfaceRef;

    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        /**
         * -------------------封装报文---------------
         */
        RequestPayload payload = RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parameterType(method.getParameterTypes())
                .parametersValue(args)
                .returnType(method.getReturnType())
                .build();
        //todo 需要对各种请求id和类型做处理
        VrpcRequest request = VrpcRequest.builder()
                .requestId(VrpcBootstrap.ID_GENERATOR.getId())
                .compressType(CompressorFactory.getCompressor(VrpcBootstrap.COMPRESS_TYPE).getCode())
                .requestType(RequestType.REQUEST.getId())
                .serializeType(SerializerFactory.getSerializer(VrpcBootstrap.SERIALIZE_TYPE).getCode())
                .timeStamp(System.currentTimeMillis())
                .requestPayload(payload)
                .build();


        //将请求内容存入本地线程中
        VrpcBootstrap.REQUEST_THREAD_LOCAL.set(request);
        InetSocketAddress address = VrpcBootstrap.LOAD_BALANCER.selectServiceAddress(interfaceRef.getName());
        if (log.isDebugEnabled()) {
            log.debug("服务调用方，发现了服务【{}】的可用主机【{}】", interfaceRef.getName(), address);
        }
        /**
         * 尝试获取通道
         */
        Channel channel = getAvailableChannel(address);
        if(log.isDebugEnabled())
        {
            log.debug("获取了和【{}】建立的链接通道 准备发送数据",address);
        }



        /**
         * -----------------同步策略-----------------
         */
//                ChannelFuture channelFuture = channel.writeAndFlush(new Object()).await();
//                //getNow获取当前结果 如果未处理完成 返回null
//                //如果已经完成
//                if (channelFuture.isDone()) {
//                    Object object = channelFuture.getNow();
//                }else if (!channelFuture.isSuccess()){
//                    //捕获异常
//                   Throwable cause = channelFuture.cause();
//                   throw new RuntimeException(cause);
//                }
        /**
         * ------------------异步策略
         */
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        //todo 需要completableFuture暴露出去
//        VrpcBootstrap.PENDING_REQUEST.put(request.getRequestId(), completableFuture);
        VrpcBootstrap.PENDING_REQUEST.put(request.getRequestId(), completableFuture);
        /**
         * -----------------之类使用writeAndFlush 写出一个请求
         */
        channel.writeAndFlush(request).addListener((ChannelFutureListener) promise -> {
            if (!promise.isSuccess()) {
                Throwable cause = promise.cause();
                completableFuture.completeExceptionally(cause);
            }
        });

        VrpcBootstrap.REQUEST_THREAD_LOCAL.remove();
        return completableFuture.get(3, TimeUnit.SECONDS);

    }

    /**
     *  根据地址获取可用通道
     * @return
     */
    private Channel getAvailableChannel(InetSocketAddress address) {
        Channel channel = VrpcBootstrap.CHANNEL_CACHE.get(address);
        if (channel == null) {
            /**
             * await等待连接成功再返回
             */
//                    channel = NettyBootstrapInitializer
//                            .getBootstrap()
//                            .connect(address)
//                            .await()
//                            .channel();

            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            NettyBootstrapInitializer.getBootstrap().connect(address).addListener((ChannelFutureListener) promiss -> {
                if (promiss.isDone()) {
                    //异步 已经完成
                    channelFuture.complete(promiss.channel());
                    if (log.isDebugEnabled()) {
                        log.debug("已经和【{}】成功建立了连接", address);
                    }
                } else if (!promiss.isSuccess()) {
                    channelFuture.completeExceptionally(promiss.cause());
                }
            });
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("获取channel时发生异常",e);
                e.printStackTrace();
                throw new DiscoveryException(e);
            }
            VrpcBootstrap.CHANNEL_CACHE.put(address, channel);
        }
        if (channel == null) {
            log.error("获取【{}】通道时发生了异常.", address);
            throw new NetworkException("获取通道时发生了异常.");
        }
        return channel;
    }
}
