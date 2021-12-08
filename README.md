# 高并发秒杀系统

[![CircleCI](https://circleci.com/gh/ThoughtsBeta/flash-sale/tree/master.svg?style=svg&circle-token=1d98eb40c37d2519d48180e9ed8d9db4e78ff358)](https://circleci.com/gh/ThoughtsBeta/flash-sale/tree/master) [![](https://img.shields.io/badge/JDK-java8-red.svg)]() [![](https://img.shields.io/badge/掘金小册-高并发秒杀的设计精要与实现-blue.svg "高并发秒杀设计精要与实现")](https://juejin.cn/book/7008372989179723787) 

本项目的工程结构如下。更多详情介绍请阅读小册章节。

```
.
├── README.md
├── Dockerfile
├── diagram
├── environment
│   ├── config
│   │   ├── elk
│   │   │   ├── config
│   │   │   │   └── export.ndjson
│   │   │   ├── elasticsearch.yml
│   │   │   ├── kibana.yml
│   │   │   ├── logstash.yml
│   │   │   └── pipeline
│   │   │       └── logstash.conf
│   │   ├── mysql
│   │   │   └── init
│   │   │       ├── flash_sale_init.sql
│   │   │       ├── flash_sale_init_sharding_0.sql
│   │   │       ├── flash_sale_init_sharding_1.sql
│   │   │       └── nacos_init.sql
│   │   ├── mysql-exporter
│   │   ├── nacos
│   │   │   ├── custom.properties
│   │   │   └── nacos-standlone-mysql.env
│   │   ├── prometheus
│   │   │   └── prometheus.yml
│   │   └── rocketmq
│   │       └── broker.conf
│   ├── data
│   ├── docker-compose-elk.yml
│   ├── docker-compose.yml
│   └── pressure-test
│       ├── flash-sale-pressure-test-full.jmx
│       ├── flash-sale-pressure-test-get-item.jmx
│       ├── flash-sale-pressure-test-place-order.jmx
│       └── tokens.csv
├── flash-sale-app
│   ├── pom.xml
│   └── src
│       ├── main
│       │   └── java
│       │       └── com
│       │           └── actionworks
│       │               └── flashsale
│       │                   └── app
│       │                       ├── auth
│       │                       │   ├── AuthorizationService.java
│       │                       │   ├── AuthorizationServiceImpl.java
│       │                       │   └── model
│       │                       │       ├── AuthResult.java
│       │                       │       ├── ResourceEnum.java
│       │                       │       └── Token.java
│       │                       ├── event
│       │                       │   └── handler
│       │                       │       ├── BucketsEventHandler.java
│       │                       │       ├── FlashActivityEventHandler.java
│       │                       │       ├── FlashIOrderEventHandler.java
│       │                       │       └── FlashItemEventHandler.java
│       │                       ├── exception
│       │                       │   ├── AppErrorCode.java
│       │                       │   ├── AppException.java
│       │                       │   ├── PlaceOrderException.java
│       │                       │   └── StockBucketException.java
│       │                       ├── model
│       │                       │   ├── PlaceOrderTask.java
│       │                       │   ├── builder
│       │                       │   │   ├── FlashActivityAppBuilder.java
│       │                       │   │   ├── FlashItemAppBuilder.java
│       │                       │   │   ├── FlashOrderAppBuilder.java
│       │                       │   │   ├── PlaceOrderTaskBuilder.java
│       │                       │   │   └── StockBucketBuilder.java
│       │                       │   ├── command
│       │                       │   │   ├── BucketsArrangementCommand.java
│       │                       │   │   ├── FlashActivityPublishCommand.java
│       │                       │   │   ├── FlashItemPublishCommand.java
│       │                       │   │   └── FlashPlaceOrderCommand.java
│       │                       │   ├── constants
│       │                       │   │   └── CacheConstants.java
│       │                       │   ├── dto
│       │                       │   │   ├── FlashActivityDTO.java
│       │                       │   │   ├── FlashItemDTO.java
│       │                       │   │   ├── FlashOrderDTO.java
│       │                       │   │   ├── StockBucketDTO.java
│       │                       │   │   └── StockBucketSummaryDTO.java
│       │                       │   ├── enums
│       │                       │   │   ├── ArrangementMode.java
│       │                       │   │   └── OrderTaskStatus.java
│       │                       │   ├── query
│       │                       │   │   ├── FlashActivitiesQuery.java
│       │                       │   │   ├── FlashItemsQuery.java
│       │                       │   │   └── FlashOrdersQuery.java
│       │                       │   └── result
│       │                       │       ├── AppMultiResult.java
│       │                       │       ├── AppResult.java
│       │                       │       ├── AppSimpleResult.java
│       │                       │       ├── OrderTaskHandleResult.java
│       │                       │       ├── OrderTaskSubmitResult.java
│       │                       │       └── PlaceOrderResult.java
│       │                       ├── mq
│       │                       │   ├── OrderTaskPostService.java
│       │                       │   ├── RocketMQOrderTaskConsumerService.java
│       │                       │   └── RocketMQOrderTaskPostService.java
│       │                       ├── scheduler
│       │                       │   ├── FlashItemWarmUpScheduler.java
│       │                       │   ├── SchedulerConfiguration.java
│       │                       │   └── StocksAlignScheduler.java
│       │                       ├── security
│       │                       │   ├── DefaultSecurityService.java
│       │                       │   └── SecurityService.java
│       │                       ├── service
│       │                       │   ├── activity
│       │                       │   │   ├── DefaultActivityAppService.java
│       │                       │   │   ├── FlashActivityAppService.java
│       │                       │   │   └── cache
│       │                       │   │       ├── FlashActivitiesCacheService.java
│       │                       │   │       ├── FlashActivityCacheService.java
│       │                       │   │       └── model
│       │                       │   │           ├── FlashActivitiesCache.java
│       │                       │   │           └── FlashActivityCache.java
│       │                       │   ├── bucket
│       │                       │   │   ├── BucketsAPPService.java
│       │                       │   │   ├── BucketsArrangementService.java
│       │                       │   │   ├── DefaultBucketsAPPService.java
│       │                       │   │   └── DefaultBucketsArrangementService.java
│       │                       │   ├── item
│       │                       │   │   ├── DefaultFlashItemAppService.java
│       │                       │   │   ├── FlashItemAppService.java
│       │                       │   │   └── cache
│       │                       │   │       ├── FlashItemCacheService.java
│       │                       │   │       ├── FlashItemsCacheService.java
│       │                       │   │       └── model
│       │                       │   │           ├── FlashItemCache.java
│       │                       │   │           └── FlashItemsCache.java
│       │                       │   ├── order
│       │                       │   │   ├── DefaultFlashOrderAppService.java
│       │                       │   │   └── FlashOrderAppService.java
│       │                       │   ├── placeorder
│       │                       │   │   ├── PlaceOrderService.java
│       │                       │   │   ├── buckets
│       │                       │   │   │   └── cache
│       │                       │   │   │       └── BucketsCacheService.java
│       │                       │   │   ├── normal
│       │                       │   │   │   ├── NormalPlaceOrderService.java
│       │                       │   │   │   └── cache
│       │                       │   │   │       └── NormalStockCacheService.java
│       │                       │   │   └── queued
│       │                       │   │       ├── PlaceOrderTaskService.java
│       │                       │   │       ├── QueuedPlaceOrderService.java
│       │                       │   │       └── QueuedPlaceOrderTaskService.java
│       │                       │   └── stock
│       │                       │       ├── ItemStockCacheService.java
│       │                       │       └── model
│       │                       │           └── ItemStockCache.java
│       │                       └── util
│       │                           ├── MD5OrderTaskIdGenerateService.java
│       │                           ├── MultiPlaceOrderTypesCondition.java
│       │                           ├── OrderNoGenerateContext.java
│       │                           ├── OrderNoGenerateService.java
│       │                           ├── OrderTaskIdGenerateService.java
│       │                           └── SnowflakeOrderNoGenerateService.java
│       └── test
│           ├── java
│           │   └── com
│           │       └── actionworks
│           │           └── flashsale
│           │               └── app
│           └── resources
│               ├── logback-test.xml
│               └── stock
│                   ├── decrease_item_stock.lua
│                   ├── increase_item_stock.lua
│                   └── init_item_stock.lua
├── flash-sale-controller
│   ├── pom.xml
│   └── src
│       └── main
│           └── java
│               └── com
│                   └── actionworks
│                       └── flashsale
│                           └── controller
│                               ├── config
│                               │   ├── LogbackConfiguration.java
│                               │   └── LogbackInterceptor.java
│                               ├── constants
│                               │   └── ExceptionCode.java
│                               ├── exception
│                               │   ├── BadRequestExceptionHandler.java
│                               │   ├── ExceptionResponse.java
│                               │   └── InternalExceptionHandler.java
│                               ├── model
│                               │   ├── builder
│                               │   │   ├── BucketsBuilder.java
│                               │   │   ├── FlashActivityBuilder.java
│                               │   │   ├── FlashItemBuilder.java
│                               │   │   ├── FlashOrderBuilder.java
│                               │   │   └── ResponseBuilder.java
│                               │   ├── request
│                               │   │   ├── BucketsArrangementRequest.java
│                               │   │   ├── FlashActivitiesRequest.java
│                               │   │   ├── FlashActivityPublishRequest.java
│                               │   │   ├── FlashItemPublishRequest.java
│                               │   │   ├── FlashItemsRequest.java
│                               │   │   └── FlashPlaceOrderRequest.java
│                               │   └── response
│                               │       ├── FlashActivityResponse.java
│                               │       ├── FlashItemResponse.java
│                               │       └── FlashOrderResponse.java
│                               └── resource
│                                   ├── BucketsStockController.java
│                                   ├── FlashActivityController.java
│                                   ├── FlashItemController.java
│                                   └── FlashOrderController.java
├── flash-sale-domain
│   ├── pom.xml
│   └── src
│       ├── main
│       │   └── java
│       │       └── com
│       │           └── actionworks
│       │               └── flashsale
│       │                   └── domain
│       │                       ├── event
│       │                       │   ├── DomainEventPublisher.java
│       │                       │   ├── FlashActivityEvent.java
│       │                       │   ├── FlashActivityEventType.java
│       │                       │   ├── FlashActivityOfflineEvent.java
│       │                       │   ├── FlashItemEvent.java
│       │                       │   ├── FlashItemEventType.java
│       │                       │   ├── FlashItemOfflineEvent.java
│       │                       │   ├── FlashOrderEvent.java
│       │                       │   ├── FlashOrderEventType.java
│       │                       │   ├── LocalDomainEventPublisher.java
│       │                       │   ├── StockBucketEvent.java
│       │                       │   └── StockBucketEventType.java
│       │                       ├── exception
│       │                       │   ├── DomainErrorCode.java
│       │                       │   └── DomainException.java
│       │                       ├── model
│       │                       │   ├── Bucket.java
│       │                       │   ├── PageResult.java
│       │                       │   ├── PagesQueryCondition.java
│       │                       │   ├── StockDeduction.java
│       │                       │   ├── entity
│       │                       │   │   ├── FlashActivity.java
│       │                       │   │   ├── FlashItem.java
│       │                       │   │   └── FlashOrder.java
│       │                       │   └── enums
│       │                       │       ├── BucketStatus.java
│       │                       │       ├── BucketType.java
│       │                       │       ├── FlashActivityStatus.java
│       │                       │       ├── FlashItemStatus.java
│       │                       │       └── FlashOrderStatus.java
│       │                       ├── repository
│       │                       │   ├── BucketsRepository.java
│       │                       │   ├── FlashActivityRepository.java
│       │                       │   ├── FlashItemRepository.java
│       │                       │   └── FlashOrderRepository.java
│       │                       └── service
│       │                           ├── BucketsDomainService.java
│       │                           ├── FlashActivityDomainService.java
│       │                           ├── FlashItemDomainService.java
│       │                           ├── FlashOrderDomainService.java
│       │                           ├── StockDeductionDomainService.java
│       │                           └── impl
│       │                               ├── BucketsDomainServiceImpl.java
│       │                               ├── BucketsStockDeductionService.java
│       │                               ├── FlashActivityDomainServiceImpl.java
│       │                               ├── FlashItemDomainServiceImpl.java
│       │                               ├── FlashOrderDomainServiceImpl.java
│       │                               └── NormalStockDeductionDomainService.java
│       └── test
│           └── java
│               └── com
│                   └── actionworks
│                       └── flashsale
│                           └── domain
├── flash-sale-infrastructure
│   ├── pom.xml
│   └── src
│       ├── main
│       │   ├── java
│       │   │   └── com
│       │   │       └── actionworks
│       │   │           └── flashsale
│       │   │               ├── cache
│       │   │               │   ├── DistributedCacheService.java
│       │   │               │   ├── localcache
│       │   │               │   │   └── LocalCacheService.java
│       │   │               │   └── redis
│       │   │               │       ├── RedisCacheService.java
│       │   │               │       ├── RedisClient.java
│       │   │               │       └── util
│       │   │               │           └── ProtoStuffSerializerUtil.java
│       │   │               ├── config
│       │   │               │   ├── ColaConfig.java
│       │   │               │   ├── DataSourceInitFunc.java
│       │   │               │   ├── LogbackAopTrace.java
│       │   │               │   ├── RedisConfig.java
│       │   │               │   └── annotion
│       │   │               │       └── BetaTrace.java
│       │   │               ├── controller
│       │   │               │   └── exception
│       │   │               │       ├── AuthException.java
│       │   │               │       └── ErrorCode.java
│       │   │               ├── lock
│       │   │               │   ├── DistributedLock.java
│       │   │               │   ├── DistributedLockFactoryService.java
│       │   │               │   └── redisson
│       │   │               │       ├── RedissonConfig.java
│       │   │               │       └── RedissonLockService.java
│       │   │               ├── persistence
│       │   │               │   ├── convertor
│       │   │               │   │   ├── BucketBuilder.java
│       │   │               │   │   ├── FlashActivityBuilder.java
│       │   │               │   │   ├── FlashItemBuilder.java
│       │   │               │   │   └── FlashOrderBuilder.java
│       │   │               │   ├── mapper
│       │   │               │   │   ├── BucketMapper.java
│       │   │               │   │   ├── FlashActivityMapper.java
│       │   │               │   │   ├── FlashItemMapper.java
│       │   │               │   │   └── FlashOrderMapper.java
│       │   │               │   ├── model
│       │   │               │   │   ├── BaseDO.java
│       │   │               │   │   ├── BucketDO.java
│       │   │               │   │   ├── FlashActivityDO.java
│       │   │               │   │   ├── FlashItemDO.java
│       │   │               │   │   └── FlashOrderDO.java
│       │   │               │   └── repository
│       │   │               │       ├── FlashActivityRepositoryImpl.java
│       │   │               │       ├── FlashItemRepositoryImpl.java
│       │   │               │       ├── FlashOrderRepositoryImpl.java
│       │   │               │       └── StockBucketRepositoryImpl.java
│       │   │               └── util
│       │   │                   ├── Base64Util.java
│       │   │                   ├── SnowflakeIdWorker.java
│       │   │                   └── StringUtil.java
│       │   └── resources
│       │       ├── mybatis
│       │       │   ├── FlashActivityMapper.xml
│       │       │   ├── FlashBucketMapper.xml
│       │       │   ├── FlashItemMapper.xml
│       │       │   └── FlashOrderMapper.xml
│       │       └── mybatis-config.xml
│       └── test
│           ├── java
│           │   └── com
│           │       └── actionworks
│           │           └── flashsale
│           └── resources
│               ├── logback-test.xml
│               └── mybatis-config-test.xml
├── mvnw
├── mvnw.cmd
├── pom.xml
├── postman
│   └── flash-sale-postman.json
└── start
    ├── pom.xml
    ├── src
    │   ├── main
    │   │   ├── java
    │   │   │   └── com
    │   │   │       └── actionworks
    │   │   │           └── flashsale
    │   │   │               └── FlashSaleApplication.java
    │   │   └── resources
    │   │       ├── application-docker.properties
    │   │       ├── application-local.properties
    │   │       ├── application-sharding.properties
    │   │       ├── application.properties
    │   │       └── logback-spring.xml
    │   └── test
    │       ├── java
    │       │   └── com
    │       │       └── actionworks
    │       │           └── flashsale
    │       │               ├── FlashSaleApplicationTests.java
    │       │               └── TestApplication.java
    │       └── resources
    │           └── logback-test.xml
    └── start.iml


```
