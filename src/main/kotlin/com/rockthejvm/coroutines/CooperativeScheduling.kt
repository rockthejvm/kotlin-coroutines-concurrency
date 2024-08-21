package com.rockthejvm.coroutines

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import kotlin.random.Random

object CooperativeScheduling {
    val LOGGER = LoggerFactory.getLogger(this::class.java)

    suspend fun greedyDeveloper() {
        LOGGER.info("I want all the coffee!!")
        while (System.currentTimeMillis() % 10000 != 0L) {
            // do nothing
        }
        LOGGER.info("I'm done with coffee, maybe now can I code!")
    }

    suspend fun developer(index: Int) {
        LOGGER.info("[dev $index] I turn coffee into code.")
        delay(Random.nextLong(1000)) // suspension point
        LOGGER.info("[dev $index] I got coffee, let's turn it into code!")
    }

    suspend fun almostGreedyDeveloper() {
        LOGGER.info("I want all the coffee!!")
        while (System.currentTimeMillis() % 10000 != 0L) {
            yield() // fundamental suspension point
        }
        LOGGER.info("I'm done with coffee, maybe now can I code!")
    }

    // functions that can suspend a coroutine
    // yield()
    // delay(...)
    // join(), await()/awaitAll()

    // never run CPU-intensive coroutines without some suspension points
    // in other words, be fair to other coroutines :)

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun startup() {
        LOGGER.info("It's 9am, let's get going")

        val singleThread = Dispatchers.Default.limitedParallelism(1)

        coroutineScope {
            launch(context = singleThread) { developer(42) }
            launch(context = singleThread) { almostGreedyDeveloper() }
        }
        LOGGER.info("It's 1am in the morning, let's go to sleep")
    }
}

suspend fun main() {
    CooperativeScheduling.startup()
}