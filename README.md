## 项目介绍

一款基于`Netty`+`Zookeeper`实现的轻量级`Java RPC`框架。提供服务注册，发现，负载均衡，支持`API`调用是一个学习`RPC`工作原理的良好示例。



通过这个简易项目的学习，可以让你从零开始实现一个类似` Dubbo` 服务框架 mini 版`RPC`，学到` RPC` 的底层原理以及各种 `Java` 编码实践的运用。下面看一下`RPC`的调用流程：

<img src="https://nexus-data0312.oss-cn-beijing.aliyuncs.com/aa/rpc2.png" style="zoom:66%;" />



## 功能&设计🚀

### 目录结构

```txt
v-rpc框架
├─v-rpc-core	--rpc核心实现类
├─rpc-demo-consumer	--[示例]服务消费者
├─rpc-demo-api	--存放服务接口
└─rpc-demo-provider	--[示例]服务提供者
```


### 功能：

- 简单易学的代码和框架，**在代码中含有大量注解**
- 基于`Netty`实现长连接通信，包括心跳检测、解决粘包半包等
- 基于`Zookeeper`实现分布式服务注册与发现
- 实现了轮询、随机、加权随机等负载均衡算法
- 实现了同步调用、异步调用多种调用方式
- 支持`jdk`、`javassist`、`CGlib`的动态代理方式
- 支持`fastJson`、`hessian`、`kryo`、`jdk`的序列化方式
- 支持简易扩展点，泛化调用等功能



### 设计：

**`easy-rpc`框架调用流程：**

![架构图](https://nexus-data0312.oss-cn-beijing.aliyuncs.com/aa/rpc9.png)



- **代理层**：负责对底层调用细节的封装；
- **链路层**：负责执行一些自定义的过滤链路，可以供后期二次扩展；
- **路由层**：负责在集群目标服务中的调用筛选策略；
- **协议层**：负责请求数据的转码封装等作用；
- **注册中心**：关注服务的上下线，以及一些权重，配置动态调整等功能；
- **容错层**：当服务调用出现失败之后需要有容错层的兜底辅助；




## 快速开始🌈

### 环境准备

- JDK8 或以上
- Maven 3
- Zookeeper 单机或者集群实例



### 启动示例

**方式一**：使用本项目中的测试用例

1. 将项目克隆到本地

   ```shell
   git clone git@github.com:anhan123456/vrpc.git
   ```

2. IDEA打开项目

   使用 IDEA 打开，等待项目初始化完成。

3. 运行`Zookeeper`

   如果没有安装的过需要先去下载。

4. 修改配置文件

   修改客户端和服务端`vrpc.xml`配置文件中zookeeper的地址(配置文件中位默的地址为`127.0.0.1:2181`)

5. 启动项目（按照图中顺序）

   PS：启动项目前，要确保`zookeeper`已启动.

![image-20240613161053705](https://nexus-data0312.oss-cn-beijing.aliyuncs.com/aa/image-20240613161053705.png)

![image-20240613161119482](https://nexus-data0312.oss-cn-beijing.aliyuncs.com/aa/image-20240613161119482.png)

### 自定义配置

1.自定义接口

![image-20240613160144756](https://nexus-data0312.oss-cn-beijing.aliyuncs.com/aa/image-20240613160144756.png)

2.修改`ConsumerApplication`代理对象

```java
        ReferenceConfig<HelloVrpc> reference = new ReferenceConfig<>();
        reference.setInterface(HelloVrpc.class);
```

3.对`vrpc.xml`进行配置

```xml
<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE configuration SYSTEM "http://expectlost.com/vrpc-config.dtd">
<configuration>
    <port>8089</port>
    <appName>yrpc-default-appName</appName>
    <registry url="zookeeper://127.0.0.1:2181"/>

    <!-- 二选一 -->
    <serializeType type="hessian"/>
    <serializer code="3" name="hession" class="com.expectlost.serialize.impl.HessianSerializer"/>

    <!-- 二选一 -->
    <compressType type="gzip"/>
    <compressor code="1" name="hession"  class="com.expectlost.compress.impl.GzipCompressor"/>

    <!-- 二选一 -->
    <loadBalancer class="com.expectlost.loadbalancer.impl.MinumumResponseTimeLoadBalancer"/>
    <loadBalancerType type="minimumResponseTime"/>

    <idGenerator class="com.expectlost.IdGenerator" dataCenterId="2" MachineId="4"/>
</configuration>
```

## FAQ

**1、`zookeeper` 连接失败**

![](https://nexus-data0312.oss-cn-beijing.aliyuncs.com/aa/linkerror.png)

解决方法：

（1）在本地机器或者在服务器上安装运行 `zookeeper` 实例；

（2）在配置文件中正确配置 `zookeeper` 地址；
