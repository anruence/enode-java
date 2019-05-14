# ENode
ENode是一个基于【DDD】【CQRS】【ES】【EDA】【In-Memory】架构风格的应用框架
![](enode-arch.png)

## 框架特色

> https://www.cnblogs.com/netfocus/p/5401179.html

 - 一个DDD开发框架，完美支持基于六边形架构思想的开发
 - 实现CQRS架构思想，并且框架提供C端命令的处理结果的返回，支持同步返回和异步返回
 - 内置Event Sourcing（ES）架构模式，让C端的数据持久化变得通用化
 - 聚合根常驻内存，in-memory domain model
 - 聚合根的处理基于Command Mailbox, Event Mailbox的思想，类似Actor Model, Actor Mailbox
 - 严格遵守聚合内强一致性、聚合之间最终一致性的原则
 - Group Commit Domain event
 - 基于聚合根ID+事件版本号的唯一索引，实现聚合根的乐观并发控制
 - 框架保证Command的幂等处理
 - 通过聚合根ID对命令或事件进行路由，做到最小的并发冲突、最大的并行处理
 - 消息发送和接收基于分布式消息队列EQueue，支持分布式部署
 - 基于事件驱动架构范式（EDA，Event-Driven Architecture）
 - 基于队列的动态扩容/缩容
 - EventDB中因为存放的都是不可变的事件，所以水平扩展非常容易，框架可内置支持
 - 支持Process Manager（Saga），以支持一个用户操作跨多个聚合根的业务场景，如订单处理，从而避免分布式事务的使用
 - ENode实现了CQRS架构面临的大部分技术问题，让开发者可以专注于业务逻辑和业务流程的开发，而无需关心纯技术问题

## 系统设计
> https://www.cnblogs.com/netfocus/p/3859371.html

## 使用说明

### Kafka配置 
https://kafka.apache.org/quickstart
```bash
bin/zookeeper-server-start.sh config/zookeeper.properties

bin/kafka-server-start.sh config/server.properties
```
### RocketMQ配置 
https://rocketmq.apache.org/docs/quick-start/
启动RocketMQ服务
```bash
nohup sh bin/mqnamesrv &

nohup sh bin/mqbroker -n 127.0.0.1:9876 &
```

### 分别顺序启动以下项目
 
- command-sender

Command端应用，一般为http服务

- command-consumer

命令处理服务

- event-consumer

领域事件处理服务

### 测试
http://localhost:8001/note/create?id=noteid&t=notetitle&c=commandid


## 参考项目
- https://github.com/tangxuehua/enode
- https://github.com/coffeewar/enode-master