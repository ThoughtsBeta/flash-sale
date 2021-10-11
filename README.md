# Flash-sale

本项目的工程结构如下。更多详情介绍请阅读小册章节。

```
.
├── README.md
├── environment  --> 应用依赖的中间件初始化脚本及数据
│   ├── config
│   ├── data
│   ├── docker-compose.yml
│   └── grafana-storage
├── postman  --> Postman测试脚本
│   └── flash-sale-postman.json
├── flash-sale-app  --> 应用层
│   ├── pom.xml
│   └── src
│       ├── main
│       │   └── java
│       │   │   └── com.actionworks.flashsale
│       └── test
│           ├── java
│           │   └── com
│           │       └── actionworks
│           │           └── flashsale
│           └── resources
│               └── logback-test.xml
├── flash-sale-controller   --> UI层
│   ├── pom.xml
│   └── src
│       └── main
│           └── java
│               └── com.actionworks.flashsale
├── flash-sale-domain   --> 领域层
│   ├── pom.xml
│   └── src
│       ├── main
│       │   └── java
│       │       └── com.actionworks.flashsale
│       └── test
│           └── java
│               └── com.actionworks.flashsale
├── flash-sale-infrastructure  --> 基础设施层
│   ├── pom.xml
│   └── src
│       ├── main
│       │   ├── java
│       │   │   └── com.actionworks.flashsale
│       │   └── resources
│       │       ├── mybatis
│       │       └── mybatis-config.xml
│       └── test
│           ├── java
│           │   └── com.actionworks.flashsale
│           └── resources
│               ├── logback-test.xml
│               └── mybatis-config-test.xml
├── mvnw
├── mvnw.cmd
├── pom.xml
└── start  --> 系统启动入口
    ├── pom.xml
    └── src
        ├── main
        │   ├── java
        │   │   └── com.actionworks.flashsale
        │   └── resources
        └── test
            ├── java
            │   └── com.actionworks.flashsale
            └── resources
                └── logback-test.xml
```
