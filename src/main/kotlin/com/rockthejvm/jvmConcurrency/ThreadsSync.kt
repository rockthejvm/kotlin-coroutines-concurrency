package com.rockthejvm.jvmConcurrency

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.random.Random

object ThreadsSync {

    // race condition
    var coffeeMachine: Int = 0
    val coffeeMachineLock = ReentrantLock()

    fun developer(index: Int) =
        Runnable {
            println("[$index] I'm a developer, I need coffee.")
            Thread.sleep(Random.nextLong(1000)) // up to 1s randomly
            coffeeMachine += 1 // race condition
            /*
                - start
                - read coffeeMachine
                - compute coffeeMachine+1
                - set coffeeMachine to that
                - end
             */
            println("[$index] I got coffee.")
        }

    // locking
    fun syncDeveloper(index: Int) = Runnable {
        println("[$index] I'm a developer, I need coffee.")
        Thread.sleep(Random.nextLong(1000)) // up to 1s randomly

        // block other threads if I'm here
        coffeeMachineLock.lock()
        // thread-safe: only one thread can access this area
        coffeeMachine += 1 // SAFE to increment!
        coffeeMachineLock.unlock()
        // unblock other threads waiting

        println("[$index] I got coffee.")
    }

    fun developerRaceCondition() {
        for (i in (1..10000)) {
            Thread(syncDeveloper(i)).start()
        }

        Thread.sleep(3000)

        // expected 10000, got 9684 (< 10000) unless you lock the race condition
        println("Coffee machine has issued $coffeeMachine coffees.")
    }

    fun developersAndMaintenance() {
        val developers = (1..10000).map { Thread(syncDeveloper(it)) }
        developers.forEach { it.start() }

        // maintainer
        val maintainer = thread {
            Thread.sleep(2000)
            coffeeMachineLock.lock()
            // run some maintenance
            println("Maintenance in progress. Please wait...")
            Thread.sleep(2000)
            println("Maintenance complete.")
            coffeeMachineLock.unlock() // unblocks the rest of the developers
        }

        developers.forEach { it.join() }
        maintainer.join()

        println("Coffee machine has issued $coffeeMachine coffees.") // 10000
    }

    // deadlock
    var userStories = 0
    var estimation = 0
    val usLock = ReentrantLock()
    val estLock = ReentrantLock()

    fun manager() = Thread {
        println("I'm a PM, I need an estimation to proceed with user stories")
        estLock.lock()
        Thread.sleep(1000)
        usLock.lock()
        userStories = 4
        println("I'm the PM, user stories are completed.")
        estLock.unlock()
        usLock.unlock()
    }

    fun developer() = Thread {
        println("I'm a developer, I need user stories to make an estimation")
        usLock.lock()
        Thread.sleep(1000)
        estLock.lock()
        println("I'm the developer, estimation is done.")
        estimation = 15
        usLock.unlock()
        estLock.unlock()
    }

    fun demoDeadlock() {
        manager().start()
        developer().start()
    }

    // livelock = multiple threads DO WORK, but don't make any progress
    data class Friend(val name: String) {
        var side = "right"
        val lock = ReentrantLock()

        fun bow(another: Friend) {
            println("$name: I am bowing to my friend ${another.name}")
            another.rise(this)
            println("$name: my friend ${another.name} has risen")
            another.pass(this)
            pass(another)
        }

        fun rise(another: Friend) {
            println("$name: I am rising to my friend ${another.name}")
        }

        fun switchSide() {
            lock.lock()
            side =
                if (side == "right") "left"
                else "right"
            lock.unlock()
        }

        fun pass(another: Friend) {
            while (this.side == another.side) {
                println("$name: Oh, ${another.name}, please go first...")
                switchSide()
                bow(another)
            }
        }
    }

    fun demoLivelock() {
        val jacques = Friend("Jacques")
        val pierre = Friend("Pierre")

        Thread { jacques.bow(pierre) }.start()
        Thread { pierre.bow(jacques) }.start()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        demoLivelock()
    }
}