package org.enodeframework.commanding.impl

import org.enodeframework.commanding.ICommandHandlerProxy

class HandlerFindResult(var findStatus: HandlerFindStatus, var findHandler: ICommandHandlerProxy?) {

    constructor(findStatus: HandlerFindStatus) : this(findStatus, null)

    companion object {
        var NotFound: HandlerFindResult = HandlerFindResult(HandlerFindStatus.NotFound)
        var TooManyHandlerData: HandlerFindResult = HandlerFindResult(HandlerFindStatus.TooManyHandlerData)
        var TooManyHandler: HandlerFindResult = HandlerFindResult(HandlerFindStatus.TooManyHandler)
    }

}