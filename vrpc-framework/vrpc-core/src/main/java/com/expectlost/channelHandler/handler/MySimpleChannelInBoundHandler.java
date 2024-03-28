package com.expectlost.channelHandler.handler;

import com.expectlost.VrpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

public class MySimpleChannelInBoundHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf msg) throws Exception {
        //服务提供方返回的结果
        String result  = msg.toString(Charset.defaultCharset());
        CompletableFuture<Object> completableFuture = VrpcBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(result);
    }
}
