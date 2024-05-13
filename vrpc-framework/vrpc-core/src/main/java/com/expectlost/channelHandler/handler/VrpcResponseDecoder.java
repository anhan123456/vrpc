package com.expectlost.channelHandler.handler;

import com.expectlost.compress.Compressor;
import com.expectlost.compress.CompressorFactory;
import com.expectlost.enumeration.RequestType;
import com.expectlost.serialize.Serializer;
import com.expectlost.serialize.SerializerFactory;
import com.expectlost.transport.message.MessageFormatConstant;
import com.expectlost.transport.message.RequestPayload;
import com.expectlost.transport.message.VrpcRequest;
import com.expectlost.transport.message.VrpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

@Slf4j
public class VrpcResponseDecoder extends LengthFieldBasedFrameDecoder {
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
        //响应码
        byte responseCode = byteBuf.readByte();
        //序列化类型
        byte serializeType = byteBuf.readByte();
        //压缩类型
        byte compressType = byteBuf.readByte();
        //请求id
        long requestId = byteBuf.readLong();
        //时间戳
        long timeStamp = byteBuf.readLong();

        VrpcResponse response = new VrpcResponse();
        response.setCode(responseCode);
        response.setCompressType(compressType);
        response.setSerializeType(serializeType);
        response.setRequestId(requestId);
        response.setTimeStamp(timeStamp);
        //心跳请求没有载荷 此处可以直接返回
//        if (requestType == RequestType.HEARTBEAT.getId()) {
//            return request;
//        }
        int resultLength = fullLength - headLength;
        byte[] result = new byte[resultLength];
       byteBuf.readBytes(result);

        //todo 解压缩

        if(result!=null&&result.length>0){

        Compressor compressor = CompressorFactory.getCompressor(response.getCompressType()).getCompressor();
        result = compressor.decompress(result);

        //todo 反序列化

        Serializer serializer = SerializerFactory.getSerializer(response.getSerializeType()).getSerializer();
        Object result_body = serializer.deserialize(result, Object.class);
        response.setBody(result_body);
            if(log.isDebugEnabled())
            {
                log.debug("响应【{}】已经在调用端完成反序列化工作",response.getRequestId());
            }
        }

        return response;


    }

    public VrpcResponseDecoder() {
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
