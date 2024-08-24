package com.rockthejvm.advanced

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

import org.slf4j.LoggerFactory
import java.util.*
import kotlin.random.Random

data class Product(val id: Int, val name: String, val price: Double)

object Flows {
    val LOGGER = LoggerFactory.getLogger(this::class.java)

    val products = listOf(
        Product(1, "laptop", 999.99),
        Product(1, "smartphone", 1999.99),
        Product(1, "tablet", 799.99),
        Product(1, "smartwatch", 399.99),
    )

    // flow = potentially infinite "list"
    val productsFlow: Flow<Product> = flowOf(
        Product(1, "laptop", 999.99),
        Product(1, "smartphone", 1999.99),
        Product(1, "tablet", 799.99),
        Product(1, "smartwatch", 399.99),
        // emitted at a later point
    )
    val productsFlow_v2 = products.asFlow()

    // emit values
    val delayedProducts: Flow<Product> = flow {
        // emit elements in this scope
        for (product in products) {
            emit(product)
            delay(500) // semantic blocking
        }
    }

    // transformers
    // map
    val prodNamesCaps: Flow<String> =
        delayedProducts.map { it.name.uppercase(Locale.getDefault()) }

    // filter
    val filteredProducts =
        delayedProducts.filter { it.price > 500 }

    // fold - collapse the flow to a single value
    suspend fun totalInventoryValue(): Double =
        delayedProducts.fold(0.0) { acc, product -> acc + product.price }

    val scannedValue: Flow<Double> =
        delayedProducts.scan(0.0) { acc, product -> acc + product.price }

    // handle exceptions
    val flowWithExceptions: Flow<Product> = flow {
        emit(Product(1, "laptop", 999.99))
        if (Random.nextBoolean())
            throw RuntimeException("Network error, cannot fetch product")
        emit(Product(2, "smartphone", 1999.99))
        delay(300)
        emit(Product(3, "tablet", 799.99))
    }.retry(2) { e ->
        e is RuntimeException
    }.catch { e ->
        LOGGER.info("Caught error: $e")
        emit(Product(0, "Unknown", 0.0)) // emit a fallback product
    }

    // side effects on emission
    val productsWithSideEffects: Flow<Product> = delayedProducts.onEach {
        LOGGER.info("generated product: $it")
    }

    // combine multiple flows: merging, concatenating, zipping
    val mergedProducts =
        merge(delayedProducts, productsFlow)

    val concatenatedProducts = flow {
        emitAll(delayedProducts)
        emitAll(productsFlow)
    }

    val orders: Flow<Int> = flow {
        (1..4).forEach {
            delay(600)
            emit(it)
        }
    }

    data class Order(val pid: Int, val quantity: Int)

    val zippedOrders: Flow<Order> =
        delayedProducts.zip(orders) { prod, q -> Order(prod.id, q) }

    /*
        Exercise: weather station
        - transform all the temps to F (9/5 * c + 32)
        - calculate the latest average across all locations - emit all the averages
        - catch any exception and retry the flow, 3 times max
        - print the avg temperatures
        - run this flow for 10 seconds, then cancel it

        - do the same thing PER LOCATION
     */

    data class TemperatureReading(val location: String, val temperature: Double, val timestamp: Long)

    suspend fun readTemperatures(): Flow<TemperatureReading> = flow {
        val locations = listOf("Paris", "Berlin", "Rome", "Bucharest", "Zagreb")
        while (true) {
            val location = locations.random()
            val temperature = (15..40).random() + Random.nextInt(10) * 1.0/10
            val timestamp = System.currentTimeMillis()
            val maybeError = Random.nextInt() % 20
            if (maybeError < 1 && maybeError > -1) // 0.1% chance of error
                throw RuntimeException("Weather station error")
            emit(TemperatureReading(location, temperature, timestamp))
            delay(Random.nextLong(1000))
        }
    }

    suspend fun weatherApp() {
        val transformedFlow_v1 = readTemperatures()
            .map { reading ->
                val fTemp = reading.temperature * 9/5 + 32
                TemperatureReading(reading.location, fTemp, reading.timestamp)
            }.scan(0.0 to 0) { acc, reading ->
                val (sum, count) = acc
                val newSum = sum + reading.temperature
                val newCount = count + 1
                newSum to newCount
            }.map { (sum, count) ->
                if (count == 0) 0.0 else sum / count
                // flow of global average temps
            }.onEach {
                LOGGER.info("Average temp: $it")
            }.retry(3) { cause ->
                LOGGER.info("Caught error, retrying the stream")
                cause is RuntimeException
            }.catch { _ ->
                LOGGER.info("Caught too many errors, stopping the stream")
            }

        val transformedFlow_v2 = readTemperatures()
            .map { reading ->
                val fTemp = reading.temperature * 9/5 + 32
                TemperatureReading(reading.location, fTemp, reading.timestamp)
            }.scan(mapOf<String, Pair<Double, Int>>()) { acc, reading ->
                val (sum, count) = acc[reading.location] ?: (0.0 to 0)
                val newSum = sum + reading.temperature
                val newCount = count + 1
                acc + (reading.location to (newSum to newCount))
                // Map<location to (sum, count)>
            }.map { map ->
                map.mapValues { (location, stats) ->
                    val (sum, count) = stats
                    if (count == 0) 0.0 else sum / count
                }
                // Map<location to avg>
            }.onEach { map ->
                val report = map.toList().joinToString("\n") { (location, avg) ->
                    "$location - $avg F"
                }
                LOGGER.info("\nReport:\n$report")
            }.retry(3) { cause ->
                LOGGER.info("Caught error, retrying the stream")
                cause is RuntimeException
            }.catch { _ ->
                LOGGER.info("Caught too many errors, stopping the stream")
            }

        coroutineScope {
            val job = launch {
                transformedFlow_v2.collect()
            }

            launch {
                delay(10000)
                job.cancel()
            }
        }
    }
}

suspend fun main() {
    Flows.weatherApp()
}