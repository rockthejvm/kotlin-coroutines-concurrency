package com.rockthejvm.aktors

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.coroutines.CoroutineContext

/*
    - call scope.launch - start a new coroutine with a new Actor
    - call the run() on that actor in that coroutine
    - return an actorRef with the actor's channel
 */
open class ActorScope {
    protected fun <T> createActor(
        behavior: Behavior<T>,
        name: String,
        scope: CoroutineScope,
        context: CoroutineContext
    ): ActorRef<T> {
        val mailbox = Channel<T>(capacity = Channel.UNLIMITED) // can configure it
        scope.launch(context) {
            val actor = Actor(name, mailbox, coroutineContext.job, scope)
            actor.run(behavior)
        }
        return ActorRef(mailbox)
    }
}