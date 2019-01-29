package com.enode.rocketmq.message.config;

public class MultiGroupProps {

    private RocketMQProps commandProps;

    private RocketMQProps eventProps;

    private RocketMQProps applicationProps;

    private RocketMQProps exceptionProps;

    private int registerFlag;

    private int listenPort;

    public RocketMQProps getCommandProps() {
        return commandProps;
    }

    public void setCommandProps(RocketMQProps commandProps) {
        this.commandProps = commandProps;
    }

    public RocketMQProps getEventProps() {
        return eventProps;
    }

    public void setEventProps(RocketMQProps eventProps) {
        this.eventProps = eventProps;
    }

    public RocketMQProps getApplicationProps() {
        return applicationProps;
    }

    public void setApplicationProps(RocketMQProps applicationProps) {
        this.applicationProps = applicationProps;
    }

    public RocketMQProps getExceptionProps() {
        return exceptionProps;
    }

    public void setExceptionProps(RocketMQProps exceptionProps) {
        this.exceptionProps = exceptionProps;
    }

    public int getRegisterFlag() {
        return registerFlag;
    }

    public void setRegisterFlag(int registerFlag) {
        this.registerFlag = registerFlag;
    }

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }
}
