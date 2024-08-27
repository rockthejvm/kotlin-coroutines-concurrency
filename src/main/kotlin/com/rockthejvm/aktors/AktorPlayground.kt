package com.rockthejvm.aktors

object AktorPlayground {
    suspend fun main() {
        ActorSystem.app<String>("FirstActorSystem") { guardian ->
            (1..100).forEach { i ->
                guardian `!` "Message $i"
            }
        }
    }
}

suspend fun main() {
    AktorPlayground.main()
}