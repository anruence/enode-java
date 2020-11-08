package org.enodeframework.eventing

import org.enodeframework.commanding.ProcessingCommand

/**
 * @author anruence@gmail.com
 */
class EventCommittingContext(val eventStream: DomainEventStream, val processingCommand: ProcessingCommand) {
    lateinit var mailBox: EventCommittingContextMailBox
}