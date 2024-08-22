package com.rockthejvm.coroutines

import kotlinx.coroutines.*

import org.slf4j.LoggerFactory
import kotlin.random.Random

object Cancellation {
    val LOGGER = LoggerFactory.getLogger(this::class.java)

    suspend fun developer(index: Int) {
        LOGGER.info("[dev $index] I'm a developer, I'm working on a feature")
        while (true) {
            delay(500)
            LOGGER.info("[dev $index] developing...")
        }
    }

    suspend fun ceo(employee: Job) {
        LOGGER.info("[ceo] I'm the CEO, I need to talk to this developer")
        delay(Random.nextLong(2000))
        employee.cancel() // the Job will get cancelled at the next suspension point
        LOGGER.info("[ceo] I have fired the developer")
        employee.invokeOnCompletion { cause ->
            if (cause is CancellationException) {
                LOGGER.info("The CEO terminated the employee's contract")
            }
            LOGGER.info("The developer ceased contract")
        }
    }

    // cancellation is cooperative
    // IF A COROUTINE DOES NOT HAVE A SUSPENSION POINT, IT IS ____NOT____ CANCELABLE

    suspend fun developerWithTry(index: Int) {
        LOGGER.info("[dev $index] I'm a developer, I'm working on a feature")
        while (true) {
            try {
                delay(500) // this point is where the coroutine gets cancelled - will throw the CancellationException
                LOGGER.info("[dev $index] developing...")
            } catch (e: CancellationException) {
                LOGGER.info("[dev $index] Oh no! I'm being fired!")
                // VERY IMPORTANT - continue to throw the cancellation exception
                // otherwise you'll ignore the cancellation - uncancelable
                // throw e
                // CancellationException caught by the continuation -> will terminate the coroutine
            } finally {
                LOGGER.info("[dev $index] I'm done with this startup!")
            }
        }
    }

    // 1. handle the cancellation exception

    /*
        When you cancel a coroutine, all suspension points become throws:

        LOGGER.info("[dev $index] I'm a developer, I'm working on a feature")
        while (true) {
            try {
                throw CancellationException
                LOGGER.info("[dev $index] developing...")
            } catch (e: CancellationException) {
                LOGGER.info("[dev $index] Oh no! I'm being fired!")
            } finally {
                LOGGER.info("[dev $index] I'm done with this startup!")
            }
        }
     */

    suspend fun startup() {
        LOGGER.info("9AM, a beautiful day to change the world")
        coroutineScope {
            val goodDeveloper = launch { developerWithTry(3) }
            val lazyDeveloperJob = launch { developerWithTry(42) }
            launch { ceo(lazyDeveloperJob) }
        }

        LOGGER.info("1AM in the morning, are we still having fun?")
    }

    // 2. resources

    class Laptop(val name: String): AutoCloseable {
        init {
            LOGGER.info("Providing the laptop '$name'")
        }

        override fun close() {
            LOGGER.info("Shutting down the laptop '$name'")
        }
    }

    suspend fun developerAtWork(index: Int) {
        Laptop("The AVENGER").use {laptop ->
            LOGGER.info("[dev $index] I'm a developer, I'm working on '${laptop.name}' on a feature")
            while (true) {
                delay(500)
                LOGGER.info("[dev $index] developing...")
            }
        }
    }

    suspend fun startupResource() {
        LOGGER.info("9AM, a beautiful day to change the world")
        coroutineScope {
            val developer = launch { developerAtWork(10) }
            launch { ceo(developer) }
        }

        LOGGER.info("1AM in the morning, are we still having fun?")
    }

    // 3. canceling child coroutines
    // cancellation propagates to children
    /*
        launch {
            // coroutine 1
            launch {
                // coroutine 2, a child of coroutine 1
            }
        }
     */

    suspend fun startupTeam() {
        LOGGER.info("9AM, a beautiful day to change the world")
        coroutineScope {
            val team = launch {
                (1..10).forEach { launch { developer(it) } }
                // if someone cancels me here, all the coroutines above will get cancelled

                // I can cancel my own children
                coroutineContext.cancelChildren()

                // once a coroutine is cancelled, it CANNOT create other coroutines
                delay(2000)
                LOGGER.info("Trying to hack my way into the startup's budget")
                (100..110).forEach { launch { developer(it) } }
            }
            launch { ceo(team) }
        }

        LOGGER.info("1AM in the morning, are we still having fun?")
    }

}

suspend fun main() {
    Cancellation.startupTeam()
}