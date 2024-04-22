package com.expectlost.serialize.impl;

import com.alibaba.fastjson2.JSON;
import com.expectlost.exceptions.SerializerException;
import com.expectlost.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Slf4j
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        // 针对不同的消息类型需要做不同的处理，心跳的请求，没有payload
        if (object == null) {
            return null;
        }
        log.debug("对象【{}】已经使用json完成解码操作", object);
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        T parse = JSON.parseObject(bytes,clazz);
        log.debug("类【{}】已经使用json完成了序列化操作", clazz.getName());
        return  parse;
    }
}
