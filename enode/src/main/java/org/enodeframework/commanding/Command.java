package org.enodeframework.commanding;

import com.google.common.collect.Maps;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.messaging.Message;

import java.util.Map;

/**
 * @author anruence@gmail.com
 */
public class Command<TAggregateRootId> extends Message implements ICommand {

    public TAggregateRootId aggregateRootId;

    public Command() {
        super();
    }

    public Command(TAggregateRootId aggregateRootId) {
        this(aggregateRootId, Maps.newHashMap());
    }

    public Command(TAggregateRootId aggregateRootId, Map<String, Object> items) {
        super();
        Ensure.notNull(aggregateRootId, "aggregateRootId");
        this.aggregateRootId = aggregateRootId;
        this.items = items;
    }

    /**
     * Init command with id and aggregateRootId
     */
    public Command(String id, TAggregateRootId aggregateRootId) {
        this(id, aggregateRootId, Maps.newHashMap());
    }

    public Command(String id, TAggregateRootId aggregateRootId, Map<String, Object> items) {
        super(id);
        Ensure.notNull(aggregateRootId, "aggregateRootId");
        this.aggregateRootId = aggregateRootId;
        this.items = items;
    }

    @Override
    public String getAggregateRootId() {
        return aggregateRootId.toString();
    }

    public void setAggregateRootId(TAggregateRootId aggregateRootId) {
        this.aggregateRootId = aggregateRootId;
    }
}
