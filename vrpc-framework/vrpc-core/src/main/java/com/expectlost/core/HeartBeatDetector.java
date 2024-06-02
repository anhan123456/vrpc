package com.expectlost.core;

import com.expectlost.NettyBootstrapInitializer;
import com.expectlost.VrpcBootstrap;
import com.expectlost.compress.CompressorFactory;
import com.expectlost.discovery.Registry;
import com.expectlost.enumeration.RequestType;
import com.expectlost.exceptions.DiscoveryException;
import com.expectlost.serialize.SerializerFactory;
import com.expectlost.transport.message.VrpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class HeartBeatDetector {
    public static void detect(String serviceName) {
        //从注册中心拉取服务列表,并建立连接
        Registry registry = VrpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry();
        //将连接进行缓存
        List<InetSocketAddress> addresses = registry.lookup(serviceName);

        for (InetSocketAddress address : addresses) {
            try {
                if (!VrpcBootstrap.CHANNEL_CACHE.containsKey(address)) {
                    Channel channel = NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                    VrpcBootstrap.CHANNEL_CACHE.put(address, channel);
                }


            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            //定时发送心跳请求

            Thread thread = new Thread(() -> {
                new Timer().scheduleAtFixedRate(new MyTimerTask(), 0, 2000);
            }, "vrpc-HeartbeatDetector-thread");
            thread.setDaemon(true);
            thread.start();

        }


    }

    private static class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            VrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.clear();
            //遍历所有Channel
            Map<InetSocketAddress, Channel> channelCache = VrpcBootstrap.getInstance().CHANNEL_CACHE;
            channelCache.forEach((address, channel) -> {
                int tryTimes = 3;
                while (tryTimes>0) {
                    long start = System.currentTimeMillis();
                    VrpcRequest vrpcRequest = VrpcRequest.builder()
                            .requestId(VrpcBootstrap.getInstance().getConfiguration().getIdGenerator().getId())
                            .compressType(CompressorFactory.getCompressor(VrpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
                            .requestType(RequestType.HEARTBEAT.getId())
                            .serializeType(SerializerFactory.getSerializer(VrpcBootstrap.getInstance().getConfiguration().getSerializeType()).getCode())
                            .requestPayload(null)
                            .timeStamp(start)
                            .build();

                    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                    VrpcBootstrap.PENDING_REQUEST.put(vrpcRequest.getRequestId(), completableFuture);

                    channel.writeAndFlush(vrpcRequest).addListener((ChannelFutureListener) promise -> {
                        if (!promise.isSuccess()) {
                            Throwable cause = promise.cause();
                            completableFuture.completeExceptionally(cause);
                        }
                    });
                    //TODO 获取时间戳信息

                    Long endTime = 0L;
                    try {
                        // completableFuture.get();
                        completableFuture.get(1, TimeUnit.SECONDS);
                        endTime = System.currentTimeMillis();
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        tryTimes--;
                        log.error("和地址为【{}】的主机连接发生异常，正在进行第【{}】次重试", channel.remoteAddress(),3-tryTimes);
                        if(tryTimes ==0)
                        {
                            VrpcBootstrap.CHANNEL_CACHE.remove(address);
                            break;
                        }

                        try {
                            Thread.sleep(10*(new Random().nextInt(5)));
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        continue;
                    }
                    Long time = endTime - start;

                    //使用treeMap进行缓存
                    VrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.put(time, address);
                    log.debug("和【{}】服务器的响应时间是【{}】.", address, time);
                    break;
                }
            });
        }
    }
}
