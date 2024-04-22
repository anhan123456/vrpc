package com.expectlost.compress;

import com.expectlost.compress.impl.GzipCompressor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CompressorFactory {
    private final static ConcurrentHashMap<String, CompressWrapper> COMPRESSOR_CACHE = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<Byte, CompressWrapper> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>();

    static {
        CompressWrapper gzip = new CompressWrapper((byte) 1, "jdk", new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip", gzip);
        COMPRESSOR_CACHE_CODE.put((byte) 1, gzip);
    }

    public static CompressWrapper getCompressor(String compressType) {
        CompressWrapper compressWrapper = COMPRESSOR_CACHE.get(compressType.toLowerCase());
        if (compressWrapper == null) {
            log.error("未找到配置的压缩策略",compressType.toLowerCase());
            return COMPRESSOR_CACHE.get("zip");
        }
        return compressWrapper;
    }

    public static CompressWrapper getCompressor(Byte code) {
        CompressWrapper compressWrapper = COMPRESSOR_CACHE_CODE.get(code);
        if (compressWrapper == null) {
            return COMPRESSOR_CACHE.get("zip");
        }
        return compressWrapper;
    }

}
