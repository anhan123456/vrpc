package com.expectlost.channelHandler.handler;

import com.expectlost.ServiceConfig;
import com.expectlost.VrpcBootstrap;
import com.expectlost.transport.message.RequestPayload;
import com.expectlost.transport.message.VrpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<VrpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, VrpcRequest vrpcRequest) throws Exception {
        RequestPayload requestPayload = vrpcRequest.getRequestPayload();

        //根据载荷内容进行方法调用

        Object object = callTargetMethod(requestPayload);
        System.out.println(object);
        //封装响应

        //写出响应

        channelHandlerContext.channel().writeAndFlush(null);
    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parameterType = requestPayload.getParameterType();
        Object[] parametersValue = requestPayload.getParametersValue();

        /**
         * 寻找合适的类进行方法调用
         */

        ServiceConfig<?> serviceConfig = VrpcBootstrap.SERVICE_LIST.get(interfaceName);
        //具体引用实现
        Object refImpl = serviceConfig.getRef();

        //通过反射调用
        Object result =null;
        try
        {
            Class<?> aClass = refImpl.getClass();
            Method method = aClass.getMethod(methodName, parameterType);
            result = method.invoke(refImpl, parametersValue);
        }catch (InvocationTargetException|NoSuchMethodException|IllegalAccessException e) {
            log.error("调用服务【{}】的方法【{}】时出现异常",interfaceName,methodName,e);
            throw new RuntimeException(e);
        }


        return result;

    }
}
