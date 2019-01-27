# ENode

Java版ENode，翻译自汤雪华ENode(C#)。ENode是一个开源的应用开发框架，为开发人员提供了一套完整的基于DDD+CQRS+ES+(in-memory)+EDA架构风格的解决方案。

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

领域时间处理服务

### 测试

http://localhost:8001/note/create?id=noteid&t=notetitle


## 参考项目
- https://github.com/tangxuehua/enode
- https://github.com/coffeewar/enode-master