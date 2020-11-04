db.event_stream.createIndex({aggregateRootId:1,commandId:1},{unique:true})
db.event_stream.createIndex({aggregateRootId:1,version:1},{unique:true})