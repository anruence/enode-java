package org.enodeframework.spring;

import com.mongodb.reactivestreams.client.MongoClient;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.mongo.MongoEventStore;
import org.enodeframework.mongo.MongoPublishedVersionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "spring.enode", name = "eventstore", havingValue = "mongo")
public class EnodeMongoEventStoreAutoConfig extends EnodeVertxAutoConfig {

    @Bean
    public MongoEventStore mongoEventStore(@Qualifier("enodeMongoClient") MongoClient mongoClient, IEventSerializer eventSerializer) {
        MongoEventStore eventStore = new MongoEventStore(mongoClient, eventSerializer);
        return eventStore;
    }

    @Bean
    public MongoPublishedVersionStore mongoPublishedVersionStore(@Qualifier("enodeMongoClient") MongoClient mongoClient) {
        return new MongoPublishedVersionStore(mongoClient);
    }
}
