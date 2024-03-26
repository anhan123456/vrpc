package com.expectlost;

import com.expectlost.netty.AppClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class NettyTest {

    @Test
    public void testCompositeByteBuf() {
        ByteBuf header = Unpooled.buffer();
        ByteBuf body = Unpooled.buffer();
        /**
         * 通过逻辑组装而不是物理拷贝 实现在jvm中零拷贝
         */
        CompositeByteBuf byteBuf = Unpooled.compositeBuffer();
        byteBuf.addComponents(header, body);
    }

    @Test
    public void testWrapper() {

        byte[] buf = new byte[1024];
        byte[] buf2 = new byte[1024];
        /**
         * 共享byte数组的内容而不是拷贝 这也算零拷贝
         */
        ByteBuf byteBuf = Unpooled.wrappedBuffer(buf, buf2);
    }

    @Test
    public void testSlice() {
        byte[] buf = new byte[1024];
        byte[] buf2 = new byte[1024];
        ByteBuf byteBuf = Unpooled.wrappedBuffer(buf, buf2);
        /**
         * 同样可以将一个byteBuf分割成多个 使用共享地址 零拷贝
         */
        ByteBuf slice = byteBuf.slice(1, 5);
        ByteBuf slice1 = byteBuf.slice(6, 15);
    }

    @Test
    public void testMessage() throws IOException {
        ByteBuf message = Unpooled.buffer();
        //魔数值
        message.writeBytes("ydl".getBytes(StandardCharsets.UTF_8));
        message.writeByte(1);
        message.writeShort(125);
        message.writeInt(256);
        message.writeByte(1);
        message.writeByte(0);
        message.writeByte(2);
        message.writeLong(251455L);

        //TODO 对象流转换成字节数组
        AppClient appClient = new AppClient();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(appClient);

        message.writeBytes(outputStream.toByteArray());

       printAsBinary(message);

    }

    public static void printAsBinary(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(), bytes);

        String binaryString = ByteBufUtil.hexDump(bytes);
        StringBuilder formattedBinary = new StringBuilder();

        for (int i = 0; i < binaryString.length(); i += 2) {
            formattedBinary.append(binaryString.substring(i, i + 2)).append(" ");
        }

        System.out.println("Binary representation: " + formattedBinary.toString());

    }


    @Test
    public void testCompress() throws IOException {
        byte[] buf = new byte[]{12,12,12,12,12,12,12,12,12,5,34,14,74,54,12,12,12,12,12,12,12,12,12,5,34,14,74,54,12,12,12,12,12,12,12,12,12,5,34,14,74,54,12,12,12,12,12,12,12,12,12,5,34,14,74,54,12,12,12,12,12,12,12,12,12,5,34,14,74,54,12,12,12,12,12,12,12,12,12,5,34,14,74,54};
        //压缩
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream  = new GZIPOutputStream(baos);
        gzipOutputStream.write(buf);
        gzipOutputStream.finish();
        byte[] bytes = baos.toByteArray();
        System.out.println(buf.length+"-->"+bytes.length);
        System.out.println(Arrays.toString(bytes));

    }
    @Test
    public void testDeCompress() throws IOException {
        byte[] buf = new byte[]{31, -117, 8, 0, 0, 0, 0, 0, 0, -1, -29, -31, -127, 2, 86, 37, 62, 47, 51, 30, -86, -16, 0, -56, 85, 91, -41, 84, 0, 0, 0};

        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        GZIPInputStream gzipInputStream = new GZIPInputStream(bais);
        byte[] bytes = gzipInputStream.readAllBytes();
        System.out.println(Arrays.toString(bytes));


    }
}
