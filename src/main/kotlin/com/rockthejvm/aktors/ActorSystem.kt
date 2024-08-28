package com.rockthejvm.aktors

import kotlinx.coroutines.*

/*
    - spawn the first actor of this system - guardian actor
    - run that action on the guardian actor

    args
        - name of the system = name of the guardian
        - lambda that runs arbitrary code on the guardian actor (ActorRef)
 */
object ActorSystem: ActorScope() {
    suspend fun <T> app(
        guardianBehavior: Behavior<T>,
        name: String,
        action: suspend (ActorRef<T>) -> Unit
    ): Unit =
        coroutineScope {
            val guardian = createActor<T>(guardianBehavior, name, this, CoroutineName(name))
            action(guardian)
        }
}