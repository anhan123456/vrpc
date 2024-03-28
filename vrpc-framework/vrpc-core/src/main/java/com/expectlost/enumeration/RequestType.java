package com.expectlost.enumeration;

import lombok.Data;

/**
 * 用来标记请求类型
 */
public enum RequestType {
    REQUEST((byte) 1,"base_request"),HEARTBEAT((byte)2,"heartbeat_request");

    public byte getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    private byte id;
    private String type;

    RequestType(byte id, String type) {
        this.id = id;
        this.type = type;
    }
}
