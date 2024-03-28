package com.expectlost.channelHandler;

import com.expectlost.channelHandler.handler.MySimpleChannelInBoundHandler;
import com.expectlost.channelHandler.handler.VrpcMessageEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                //消息编码器
                .addLast(new VrpcMessageEncoder())
                .addLast(new MySimpleChannelInBoundHandler());
    }
}
