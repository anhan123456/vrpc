package com.expectlost.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class AppClient implements Serializable {

    public void run()
    {
        //TODO 定义线程池 EventLoopGroup
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
        //TODO 创建启动辅助类
        Bootstrap bootstrap = new Bootstrap();
        bootstrap = bootstrap.group(group)
                .remoteAddress(new InetSocketAddress(8080))
                //TODO 选择初始化一个什么样的Channel
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new MyChannelHandler2());
                    }
                });
        ChannelFuture channelFuture = null;

            //TODO 尝试连接服务器  同步
            channelFuture = bootstrap.connect().sync();
            //TODO 获取channel 并且写出数据
            channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("hello netty".getBytes(StandardCharsets.UTF_8)));
            //TODO 阻塞程序 等待接收消息
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new AppClient().run();
    }
}
