package com.rockthejvm.aktors

sealed interface Behavior<in T> {
    fun <S:T> ifSameThen(other: Behavior<S>): Behavior<S> =
        if (this == Behaviors.Same) other
        else this
}

object Behaviors {
    fun <T> receiveMessage(handler: (T) -> Behavior<T>): Behavior<T> =
        ReceiveMessage(handler)

    fun <T> setup(initialization: () -> Behavior<T>): Behavior<T> =
        Setup(initialization)

    @Suppress("UNCHECKED_CAST")
    fun <T> same(): Behavior<T> =
        Same as Behavior<T>

    @Suppress("UNCHECKED_CAST")
    fun <T> stopped(): Behavior<T> =
        Stopped as Behavior<T>

    class ReceiveMessage<T>(val handler: (T) -> Behavior<T>): Behavior<T>
    class Setup<T>(val initialization: () -> Behavior<T>): Behavior<T>
    data object Same: Behavior<Nothing>
    data object Stopped: Behavior<Nothing>
}
