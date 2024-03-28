package com.expectlost.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务调用方发起的请求
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VrpcRequest {
    //请求id
    private long requestId;
    //请求类型 压缩类型 参数列表 返回值类型
    private byte requestType;
    private byte compressType;
    private byte serializeType;

    //消息体
    private RequestPayload requestPayload;
}
