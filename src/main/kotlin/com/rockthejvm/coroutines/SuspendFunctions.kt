package com.rockthejvm.coroutines

import kotlinx.coroutines.*

import org.slf4j.LoggerFactory

object SuspendFunctions {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    suspend fun takeTheBus() { // this code can run on a coroutine
        LOGGER.info("Getting in the bus")
        (0..10).forEach {
            LOGGER.info("${it * 10}% done")
            delay(300) // yielding point - coroutine that runs this code can be SUSPENDED
            // cooperative scheduling
        }
        LOGGER.info("Getting off the bus, I'm done!")
    }

    // suspend functions CANNOT be run from regular functions

    // continuation = state of the code at the point a coroutine is suspended
    suspend fun demoSuspendedCoroutine() {
        LOGGER.info("Starting to run some code")

        val resumedComputation = suspendCancellableCoroutine { continuation ->
            LOGGER.info("This runs when I'm suspended")
            continuation.resumeWith(Result.success(42))
        } // yielding point - coroutine is SUSPENDED

        LOGGER.info("This prints AFTER resuming the coroutine: $resumedComputation")
    }

    // CPS - continuation passing style
    // suspend functions compile to functions with Continuation as their last arg

    // suspend function values (lambdas)
    val suspendLambda: suspend (Int) -> Int = { it + 1 }
    // (Int) -> Int and `suspend` (Int) -> Int are DIFFERENT TYPES

    // suspend lambdas with receivers
    val increment: suspend Int.() -> Int = { this + 1 }

    suspend fun suspendLambdasDemo() {
        LOGGER.info("Suspend call: ${suspendLambda(2)}")
        val four = 3.increment()
        LOGGER.info("Suspend lambda with receivers: $four")
    }

    // Why does it not work with a suspend fun main in the object?
    // @JvmStatic // public static void main(String[], Continuation) - what Kotlin compiles to
    // // public static void main(String[]) - needed for the JVM
    // suspend fun main(args: Array<String>) {
    //     takeTheBus()
    // }
}

suspend fun main() {
    SuspendFunctions.suspendLambdasDemo()
}
