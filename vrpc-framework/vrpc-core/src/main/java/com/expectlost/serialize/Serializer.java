package com.expectlost.serialize;


import java.io.Serializable;

public interface Serializer {
    byte[] serialize(Object object);
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
