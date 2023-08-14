package com.yxyl.vertx

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.awaitEvent
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun main(args: Array<String>) {
    val vertx = Vertx.vertx()
    GlobalScope.launch(vertx.dispatcher()) {
        val timerId = awaitEvent<Long> { handler ->
            vertx.setTimer(1000, handler)
        }
        println("Event fired from timer with id $timerId")
        vertx.deployVerticle(MainVert()).await()
    }

}