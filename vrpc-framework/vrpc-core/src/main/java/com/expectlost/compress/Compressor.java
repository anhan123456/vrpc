package com.expectlost.compress;

public interface Compressor {
    /**
     * 压缩
     * @param bytes
     * @return
     */
    byte[] compress(byte[]bytes);

    /**
     * 解压缩
     * @param bytes
     * @return
     */
    byte[] decompress(byte[]bytes);
}
