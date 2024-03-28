package com.expectlost.enumeration;

public enum RespCode {
    SUCCESS((byte)1,"成功"),FAIL((byte)2,"失败");
    private byte code;
    private String desc;

    public String getDesc() {
        return desc;
    }

    public byte getCode() {
        return code;
    }

    RespCode(byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
