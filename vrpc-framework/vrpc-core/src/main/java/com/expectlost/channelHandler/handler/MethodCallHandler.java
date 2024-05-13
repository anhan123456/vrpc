package com.expectlost.channelHandler.handler;

import com.expectlost.ServiceConfig;
import com.expectlost.VrpcBootstrap;
import com.expectlost.enumeration.RequestType;
import com.expectlost.enumeration.RespCode;
import com.expectlost.transport.message.RequestPayload;
import com.expectlost.transport.message.VrpcRequest;
import com.expectlost.transport.message.VrpcResponse;
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

        Object result = null;
        //根据载荷内容进行方法调用
        //如果不是心跳才调用
        if(vrpcRequest.getRequestType()!= RequestType.HEARTBEAT.getId()){
            //封装响应
           result = callTargetMethod(requestPayload);
            if(log.isDebugEnabled())
            {
                log.debug("请求【{}】已经在服务端完成方法调用",vrpcRequest.getRequestId());
            }
        }

        //写出响应
        VrpcResponse response = new VrpcResponse();
        response.setCode(RespCode.SUCCESS.getCode());
        response.setCompressType(vrpcRequest.getCompressType());
        response.setRequestId(vrpcRequest.getRequestId());
        response.setSerializeType(vrpcRequest.getSerializeType());
        response.setBody(result);

        channelHandlerContext.channel().writeAndFlush(response);
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
