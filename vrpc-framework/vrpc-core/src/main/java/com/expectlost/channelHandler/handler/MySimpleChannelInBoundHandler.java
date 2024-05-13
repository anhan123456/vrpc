package com.expectlost.channelHandler.handler;

import com.expectlost.VrpcBootstrap;
import com.expectlost.transport.message.VrpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MySimpleChannelInBoundHandler extends SimpleChannelInboundHandler<VrpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, VrpcResponse response) throws Exception {

        //服务提供方返回的结果
        Object result = response.getBody();
        result = result == null ? new Object() : result;
        CompletableFuture<Object> completableFuture = VrpcBootstrap.PENDING_REQUEST.get(response.getRequestId());
        completableFuture.complete(result);
    }
}
