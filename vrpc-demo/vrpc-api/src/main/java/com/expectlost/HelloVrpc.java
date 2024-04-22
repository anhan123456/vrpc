package com.expectlost;

public interface HelloVrpc {

    /**
     * 通用接口 server和client都需要依赖
     *
     * @param msg
     * @return
     */
    String sayHi(String msg);


    Integer sum(Integer a,Integer b);

}
