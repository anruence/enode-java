package com.enode.configurations;

public class ConfigurationSetting {
    /**
     * 处理领域事件的处理器的名字，默认为DefaultEventHandler
     */
    private String domainEventStreamMessageHandlerName;
    /**
     * 默认的数据库配置信息
     */
    private DefaultDBConfigurationSetting defaultDBConfigurationSetting;
    /**
     * 当使用默认的从内存清理聚合根的服务时，该属性用于配置扫描过期的聚合根的时间间隔，默认为5秒；
     */
    private int scanExpiredAggregateIntervalMilliseconds;
    /**
     * 当使用默认的MemoryCache时，该属性用于配置聚合根的最长允许的不活跃时间，超过这个时间就认为是过期，就可以从内存清除了；然后下次如果再需要用的时候再重新加载进来；默认为3天；
     */
    private int aggregateRootMaxInactiveSeconds;

    /**
     * CommandMailBox中的命令处理时一次最多处理多少个命令，默认为1000个
     */
    private int commandMailBoxPersistenceMaxBatchSize;

    /**
     * EventMailBox中的事件持久化时一次最多持久化多少个事件，默认为1000个
     */
    private int eventMailBoxPersistenceMaxBatchSize;

    public ConfigurationSetting() {
        domainEventStreamMessageHandlerName = "DefaultEventProcessor";
        defaultDBConfigurationSetting = new DefaultDBConfigurationSetting();
        scanExpiredAggregateIntervalMilliseconds = 5000;
        aggregateRootMaxInactiveSeconds = 3600 * 24 * 3;
        commandMailBoxPersistenceMaxBatchSize = 1000;
        eventMailBoxPersistenceMaxBatchSize = 1000;
    }

    public String getDomainEventStreamMessageHandlerName() {
        return domainEventStreamMessageHandlerName;
    }

    public void setDomainEventStreamMessageHandlerName(String domainEventStreamMessageHandlerName) {
        this.domainEventStreamMessageHandlerName = domainEventStreamMessageHandlerName;
    }

    public int getScanExpiredAggregateIntervalMilliseconds() {
        return scanExpiredAggregateIntervalMilliseconds;
    }

    public void setScanExpiredAggregateIntervalMilliseconds(int scanExpiredAggregateIntervalMilliseconds) {
        this.scanExpiredAggregateIntervalMilliseconds = scanExpiredAggregateIntervalMilliseconds;
    }

    public int getAggregateRootMaxInactiveSeconds() {
        return aggregateRootMaxInactiveSeconds;
    }

    public void setAggregateRootMaxInactiveSeconds(int aggregateRootMaxInactiveSeconds) {
        this.aggregateRootMaxInactiveSeconds = aggregateRootMaxInactiveSeconds;
    }

    public DefaultDBConfigurationSetting getDefaultDBConfigurationSetting() {
        return defaultDBConfigurationSetting;
    }

    public void setDefaultDBConfigurationSetting(DefaultDBConfigurationSetting defaultDBConfigurationSetting) {
        this.defaultDBConfigurationSetting = defaultDBConfigurationSetting;
    }

    public int getCommandMailBoxPersistenceMaxBatchSize() {
        return commandMailBoxPersistenceMaxBatchSize;
    }

    public void setCommandMailBoxPersistenceMaxBatchSize(int commandMailBoxPersistenceMaxBatchSize) {
        this.commandMailBoxPersistenceMaxBatchSize = commandMailBoxPersistenceMaxBatchSize;
    }

    public int getEventMailBoxPersistenceMaxBatchSize() {
        return eventMailBoxPersistenceMaxBatchSize;
    }

    public void setEventMailBoxPersistenceMaxBatchSize(int eventMailBoxPersistenceMaxBatchSize) {
        this.eventMailBoxPersistenceMaxBatchSize = eventMailBoxPersistenceMaxBatchSize;
    }
}
