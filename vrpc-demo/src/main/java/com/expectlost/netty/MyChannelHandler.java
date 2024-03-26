package com.expectlost.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.channels.Channel;
import java.nio.charset.StandardCharsets;

/**
 * 处理器
 */
public class MyChannelHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //TODO 读信息
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println("服务端已经接收到了消息：--》"+byteBuf.toString(StandardCharsets.UTF_8));
        //TODO 通过ctx获取channel
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer("hello client!!!".getBytes(StandardCharsets.UTF_8)));
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
         //出现异常的时候执行的动作（打印并关闭通道）
        cause.printStackTrace();
        ctx.close();
    }

}
