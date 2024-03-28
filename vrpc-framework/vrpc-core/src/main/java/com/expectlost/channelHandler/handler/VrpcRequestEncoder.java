package com.expectlost.channelHandler.handler;

import com.expectlost.enumeration.RequestType;
import com.expectlost.transport.message.MessageFormatConstant;
import com.expectlost.transport.message.RequestPayload;
import com.expectlost.transport.message.VrpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 4byte magic魔数值 --->"vrpc".getBytes()
 * 1byte version --->1
 * 2byte header length 头部长度
 * 4byte full length 报文长度
 * 1byte serialize
 * 1byte compress
 * 1byte requestType
 * <p>
 * <p>
 * body
 */

/**
 * 出站时第一个经过的处理器 进行数据编码
 */
@Slf4j
public class VrpcRequestEncoder extends MessageToByteEncoder<VrpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, VrpcRequest vrpcRequest, ByteBuf byteBuf) throws Exception {

        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        //总长度
        byteBuf.writerIndex(byteBuf.writerIndex() + 4);
        byteBuf.writeByte(vrpcRequest.getRequestType());
        byteBuf.writeByte(vrpcRequest.getSerializeType());
        byteBuf.writeByte(vrpcRequest.getCompressType());

        //8字节请求id
        byteBuf.writeLong(vrpcRequest.getRequestId());

//        if(vrpcRequest.getRequestType() ==RequestType.HEARTBEAT.getId())
//        {
//            int wirterIndex = byteBuf.writerIndex();
//            byteBuf.writerIndex(MessageFormatConstant.MAGIC.length
//                    + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH);
//            byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH);
//            byteBuf.writerIndex(wirterIndex);
//            return;
//        }

        //写入请求体 requestPayload
        byte[] body = getBodyBytes(vrpcRequest.getRequestPayload());
        if(body!=null)
        {
            byteBuf.writeBytes(body);
        }
        int bodyLength  = body ==null ?0:body.length;

        //重新处理报文总长度
        int wirterIndex = byteBuf.writerIndex();
        //将写指针位置移动到总长度位置
        byteBuf.writerIndex(MessageFormatConstant.MAGIC.length
                + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH);
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH +bodyLength);
        byteBuf.writerIndex(wirterIndex);
        if (log.isDebugEnabled())
        {
            log.debug("请求【{}】 已经完成报文编码",vrpcRequest.getRequestId());
        }
    }

    private byte[] getBodyBytes(RequestPayload requestPayload) {

        if(requestPayload==null)
        {
            return null;
        }
        //todo 针对不同消息类型做不同处理 心跳请求没有payload
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(requestPayload);
            //压缩
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("序列化时出现了异常");
            throw new RuntimeException(e);
        }
    }


}















