package com.enode.commanding;

public class CommandResult {
    private CommandStatus status;
    private String commandId;
    private String aggregateRootId;
    private String result;
    private String resultType;

    public CommandResult() {
    }

    public CommandResult(CommandStatus status, String commandId, String aggregateRootId, String result, String resultType) {
        this.status = status;
        this.commandId = commandId;
        this.aggregateRootId = aggregateRootId;
        this.result = result;
        this.resultType = resultType;
    }

    @Override
    public String toString() {
        return String.format("[CommandId=%s,Status=%s,AggregateRootId=%s,Result=%s,ResultType=%s]",
                commandId,
                status,
                aggregateRootId,
                result,
                resultType);
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
