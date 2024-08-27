package com.rockthejvm.aktors

import kotlinx.coroutines.channels.*

/*
    - receives a message of a certain type
    - wraps a coroutine channel
    - a method tell(msg: your type) -> push an element to that channel
    - a method `!`
 */
class ActorRef<T>(private val mailbox: SendChannel<T>) {
    suspend fun tell(msg: T) =
        mailbox.send(msg)

    suspend infix fun `!`(msg: T) =
        tell(msg)
}