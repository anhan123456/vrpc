package com.expectlost.compress.impl;

import com.expectlost.compress.Compressor;
import com.expectlost.exceptions.CompressException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class GzipCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = null;
        try {
            gzipOutputStream = new GZIPOutputStream(baos);
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            byte[] bytes1 = baos.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("对字节数据进行压缩长度为【{}】->【{}】", bytes.length, bytes1.length);
            }
            return bytes1;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("对字节数据进行压缩时出现了异常", e);
            throw new CompressException();
        }
    }

//    public static void main(String[] args) {
//        String a = "1323123";
//        GzipCompressor gzipCompressor = new GzipCompressor();
//        byte[] bytes = {1, 2, 1, 1, 2};
//        byte[] en = gzipCompressor.compress(bytes);
//        System.out.println(Arrays.toString(en));
//        System.out.println(Arrays.toString(gzipCompressor.decompress(en)));
//    }

    @Override
    public byte[] decompress(byte[] bytes) {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        GZIPInputStream gzipInputStream = null;
        try {
            gzipInputStream = new GZIPInputStream(bais);
            byte[] bytes1 = gzipInputStream.readAllBytes();
            if (log.isDebugEnabled()) {
                log.debug("对字节数据进行解压缩长度为【{}】->【{}】", bytes.length, bytes1.length);
            }
            return bytes1;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("字节解压缩时出现了异常", e);
            throw new CompressException();
        }
    }
}
