package com.rockthejvm.aktors

sealed interface Behavior<in T> {
    fun <S:T> ifSameThen(other: Behavior<S>): Behavior<S> =
        if (this == Behaviors.Same) other
        else this
}

object Behaviors {
    fun <T> receive(handler: suspend (ActorContext<T>, T) -> Behavior<T>): Behavior<T> =
        Receive(handler)

    fun <T> receiveMessage(handler: suspend (T) -> Behavior<T>): Behavior<T> =
        Receive { _, msg ->
            handler(msg)
        }

    fun <T> setup(initialization: suspend (ActorContext<T>) -> Behavior<T>): Behavior<T> =
        Setup(initialization)

    @Suppress("UNCHECKED_CAST")
    fun <T> same(): Behavior<T> =
        Same as Behavior<T>

    @Suppress("UNCHECKED_CAST")
    fun <T> stopped(): Behavior<T> =
        Stopped as Behavior<T>

    @Suppress("UNCHECKED_CAST")
    fun <T> empty(): Behavior<T> =
        Empty as Behavior<T>

    class Receive<T>(val handler: suspend (ActorContext<T>, T) -> Behavior<T>): Behavior<T>
    class Setup<T>(val initialization: suspend (ActorContext<T>) -> Behavior<T>): Behavior<T>
    data object Same: Behavior<Nothing>
    data object Stopped: Behavior<Nothing>
    data object Empty: Behavior<Nothing>
}
