package com.expectlost.serialize.impl;

import com.expectlost.exceptions.SerializerException;
import com.expectlost.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class JdkSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        // 针对不同的消息类型需要做不同的处理，心跳的请求，没有payload
        if (object == null) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(baos);
            outputStream.writeObject(object);
            if(log.isDebugEnabled())
            {
                log.debug("对象【{}】已经完成了序列化操作",object);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("序列化【{}】时出现异常", object);
            throw new SerializerException();
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {

        if (bytes == null || clazz == null) {
            return null;
        }
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream objectOutputStream = new ObjectInputStream(bais);
            if(log.isDebugEnabled())
            {
                log.debug("类【{}】已经完成了序列化操作",clazz.getName());
            }
            return (T) objectOutputStream.readObject();
        } catch (Exception e) {
            throw new SerializerException();
        }


    }
}
