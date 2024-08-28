package com.rockthejvm.aktors

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

class ActorContext<T>(
    val self: ActorRef<T>,
    val name: String,
    // infra
    val job: Job, // coroutine of this actor
    val scope: CoroutineScope,
): ActorScope() {
    val log = LoggerFactory.getLogger(name)

    fun <S> spawn(name: String, behavior: Behavior<S>): ActorRef<S> =
        createActor(behavior, name, scope, buildCoroutineContext(job, name))

    private fun buildCoroutineContext(parentJob: Job, name: String): CoroutineContext =
        parentJob + CoroutineName(name)
}

/*
    data class Command(payload: String, replyTo: ActorRef<>)

    Behavior.setup { ctx ->
        // logging
        ctx.log(...)
        // spawning a child actor
        val childRef = ctx.spawn(name, behavior)

        // later
        childRef `!` Command("some message", ctx.self)

        // request-response
        // asking and getting a value
        // pipe items
    }
 */