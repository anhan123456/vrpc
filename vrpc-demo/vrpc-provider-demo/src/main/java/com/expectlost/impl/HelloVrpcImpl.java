package com.expectlost.impl;

import com.expectlost.HelloVrpc;

public class HelloVrpcImpl implements HelloVrpc {
    @Override
    public String sayHi(String msg) {
        return "hi consumer: "+msg;
    }
}
