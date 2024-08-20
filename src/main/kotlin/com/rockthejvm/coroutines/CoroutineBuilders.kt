package com.rockthejvm.coroutines

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import kotlin.random.Random

object CoroutineBuilders {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    suspend fun developer(index: Int) {
        LOGGER.info("[dev $index] I'm a developer. I need coffee.")
        delay(Random.nextLong(1000))
        LOGGER.info("[dev $index] I got coffee, let's get coding!")
    }

    suspend fun projectManager() {
        LOGGER.info("[PM] I'm a PM. I need to check the devs' progress.")
        delay(Random.nextLong(1000)) // can suspend the coroutine
        LOGGER.info("[PM] I checked progress, let's grab lunch.")
    }

    suspend fun startup() {
        LOGGER.info("It's 9AM, let's start")
        // COROUTINE SCOPE
        coroutineScope {
            // the ability to launch coroutines concurrently
            launch { developer(42) }
            launch { projectManager() }
            // manages lifecycle of coroutines ...
        } // will (semantically) block until ALL coroutines inside will finish

        LOGGER.info("It's 6pm, time to go home.")

        coroutineScope {
            launch { developer(1) }
            launch { developer(2) }
        }

        LOGGER.info("It's midnight, time to sleep.")
    }

    suspend fun globalStartup() {
        LOGGER.info("It's 9AM, let's start")
        // global scope - for the duration of the entire app
        val dev1Job: Job = GlobalScope.launch { developer(1) }
        val dev2Job: Job = GlobalScope.launch { developer(2) }
        // easy to leak resources on GlobalScope

        // manually join coroutines
        dev1Job.join() // semantically blocking
        dev2Job.join()

        LOGGER.info("It's 6pm, time to go home.")
    }

    // async - return a value out of a coroutine

    suspend fun developerCoding(index: Int): String {
        LOGGER.info("[dev $index] I'm a developer. I need coffee.")
        delay(Random.nextLong(1000))
        LOGGER.info("[dev $index] I got coffee, let's get coding!")
        return """
            fun main() { println("this is KOTLIN!") }
        """.trimIndent()
    }

    suspend fun projectManagerEstimating(): Int {
        LOGGER.info("[PM] I'm a PM. I need to check the devs' progress.")
        delay(Random.nextLong(1000)) // can suspend the coroutine
        LOGGER.info("[PM] I checked progress, let's grab lunch.")
        return 12
    }

    data class Project(val code: String, val estimation: Int)

    suspend fun startupValues() {
        LOGGER.info("It's 9AM, let's start")
        val project = coroutineScope {
            val deferredCode = async { developerCoding(42) }
            val deferredEstimation = async { projectManagerEstimating() }

            val code = deferredCode.await() // semantically blocking
            val estimation = deferredEstimation.await()

            Project(code, estimation)
        }

        LOGGER.info("It's 9PM, still going. We have the project $project")
    }
}

suspend fun main() {
    CoroutineBuilders.startupValues()
}