package com.expectlost;

import com.expectlost.channelHandler.ConsumerChannelInitializer;
import com.expectlost.channelHandler.handler.MySimpleChannelInBoundHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 提供bootstrap单例
 */
@Slf4j
public class NettyBootstrapInitializer {

    private static Bootstrap bootstrap = new Bootstrap();

    private NettyBootstrapInitializer() {

    }

    static {
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                //TODO 选择初始化一个什么样的Channel
                .channel(NioSocketChannel.class)
                .handler(new ConsumerChannelInitializer());
    }

    public static Bootstrap getBootstrap() {
        return bootstrap;
    }
}
