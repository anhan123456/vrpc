package com.expectlost.serialize;

import com.expectlost.serialize.impl.HessianSerializer;
import com.expectlost.serialize.impl.JdkSerializer;
import com.expectlost.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SerializerFactory {
    private final static ConcurrentHashMap<String, SerializerWrapper> SERIALIZER_CACHE = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<Byte, SerializerWrapper> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>();

    static {
        SerializerWrapper jdk = new SerializerWrapper((byte) 1, "jdk", new JdkSerializer());
        SerializerWrapper json = new SerializerWrapper((byte) 2, "json", new JsonSerializer());
        SerializerWrapper hessian = new SerializerWrapper((byte) 3, "json", new HessianSerializer());
        SERIALIZER_CACHE.put("jdk", jdk);
        SERIALIZER_CACHE.put("json", json);
        SERIALIZER_CACHE.put("hessian", hessian);
        SERIALIZER_CACHE_CODE.put((byte) 1, jdk);
        SERIALIZER_CACHE_CODE.put((byte) 2, json);
        SERIALIZER_CACHE_CODE.put((byte) 3, hessian);
    }

    public static SerializerWrapper getSerializer(String serializeType) {
        SerializerWrapper serializerWrapper = SERIALIZER_CACHE.get(serializeType.toLowerCase());
        if (serializerWrapper == null) {
            log.error("未找到配置的序列化策略",serializeType.toLowerCase());
            return SERIALIZER_CACHE.get("jdk");
        }
        return serializerWrapper;
    }

    public static SerializerWrapper getSerializer(Byte code) {
        SerializerWrapper serializerWrapper = SERIALIZER_CACHE_CODE.get(code);

        if (serializerWrapper == null) {
            return SERIALIZER_CACHE.get("jdk");
        }
        return serializerWrapper;
    }

}
