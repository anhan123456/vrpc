package com.expectlost.discovery;

import com.expectlost.Constant;
import com.expectlost.discovery.Registry;
import com.expectlost.discovery.impl.NacosRegistry;
import com.expectlost.discovery.impl.ZookeeperRegistry;
import com.expectlost.exceptions.DiscoveryException;

public class RegistryConfig {
    //定义连接的url zookeeper://127.0.0.1:2181 mysql://192.168.12.125:3306
    private String connectString;

    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }

    /**
     * 使用简单工程通过coDnnectString 获取 registry
     * @return
     */
    public Registry getRegistry() {

        String registryType = getRegistryType(connectString,true).toLowerCase().trim();
        if (registryType.equals("zookeeper"))
        {
            String host = getRegistryType(connectString, false);
            return new ZookeeperRegistry(host, Constant.TIME_OUT);
        }else {
            //**** nacos
            return new NacosRegistry();
        }
//        throw new DiscoveryException("未发现合适的注册中心");

    }
    private String getRegistryType(String connectString,boolean ifType)
    {
        String[] typeAndHost = connectString.split("://");
        if(typeAndHost.length!=2)
        {
            throw new RuntimeException("给定注册中心url不合法");
        }
        return ifType?typeAndHost[0]:typeAndHost[1];
    }
}
