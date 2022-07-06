[![CircleCI](https://circleci.com/gh/ThoughtsBeta/flash-sale/tree/master.svg?style=svg&circle-token=1d98eb40c37d2519d48180e9ed8d9db4e78ff358)](https://circleci.com/gh/ThoughtsBeta/flash-sale/tree/master) [![](https://img.shields.io/badge/JDK-java8-red.svg)]() [![](https://img.shields.io/badge/掘金小册-高并发秒杀的设计精要与实现-blue.svg "高并发秒杀设计精要与实现")](https://juejin.cn/book/7008372989179723787) [![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/ThoughtsBeta/flash-sale.svg)](http://isitmaintained.com/project/ThoughtsBeta/flash-sale "Average time to resolve an issue") [![Percentage of issues still open](http://isitmaintained.com/badge/open/ThoughtsBeta/flash-sale.svg)](http://isitmaintained.com/project/ThoughtsBeta/flash-sale "Percentage of issues still open") ![license](https://img.shields.io/github/license/ThoughtsBeta/flash-sale.svg)

# 高并发多方案秒杀架构

本源码是掘金小册的配套源码，旨在帮助小册读者从源码**解构高并发设计的核心要义**。源码包含两个部分：**[核心应用](https://github.com/ThoughtsBeta/flash-sale)**和**[网关应用](https://github.com/ThoughtsBeta/flash-sale-gateway)**。受限于版权要求，如需详尽的源码解读，请参阅掘金小册《[高并发秒杀设计精要与实现](https://juejin.cn/book/7008372989179723787)》。

**源码核心特性**

* 基于Spring Boot和Spring Cloud的完整分布式架构应用实践；
* 本地缓存与分布式缓存的设计技巧；
* 同步下单和高并发库存扣减方式；
* 异步队列下单和库存扣减；
* 去中心化分库分表和分桶库存扣减；
* 限流的原理和应用；
* 黑白攻防和安全风控策略；
* 领域驱动设计方法与实践；
* 限流、降级与熔断落地与实践；
* 容器化技术与Swarm集群部署；
* Redis+Nacos+RocketMQ+ELK等10+中间件的应用与实践；
* 应用动态配置方法和实践；
* 分布式架构的度量与监控；
* RESTful APIs设计与体验。

**链路视角下的整体架构**

![image-20211206171026724](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7e32915193c74ce6aaf9ba42d3400487~tplv-k3u1fbpfcp-zoom-1.image)

## 一、源码快速上手指南

为了方便读者快速上手源码，我们制作了视频指南，请移步[B站查看](https://www.bilibili.com/video/BV1EF41187ij?share_source=copy_web)。

[![Snip20211130_288.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/5fed62008afb44b49aaa420f514bda2c~tplv-k3u1fbpfcp-watermark.image?)](https://www.bilibili.com/video/BV1EF41187ij?share_source=copy_web)

## 二、源码结构

![image-20211209171902241](https://writting.oss-cn-beijing.aliyuncs.com/image-20211209171902241.png)

## 三、技术选型概览 

为了兼顾不同层次的读者，降低对技术理解的门槛和成本，在具体的技术选型上我们主要采用目前市面上主流的技术产品和方案：

* 应用框架：Spring Boot，Spring Cloud Gateway，Srping Cloud  Sleuth
* 数据库：MySQL+Mybatis
* 缓存：Redis+本地缓存
* 单元测试：Junit
* 容器化：Docker
* 容器化管理：Swarm、Portainer
* 可观性与可视化监控：Prometheus、Grafana
* 限流、降级与熔断：Sentinel
* 动态配置：Nacos
* 日志治理：Elasticsearch、Logstash、Kibana
* 测试工具：Postman和Jmeter

## 四、如何启动并运行应用

对于FlashSale所使用到的中间件，我们提供了基于Docker-compose的完整方案，读者可以在Docker环境下一键安装。下载并打开源码之后，在项目的根目录下，你会看到`enviroment`目录。这个目录有四个相关的额文件和文件夹：

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/05af4c0dfd6d487abdeea83a24f94e94~tplv-k3u1fbpfcp-watermark.image?)

* **docker-compose.yml**：【完整版】中间件部署脚本。相关中间件的安装脚本，你可以通过执行`docker-compose -f docker-compose.yml up`命令安装所依赖的全部中间件；
* **docker-compose-light.yml**：【轻量版】中间件部署脚本（**本地开发推荐**），移除了非必要的中间件服务。相关中间件的安装脚本，你可以通过执行`docker-compose -f docker-compose-light.yml up`命令安装所依赖的全部中间件；
* **docker-cluster-middlewares.yml**：【集群化】的中间件部署方案，适用于Swarm网络下的集群部署，具体部署方式可以参考第15章节；
* **docker-cluster-apps.yml**：【集群化】的应用部署方案，适用于Swarm网络下的集群部署，具体部署方式可以参考第15章节；
* **config**:相关中间件的配置文件所在位置，包括Prometheus和MYSQL等配置；
* **grafana-storage**: grafana存储的配置数据。之所以要把这个文件也提供出来，主要是因为Grafana的数据源和报表配置相对比较麻烦，自行配置可能需要倒腾好一阵子，我们提供出来便可以直接加载使用；
* **data**:部分中间件的数据存储位置，包括MYSQL等。这个目录的数据是由中间件系统运行时产生，数据多且杂乱，我们并没有把它放在git中，因此你下载源码后不会看到它，但是运行时就会出现。那为什么要把它放在这个位置，而不是计算机系统的其他位置？这个主要是为了方便数据查看和管理，你可以随时清除所有数据重新来过，当然也可以把它放在任何地方。

### 第一步：启动中间件

1. 下载源码后进入`environment`目录，执行`docker-compose -f docker-compose-light.yml up `启动中间件；
2. 如果你对Docker命令不熟悉，建议安装[Docker Desktop](https://www.docker.com/products/docker-desktop)简化容器管理，可以直观看到容器的启动状态和日志输出；
3. 需要停止所有容器时，请执行`docker-compose -f docker-compose-light.yml down`；
4. 需要重新创建所有容器时，请执行`docker-compose -f docker-compose-light.yml up --force-recreate`.

#### 关于数据库中库表的初始化

对于FlashSale所使用的业务表，我们已经将初始化脚本放在`enviroment/config/mysql`  中，**docker-compose在安装完MYSQL之后，便会执行数据表初始化动作，实现数据库的开箱即用**。

```
.
├── config
│   └── mysql
│   	   └── init
│   	      ├── flash_sale_init.sql // 默认主库初始化语句
│   	      ├── flash_sale_init_sharding_0.sql // #0号数据库初始化语句
│   	      ├── flash_sale_init_sharding_1.sql // #1号数据库初始化语句
│   	      └── nacos_init.sql //Nacos持久化语句
├── docker-cluster-apps.yml
├── docker-cluster-middlewares.yml
├── docker-compose-light.yml
└── docker-compose.yml
```


需要稍微注意的时，我们为MYSQL提供了两份初始化脚本：`flash_sale_init.sql`和`nacos_init.yml`，前者是业务表初始化脚本，后者是Nacos的初始化脚本，因为FlashSale需要借助Nacos所提供的动态配置功能，但是Nacos默认是内存存储，所以我们为它实现了基于MYSQL的持久化存储方案。

### 第二步：通过IDE启动应用运行

在启用应用前，请务必确保已成功执行第一步，并且各中间件容器启动成功。

1. 下载源码后执行`./mvnw clean install`完成系统依赖包的安装（更快应用构建可以考虑使用[mvnd](https://github.com/apache/maven-mvnd)）；
2. 选择`start`模块中的`com.actionworks.flashsale.FlashSaleApplication`作为程序入口运行。
>**特别提醒**
>本地启动时请在IDE中指定properties为`local`.

在调试阶段，推荐使用这种方式。FlashSale启动时，将会连接到前面所安装的中间件。


**可选：通过Docker启动运行**

除了在IDE启动FlashSale之外，通过Docker启动也是一种非常便捷的方案。

1. 通过下面的命令构建FlashSale本地镜像：

```shell
docker build -t flash-sale-app . 
```

构建完成后，通过`docker images`查看镜像是否已经存在。

2. 将下面的配置添加到前面所说的`docker-compose.yml`中，在运行中间的时候，也将同时启动系统。当然，我们也可以通过独立的文件运行。需要注意的是，在通过docker运行时，FlashSale将和中间件共处同一个网络中，我们为此创建了独立的配置文件，在运行时需要指定`docker` 配置。

```yml
services:
  flash-sale-app:
    image: flash-sale-app
    container_name: flash-sale-app
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    ports:
      - 8090:8090
    networks:
      - thoughts-beta-cluster-apps
    restart: on-failure
  flash-sale-gateway:
    image: flash-sale-gateway
    container_name: flash-sale-gateway
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    ports:
      - 8080:8080
    networks:
      - thoughts-beta-cluster-apps
    restart: on-failure
```

## 五、 如何测试接口

在完成中间件的安装和初始化，并启动应用后，接下来我们可以试着测试接口，来判断中间件和应用是否已经就绪并工作正常。

同样的，我们不会让读者自己创建脚本和准备测试数据，毕竟这不符合我们**读者至上**和**开箱即用**的原则。为此，在项目的根目录下，你会看到我们提供的`postman`目录，它是Postman的测试脚本，包含了接口定义和测试数据，你可以直接选择某个接口点击测试即可。

Postman的脚本位置如下所示：

```shell
├── environment
│   ├── config
│   ├── data
│   ├── docker-compose.yml
│   └── grafana-storage
└── postman
    └── flash-sale-postman.json # 测试脚本
```

![image-20211011232435923](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6ae8034b52214884b446ca224a6774d2~tplv-k3u1fbpfcp-zoom-1.image)


## 关于作者

![image-20211111232745734](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/87887f3c60044ce1b91e43108ca6ab32~tplv-k3u1fbpfcp-zoom-1.image)

* [《王者并发课》专栏](https://juejin.cn/column/6963590682602635294)
* 订阅号：MetaThoughts


