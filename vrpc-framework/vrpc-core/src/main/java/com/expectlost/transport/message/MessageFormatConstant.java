package com.expectlost.transport.message;

import java.nio.charset.StandardCharsets;

public class MessageFormatConstant {
    public static final byte[] MAGIC = "vrpc".getBytes(StandardCharsets.UTF_8);
    public static final byte VERSION = 1;
    //头部信息长度
    public static final short HEADER_LENGTH = (byte)(MAGIC.length+1+2+4+1+1+1+8);
    //头部信息长度占用字节数
    public static final int HEADER_FIELD_LENGTH = 2;
    public final static int MAX_FRAME_LENGTH = 1024*1024;

    public static final int VERSION_LENGTH = 1;

    //总长度字段的占用长度
    public static final int FULL_FIELD_LENGTH = 4;
}
