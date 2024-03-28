package com.expectlost.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用来描述请求调用方 所请求的接口方法的描述
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestPayload implements Serializable {
    //接口名
    private String interfaceName;
    private String methodName;
    //参数列表
    private Class<?>[] parameterType;
    private Object[] parametersValue;

    //返回值封装
    private Class<?> returnType;
}
