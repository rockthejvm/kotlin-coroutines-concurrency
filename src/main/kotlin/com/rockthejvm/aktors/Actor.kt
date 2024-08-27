package com.rockthejvm.aktors

import kotlinx.coroutines.channels.*
import org.slf4j.LoggerFactory

/*
    - name and channel args
    - start() method which pops elements off the channel and logs them
 */
internal class Actor<T>(private val name: String, private val channel: Channel<T>) {
    private val log = LoggerFactory.getLogger(this::class.java)

    suspend fun run() {
        while(true) {
            val msg = channel.receive() // semantically blocking
            log.info("[$name] $msg")
        }
    }
}

