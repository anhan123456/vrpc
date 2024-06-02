package com.expectlost.config;

import com.expectlost.IdGenerator;
import com.expectlost.discovery.RegistryConfig;
import com.expectlost.loadbalancer.LoadBalancer;
import com.expectlost.loadbalancer.impl.RoundRobinLoadBalancer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Slf4j
public class Configuration {
    
    // 配置信息-->端口号
    private int port = 8094;
    
    // 配置信息-->应用程序的名字
    private String appName = "default";
    
    // 分组信息
    private String group = "default";
    
    // 配置信息-->注册中心
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");
    
    // 配置信息-->序列化协议
    private String serializeType = "jdk";
    
    // 配置信息-->压缩使用的协议
    private String compressType = "gzip";
    
    // 配置信息-->id发射器
    public IdGenerator idGenerator = new IdGenerator(1, 2);
    
    // 配置信息-->负载均衡策略
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();
    
    // 为每一个ip配置一个限流器
//    private final Map<SocketAddress, RateLimiter> everyIpRateLimiter = new ConcurrentHashMap<>(16);
//    // 为每一个ip配置一个断路器，熔断
//    private final Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = new ConcurrentHashMap<>(16);
//
    // 读xml，dom4j
    public Configuration() {
        // 1、成员变量的默认配置项
        
        // 2、spi机制发现相关配置项
        SpiResolver spiResolver = new SpiResolver();
        spiResolver.loadFromSpi(this);
        
        // 3、读取xml获得上边的信息
        XmlResolver xmlResolver = new XmlResolver();
        xmlResolver.loadFromXml(this);
        
        // 4、编程配置项，yrpcBootstrap提供
    }
    
    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        System.out.println(configuration);
    }
    
}
