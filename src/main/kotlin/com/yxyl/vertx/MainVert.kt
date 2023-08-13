package com.yxyl.vertx

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await

class MainVert : CoroutineVerticle() {
    var contentTypeJson = "application/json"

    companion object {
        init {
            val objectMapper = DatabindCodec.mapper()
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            objectMapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            objectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
            objectMapper.registerModule(JavaTimeModule())
            objectMapper.registerKotlinModule()
        }
    }

    override suspend fun start() {
        // TODO: to be continued

        val serverFuture = vertx.createHttpServer()
            .requestHandler(routes())
            .listen(8080)
        //挂起
        val server = serverFuture.await()
        println("HTTP server port: ${server.actualPort()}")
    }

    private fun routes(): Handler<HttpServerRequest>? {
        TODO("add the routers")
    }

    private fun createMysqlPool(vertx: Vertx) {
        // TODO create datasource pool
    }

}