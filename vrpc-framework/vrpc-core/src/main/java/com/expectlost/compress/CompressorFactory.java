package com.expectlost.compress;

import com.expectlost.compress.Compressor;
import com.expectlost.compress.impl.GzipCompressor;
import com.expectlost.config.ObjectWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CompressorFactory {

    private final static Map<String, ObjectWrapper<Compressor>> COMPRESSOR_CACHE = new ConcurrentHashMap<>(8);
    private final static Map<Byte, ObjectWrapper<Compressor>> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>(8);

    static {
        ObjectWrapper<Compressor> gzip = new ObjectWrapper<>((byte) 1, "gzip", new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip", gzip);
        COMPRESSOR_CACHE_CODE.put((byte) 1, gzip);
    }

    /**
     * 使用工厂方法获取一个CompressorWrapper
     * @param compressorType 序列化的类型
     * @return CompressWrapper
     */
    public static ObjectWrapper<Compressor> getCompressor(String compressorType) {
        ObjectWrapper<Compressor> compressorObjectWrapper = COMPRESSOR_CACHE.get(compressorType);
        if(compressorObjectWrapper == null){
            log.error("未找到您配置的【{}】压缩算法，默认选用gzip算法。",compressorType);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressorObjectWrapper;
    }

    public static ObjectWrapper<Compressor> getCompressor(byte serializeCode) {
        ObjectWrapper<Compressor> compressorObjectWrapper = COMPRESSOR_CACHE.get(serializeCode);
        if ((compressorObjectWrapper == null)){
            log.error("未找到您配置的编号为【{}】的压缩算法，默认选用gzip算法。",serializeCode);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressorObjectWrapper;
    }

    /**
     * 给工厂中新增一个压缩方式
     * @param compressorObjectWrapper 压缩类型的包装
     */
    public static void addCompressor(ObjectWrapper<Compressor> compressorObjectWrapper){
        COMPRESSOR_CACHE.put(compressorObjectWrapper.getName(),compressorObjectWrapper);
        COMPRESSOR_CACHE_CODE.put(compressorObjectWrapper.getCode(),compressorObjectWrapper);
    }
}
