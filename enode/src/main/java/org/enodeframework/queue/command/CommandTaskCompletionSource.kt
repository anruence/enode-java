package org.enodeframework.queue.command

import org.enodeframework.commanding.CommandResult
import org.enodeframework.commanding.CommandReturnType
import java.util.concurrent.CompletableFuture

class CommandTaskCompletionSource(var aggregateRootId: String, var commandReturnType: CommandReturnType, var taskCompletionSource: CompletableFuture<CommandResult>)