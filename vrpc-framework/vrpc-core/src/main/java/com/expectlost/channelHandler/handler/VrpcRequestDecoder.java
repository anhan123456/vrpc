package com.expectlost.channelHandler.handler;

import com.expectlost.compress.Compressor;
import com.expectlost.compress.CompressorFactory;
import com.expectlost.enumeration.RequestType;
import com.expectlost.serialize.Serializer;
import com.expectlost.serialize.SerializerFactory;
import com.expectlost.transport.message.MessageFormatConstant;
import com.expectlost.transport.message.RequestPayload;
import com.expectlost.transport.message.VrpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

@Slf4j
public class VrpcRequestDecoder extends LengthFieldBasedFrameDecoder {
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf byteBuf) {
            return decodeFrame(byteBuf);
        }
        return null;
    }

    //解析报文
    private Object decodeFrame(ByteBuf byteBuf) {
        //解析魔术值
        byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        //检测魔数是否匹配
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != MessageFormatConstant.MAGIC[i]) {
                throw new RuntimeException("获得的请求类型不合法");
            }
        }

        //解析版本号
        byte version = byteBuf.readByte();
        if (version > MessageFormatConstant.VERSION) {
            throw new RuntimeException("获得的请求版本不被支持");
        }
        //头部长度
        short headLength = byteBuf.readShort();
        //总长度
        int fullLength = byteBuf.readInt();
        //请求类型
        byte requestType = byteBuf.readByte();
        //序列化类型
        byte serializeType = byteBuf.readByte();
        //压缩类型
        byte compressType = byteBuf.readByte();
        //请求id
        long requestId = byteBuf.readLong();
        //时间戳
//        long timeStamp = byteBuf.readLong();

        VrpcRequest request = new VrpcRequest();
        request.setRequestType(requestType);
        request.setCompressType(compressType);
        request.setSerializeType(serializeType);
        request.setRequestId(requestId);

        //心跳请求没有载荷 此处可以直接返回
        if (requestType == RequestType.HEARTBEAT.getId()) {
            return request;
        }
        int payloadLength = fullLength - headLength;
        byte[] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);

        //todo 解压缩
        Compressor compressor = CompressorFactory.getCompressor(request.getCompressType()).getCompressor();
        payload = compressor.decompress(payload);

        //todo 反序列化

        Serializer serializer = SerializerFactory.getSerializer(request.getSerializeType()).getSerializer();
        RequestPayload requestPayload = serializer.deserialize(payload, RequestPayload.class);
        request.setRequestPayload(requestPayload);
        log.debug("请求【{}】 已经在服务端完成反序列化工作",request.getRequestId());
        return request;


    }

    public VrpcRequestDecoder() {
        super(
                //最大帧长度
                MessageFormatConstant.MAX_FRAME_LENGTH,
                //长度字段偏移量
                MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH
                        + MessageFormatConstant.HEADER_FIELD_LENGTH,
                MessageFormatConstant.FULL_FIELD_LENGTH,
                //负载适配长度
                -(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH
                        + MessageFormatConstant.HEADER_FIELD_LENGTH + MessageFormatConstant.FULL_FIELD_LENGTH),
                0);
    }
}
