package com.rockthejvm.jvmConcurrency

import java.util.concurrent.Executors
import kotlin.random.Random

object VirtualThreads {

    // CPUs <--- (OS scheduler) OS threads <- (1 to 1) JVM Threads
    // preemptive scheduling - no control over which threads are suspended at which time

    // Virtual threads - managed/scheduled by the JVM
    // CPUs <--- (OS scheduler) OS threads <--- (JVM scheduler) virtual threads
    // OS threads - 1000s - 10000s
    // Virtual threads - millions easily (on the heap)

    fun indefinitely() {
        val threads = (1..1000000).map { i ->
            Thread.ofVirtual().start {
                while(true) {
                    // do nothing
                }
            }
        }

        Thread.sleep(5000)
        println("virtual threads ok")
    }

    fun demoVTFactory() {
        val factory = Thread.ofVirtual().name("rtjvm-", 0).factory()
        val threads = (1..1000000).map { i ->
            factory.newThread {
                while(true) {
                    Thread.sleep(Random.nextLong(1000))
                    println("[${Thread.currentThread().name}] I'm a virtual thread")
                }
            }.start()
        }

        Thread.sleep(5000)
        println("all virtual threads done")
    }

    // OS threads? as many CPU cores you have

    fun demoVTExecutor() {
//        val executor = Executors.newVirtualThreadPerTaskExecutor()

        val factory = Thread.ofVirtual().name("rtjvm-", 0).factory()
        val executor = Executors.newThreadPerTaskExecutor(factory)

        (1..1000000).map { _ ->
            executor.submit {
                while(true) {
                    Thread.sleep(Random.nextLong(1000))
                    println("[${Thread.currentThread().name}] I'm a task running on a virtual thread")
                }
            }
        }

        Thread.sleep(7000)
        println("all virtual threads done")
    }

    fun threadWhichNeverYields() =
        Runnable {
            println("I'm a virtual thread that will NEVER block")
            while (true) {
                // do nothing
            }
        }

    fun threadWhichWantsToRun() =
        Runnable {
            println("I'd like to run. If this prints, then I'm successful")
        }

    fun cooperativeFailureDemo() {
        val factory = Thread.ofVirtual().name("mini-thread-", 0).factory()
        val executor = Executors.newThreadPerTaskExecutor(factory)

        executor.submit(threadWhichNeverYields())
        executor.submit(threadWhichWantsToRun())

        Thread.sleep(5000)
        executor.shutdown()
    }
    /*
        -Djdk.virtualThreadScheduler.maxPoolSize=1 - max number of OS threads on the JVM
        -Djdk.virtualThreadScheduler.parallelism - number of OS threads / core
     */

    @JvmStatic
    fun main(args: Array<String>) {
        cooperativeFailureDemo()
    }
}