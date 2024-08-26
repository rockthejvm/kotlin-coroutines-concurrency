package com.rockthejvm.advanced

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.slf4j.LoggerFactory
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

object Channels {
    val LOGGER = LoggerFactory.getLogger(this::class.java)

    // channel = concurrent queue
    // producer-consumer problem

    data class StockPrice(val symbol: String, val price: Double, val timestamp: Long)


    suspend fun pushStocks(channel: SendChannel<StockPrice>) {
        LOGGER.info("Trying to add an element")
        channel.send(StockPrice("AAPL", 100.0, System.currentTimeMillis()))
        LOGGER.info("Pushed an element")
        delay(Random.nextLong(1000))

        LOGGER.info("Trying to add an element")
        channel.send(StockPrice("GOOG", 789.0, System.currentTimeMillis()))
        LOGGER.info("Pushed an element")
        delay(Random.nextLong(1000))

        LOGGER.info("Trying to add an element")
        channel.send(StockPrice("MSFT", 78.0, System.currentTimeMillis()))
        LOGGER.info("Pushed an element")
        // when done, close the channel
        channel.close() // cannot push any new elements into the channel
        // semantic blocking + suspension point
    }

    // least power: use just receive channel or send channel if you can
    suspend fun readStocks(channel: ReceiveChannel<StockPrice>) {
        repeat(4) {
            val result: ChannelResult<StockPrice> = channel.receiveCatching() // can be a value, failed, closed
            val maybePrice = result.getOrNull()
            if (maybePrice != null)
                LOGGER.info("I've read: $maybePrice")
            // receiving is semantically blocking
            // receiving from a closed channel is an error
        }
    }

    /*
        Can use `channel.isClosedForReceive`, but be careful that receiving right after might fail.
        Can use `channel.tryReceive`, but it does NOT wait
     */

    suspend fun stockMarketTerminal() =
        coroutineScope {
            val stocksChannel = Channel<StockPrice>() // both read and write

            launch {
                pushStocks(stocksChannel)
            }

            launch {
                readStocks(stocksChannel)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun stockMarketNicer() =
        coroutineScope {
            val stocksChannel = produce {
                // launches a coroutine with a send()
                pushStocks(channel)
            } // will automatically close the channel

            launch {
                readStocks(stocksChannel)
            }
        }

    /*
        Customize a channel
        - optional capacity
     */
    suspend fun demoCustomizedChannels() =
        coroutineScope {
            val stocksChannel = Channel<StockPrice>(
                capacity = 2,
                onBufferOverflow = BufferOverflow.DROP_OLDEST
            ) // both read and write

            // producer
            launch {
                pushStocks(stocksChannel)
                // buffer items inside
                /*
                    if the buffer is full, any send() will either
                    - semantically block (default)
                    - drop oldest element in buffer
                    - drop the element which wants to get in (latest)
                */
            }

            // consumer
            launch {
                LOGGER.info("Taking a while for the consumer to start...")
                delay(5000)
                readStocks(stocksChannel)
            }
        }

    // closing = cannot send() any more elements, but can receive() any elements CURRENTLY in the channel
    // cancelling = closing + dropping all current elements in the channel
    /*
        onUndeliveredElement triggers if the channel has elements that are about to be discarded:
        - channel gets cancelled with elements inside
        - send() throws an error, e.g. if the channel is closed
        - receive() throws an error, e.g. if someone cancels the coroutine calling receive()
     */
    suspend fun demoOnUndelivered() =
        coroutineScope {
            val channel = Channel<StockPrice>(
                capacity = 10,
                onUndeliveredElement = { stockPrice ->
                    LOGGER.info("Just dropped: $stockPrice")
                }
            )

            val prices = listOf(
                StockPrice("AAPL", 100.0, System.currentTimeMillis()),
                StockPrice("GOOG", 789.0, System.currentTimeMillis()),
                StockPrice("MSFT", 78.0, System.currentTimeMillis()),
                StockPrice("AMZN", 1234.8, System.currentTimeMillis())
            )

            val producer = launch {
                for (price in prices) {
                    LOGGER.info("Sending: $price")
                    channel.send(price)
                    delay(200)
                }
            }

            val consumer = launch {
                repeat(2) {
                    val price = channel.receive()
                    LOGGER.info("Received: $price")
                    delay(1000)
                }
                channel.cancel() // close + drop all elements in the buffer
            }

            producer.join()
            consumer.join()
        }

}

suspend fun main() {
    Channels.demoOnUndelivered()
}
