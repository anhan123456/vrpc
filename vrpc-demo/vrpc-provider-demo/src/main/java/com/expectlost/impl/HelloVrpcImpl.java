package com.expectlost.impl;

import com.expectlost.HelloVrpc;

public class HelloVrpcImpl implements HelloVrpc {
    @Override
    public String sayHi(String msg) {
        return "提供者处理完毕: "+msg;
    }

    @Override
    public Integer sum(Integer a, Integer b) {
        return a+b;
    }
}
