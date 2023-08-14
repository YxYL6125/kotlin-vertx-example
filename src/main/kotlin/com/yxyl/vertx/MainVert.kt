package com.yxyl.vertx

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.yxyl.vertx.exception.PostNotFoundException
import com.yxyl.vertx.handler.PostHandler
import com.yxyl.vertx.repository.PostRepo
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.Json
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.awaitEvent
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import kotlinx.coroutines.launch


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
        val mysqlPool = createMysqlPool(vertx)
        val postRepo = PostRepo(mysqlPool)
        val postHandler = PostHandler(postRepo)

        val serverFuture = vertx.createHttpServer()
            .requestHandler(routes(postHandler))
            .listen(8080)
        //挂起 Start the server
        val server = serverFuture.await()
        println("HTTP server port: ${server.actualPort()}")
    }


    private fun routes(postHandlers: PostHandler): Handler<HttpServerRequest>? {
        val router = Router.router(vertx)

        val health = mapOf("name" to "Link")
        router.get("/").handler { it.response().end(Json.encode(health)) }

        router.get("/health").handler { r ->
            launch {
                awaitEvent<Long> { h -> vertx.setTimer(2000, h) }
                r.response().end(Json.encode(health)).await()
            }
        }
        router.get("/posts")
            .produces(contentTypeJson)
            .coroutineHandler { postHandlers.all(it) }
        router.post("/posts")
            .consumes(contentTypeJson)
            .produces(contentTypeJson)
            .handler(BodyHandler.create())
            .coroutineHandler { postHandlers.save(it) }
        router.get("/posts/:id")
            .produces(contentTypeJson)
            .coroutineHandler { postHandlers.getById(it) }
        router.post("/posts/:id")
            .consumes(contentTypeJson)
            .produces(contentTypeJson)
            .handler(BodyHandler.create())
            .coroutineHandler { postHandlers.update(it) }

        router.route().failureHandler { r ->
            if (r.failure() is PostNotFoundException)
                r.response().setStatusCode(404)
                    .end(json {
                        obj("message" to "${r.failure().message}", "code" to "not found")
                            .toString()
                    })
        }

        return router
    }

    /**
     * PG
     */
    private fun createPglPool(vertx: Vertx): PgPool {
        val opt = PgConnectOptions()
        opt.host = "127.0.0.1"
        opt.user = "postgres"
        opt.password = "6125"
        opt.database = "postgres"
        val poolOptions = PoolOptions()
        poolOptions.maxSize = 10
        return PgPool.pool(vertx, opt, poolOptions)
    }

    /**
     * Mysql
     */
    private fun createMysqlPool(vertx: Vertx): MySQLPool {
        val connectOptions = MySQLConnectOptions()
            .setPort(3306)
            .setHost("127.0.0.1")
            .setDatabase("blog")
            .setUser("root")
            .setPassword("6125")
        // 连接池选项
        val poolOptions = PoolOptions().setMaxSize(10)
        // 创建带连接池的客户端
        val client = MySQLPool.pool(vertx, connectOptions, poolOptions)

        return client
    }

}