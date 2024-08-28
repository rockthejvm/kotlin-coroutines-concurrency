package com.rockthejvm.aktors

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.slf4j.LoggerFactory

/*
    - name and channel args
    - start() method which pops elements off the channel and logs them
 */
internal class Actor<T>(
    private val name: String,
    private val channel: Channel<T>,
    private val job: Job,
    private val scope: CoroutineScope
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val self = ActorRef(channel)
    private val ctx = ActorContext(self, name, job, scope)

    suspend fun run(startBehavior: Behavior<T>) {
        var behavior = startBehavior
        var newBehavior = behavior
        while(true) {
            when (behavior) {
                is Behaviors.Receive -> {
                    val msg = channel.receive()
                    val handle = behavior.handler
                    newBehavior = handle(ctx, msg)
                    behavior = newBehavior.ifSameThen(behavior)
                }

                is Behaviors.Setup -> {
                    newBehavior = behavior.initialization(ctx)
                    behavior = newBehavior.ifSameThen(Behaviors.stopped())
                }

                is Behaviors.Empty -> {
                    // nothing happens
                    delay(1000)
                }

                is Behaviors.Same ->
                    throw IllegalStateException("The INSTANCE 'Behaviors.Same' is illegal, probably a bug in the code.")

                is Behaviors.Stopped -> {
                    channel.close() // prevent other coroutines from sending new messages
                    // TODO - what do you do with messages that arrive at this (closed) channel?
                    // dead letters - receives any messages that don't have a valid destination
                    job.cancel()
                }
            }

            yield() // suspension point is important
        }
    }
}

