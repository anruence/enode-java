package com.enodeframework.commanding;

/**
 * @author anruence@gmail.com
 */
public class CommandResult {
    /**
     * Represents the result status of the command.
     */
    private CommandStatus status;
    /**
     * Represents the unique identifier of the command.
     */
    private String commandId;
    /**
     * Represents the aggregate root id associated with the command.
     */
    private String aggregateRootId;
    /**
     * Represents the command result data.
     */
    private String result;
    /**
     * Represents the command result data type.
     */
    private String resultType;

    public CommandResult() {
    }

    /**
     * Parameterized constructor.
     *
     * @param status
     * @param commandId
     * @param aggregateRootId
     * @param result
     * @param resultType
     */
    public CommandResult(CommandStatus status, String commandId, String aggregateRootId, String result, String resultType) {
        this.status = status;
        this.commandId = commandId;
        this.aggregateRootId = aggregateRootId;
        this.result = result;
        this.resultType = resultType;
    }

    @Override
    public String toString() {
        return "CommandResult{" +
                "status=" + status +
                ", commandId='" + commandId + '\'' +
                ", aggregateRootId='" + aggregateRootId + '\'' +
                ", result='" + result + '\'' +
                ", resultType='" + resultType + '\'' +
                '}';
    }

    public CommandStatus getStatus() {
        return status;
    }

    public String getCommandId() {
        return commandId;
    }

    public String getAggregateRootId() {
        return aggregateRootId;
    }

    public String getResult() {
        return result;
    }

    public String getResultType() {
        return resultType;
    }
}
