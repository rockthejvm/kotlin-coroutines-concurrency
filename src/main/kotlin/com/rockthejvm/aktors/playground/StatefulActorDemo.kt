package com.rockthejvm.aktors.playground

import com.rockthejvm.aktors.ActorSystem
import com.rockthejvm.aktors.Behavior
import com.rockthejvm.aktors.Behaviors
import org.slf4j.LoggerFactory

object WordCounter {
    private val log = LoggerFactory.getLogger(this::class.java)

    operator fun invoke(): Behavior<String> = Behaviors.setup {
        log.info("Setting up")
        var total = 0

        Behaviors.receiveMessage { msg ->
            val newCount = msg.split(" ").size
            total += newCount
            log.info("received new message, updated count to $total")
            Behaviors.same()
        }
    }
}

object WordCounterStateless {
    private val log = LoggerFactory.getLogger(this::class.java)

    operator fun invoke(): Behavior<String> =
        active(0)

    private fun active(currentCount: Int): Behavior<String> =
        Behaviors.receiveMessage { msg ->
            val newCount = msg.split(" ").size
            val newTotal = currentCount + newCount
            log.info("received new message, updated count to $newTotal")
            active(newTotal)
        }
}

object StatefulActorDemo {
    suspend fun main() {
        ActorSystem.app(WordCounterStateless(), "WordCounterSystem") { guardian ->
            guardian `!` "This is an actor framework on top of coroutines"
            guardian `!` "Coroutines rock"
        }
    }
}

suspend fun main() {
    StatefulActorDemo.main()
}