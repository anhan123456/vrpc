package com.expectlost.impl;

import com.expectlost.HelloVrpc;
import com.expectlost.HelloVrpc2;
import com.expectlost.annotation.VrpcApi;

@VrpcApi
public class HelloVrpcImpl2 implements HelloVrpc2 {
    @Override
    public String sayHi(String msg) {
        return "提供者处理完毕: "+msg;
    }

    @Override
    public Integer sum(Integer a, Integer b) {
        return a+b;
    }
}
