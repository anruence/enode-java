db.event_stream.createIndex({aggregateRootId:1,commandId:1},{unique:true})
db.event_stream.createIndex({aggregateRootId:1,version:1},{unique:true})
db.published_version.createIndex({processorName:1,aggregateRootId:1,version:1},{unique:true})