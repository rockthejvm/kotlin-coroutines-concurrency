package com.rockthejvm.aktors.playground

import com.rockthejvm.aktors.*
import kotlinx.coroutines.*

class ChildActorsDemo {
    sealed interface Command
    data class CreateChild(val name: String): Command
    data class TellChild(val message: String): Command
    data object StopChild: Command

    object Parent {
        operator fun invoke(): Behavior<Command> =
            idle()

        private fun idle(): Behavior<Command> = Behaviors.receive { ctx, msg ->
            when(msg) {
                is CreateChild -> {
                    ctx.log.info("[parent] Creating child with name ${msg.name}")
                    val childRef = ctx.spawn(msg.name, Child())
                    withChild(childRef)
                }
                else -> {
                    ctx.log.info("[parent] I don't recognize this message: $msg")
                    Behaviors.same()
                }
            }
        }

        private fun withChild(childRef: ActorRef<String>): Behavior<Command> =
            Behaviors.receive { ctx, command ->
                when(command) {
                    is TellChild -> {
                        ctx.log.info("[parent] Sending message to my child: ${command.message}")
                        childRef `!` command.message
                        Behaviors.same()
                    }
                    is StopChild -> {
                        ctx.log.info("[parent] Stopping my child")
                        childRef `!` "STOP"
                        idle()
                    }
                    else -> {
                        ctx.log.info("[parent] I don't recognize this message: $command")
                        Behaviors.same()
                    }
                }
            }
    }

    object Child {
        operator fun invoke(): Behavior<String> = Behaviors.receive { ctx, msg ->
            ctx.log.info("[child] I've received $msg")

            if (msg == "STOP") Behaviors.stopped()
            else Behaviors.same()
        }
    }
}

suspend fun main() =
    ActorSystem.app(ChildActorsDemo.Parent(), "ParentChildDemo") { parent ->
        parent `!` ChildActorsDemo.CreateChild("kid")
        parent `!` ChildActorsDemo.TellChild("Hey kid, let's go play with Kotlin coroutines!")
        delay(1000)
        parent `!` ChildActorsDemo.TellChild("Hey kid, have you done your homework!")
        delay(1000)
        parent `!` ChildActorsDemo.StopChild
        delay(1000)
        parent `!` ChildActorsDemo.TellChild("Hey kid, where are you?")
    }