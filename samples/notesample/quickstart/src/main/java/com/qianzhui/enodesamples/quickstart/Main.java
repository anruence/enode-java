package com.qianzhui.enodesamples.quickstart;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.qianzhui.enode.ENode;
import com.qianzhui.enode.commanding.CommandReturnType;
import com.qianzhui.enode.commanding.ICommandService;
import com.qianzhui.enode.rocketmq.client.impl.NativePropertyKey;
import com.qianzhui.enode.rocketmq.client.ons.PropertyKeyConst;
import com.qianzhui.enodesamples.notesample.commands.ChangeNoteTitleCommand;
import com.qianzhui.enodesamples.notesample.commands.CreateNoteCommand;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.UUID;

public class Main {
    public static void main(String[] args) throws Exception {
        boolean isONS = true;
        /**============= Enode所需消息队列配置，RocketMQ实现 ======*/
        Properties producerSetting = new Properties();
        producerSetting.setProperty(NativePropertyKey.NAMESRV_ADDR, "test.jishulink.com:9876");
        producerSetting.setProperty(NativePropertyKey.ProducerGroup, "NoteSampleProducerGroup");

        Properties consumerSetting = new Properties();
        consumerSetting.setProperty(NativePropertyKey.NAMESRV_ADDR, "test.jishulink.com:9876");
        consumerSetting.setProperty(NativePropertyKey.ConsumerGroup, "NoteSampleConsumerGroup");
        /**=============================================================*/

        /**============= Enode所需消息队列配置，ONS实现 ======*/
        producerSetting = new Properties();
        producerSetting.setProperty(PropertyKeyConst.ProducerId, "PID_EnodeCommon");
        producerSetting.setProperty(PropertyKeyConst.AccessKey, "G6aUujQD6m1Uyy68");
        producerSetting.setProperty(PropertyKeyConst.SecretKey, "TR6MUs6R8dK6GTOKudmaaY80K2dmxI");

        consumerSetting = new Properties();
        consumerSetting.setProperty(PropertyKeyConst.ConsumerId, "CID_NoteSample");
        consumerSetting.setProperty(PropertyKeyConst.AccessKey, "G6aUujQD6m1Uyy68");
        consumerSetting.setProperty(PropertyKeyConst.SecretKey, "TR6MUs6R8dK6GTOKudmaaY80K2dmxI");
        /**=============================================================*/

        /**============= Enode数据库配置（内存实现不需要配置） ===========*/
        Properties properties = new Properties();
        properties.setProperty("driverClassName", "com.mysql.jdbc.Driver");
        /*properties.setProperty("url", "jdbc:mysql://localhost:3306/enode_new");
        properties.setProperty("username", "root");
        properties.setProperty("password", "coffee");*/

        properties.setProperty("url", "jdbc:mysql://localhost:3306/enode");
        properties.setProperty("username", "root");
        properties.setProperty("password", "anruence");
        properties.setProperty("initialSize", "1");
        properties.setProperty("maxTotal", "1");
        /**=============================================================*/

        DataSource dataSource = DruidDataSourceFactory.createDataSource(properties);

        ENode enode = ENode.create("com.qianzhui.enodesamples")
                .registerDefaultComponents()
                .useMysqlComponents(dataSource); // 注销此行，启用内存实现（CommandStore,EventStore,SequenceMessagePublishedVersionStore,MessageHandleRecordStore）

        if (isONS) {
            enode.useONS(producerSetting, consumerSetting, 6000,
                    ENode.COMMAND_SERVICE
                            | ENode.DOMAIN_EVENT_PUBLISHER
                            | ENode.DOMAIN_EVENT_CONSUMER
                            | ENode.COMMAND_CONSUMER
                            | ENode.EXCEPTION_PUBLISHER
                            | ENode.EXCEPTION_CONSUMER
            );
        } else {
            enode.useNativeRocketMQ(producerSetting, consumerSetting, 6000, ENode.COMMAND_SERVICE
                    | ENode.DOMAIN_EVENT_PUBLISHER
                    | ENode.DOMAIN_EVENT_CONSUMER
                    | ENode.COMMAND_CONSUMER);
        }
        enode.start();
        ICommandService commandService = enode.getContainer().resolve(ICommandService.class);
        String noteId = UUID.randomUUID().toString();
        CreateNoteCommand command1 = new CreateNoteCommand(noteId, "Sample Title1");
        ChangeNoteTitleCommand command2 = new ChangeNoteTitleCommand(noteId, "Sample Title3");
        commandService.executeAsync(command1, CommandReturnType.EventHandled).get();
        commandService.executeAsync(command2, CommandReturnType.EventHandled).get();
        System.out.println("Press Enter to exit...");
        System.in.read();
        enode.shutdown();
    }
}
