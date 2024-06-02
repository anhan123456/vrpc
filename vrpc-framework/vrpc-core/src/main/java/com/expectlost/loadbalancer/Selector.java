package com.expectlost.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

public interface Selector {
    InetSocketAddress getNext();

    /**
     * 服务动态上线需要rebalance
     */
}
