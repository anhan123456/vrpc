package com.expectlost.serialize.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.expectlost.exceptions.SerializerException;
import com.expectlost.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Hessian2Output hessian2Output = new Hessian2Output(baos);
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            if (log.isDebugEnabled())
            {
                log.debug("对象【{}】已经使用hessian完成解码操作",object);
            }
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("hessian序列化【{}】时出现异常", object);
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
            Hessian2Input hessian2Input = new Hessian2Input(bais);
            if(log.isDebugEnabled())
            {
                log.debug("类【{}】已经使用hessian完成了序列化操作",clazz.getName());
            }
            return (T) hessian2Input.readObject();
        } catch (Exception e) {
            log.error("hessian反序列化【{}】时出现异常", clazz.getName());
            throw new SerializerException();
        }
    }
}
