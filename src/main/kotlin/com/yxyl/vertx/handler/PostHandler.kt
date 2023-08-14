package com.yxyl.vertx.handler

import com.yxyl.vertx.entity.CreatePostCommand
import com.yxyl.vertx.entity.Post
import com.yxyl.vertx.exception.PostNotFoundException
import com.yxyl.vertx.repository.PostRepo
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.await
import java.util.logging.Level
import java.util.logging.Logger

class PostHandler(val posts: PostRepo) {

    suspend fun all(rc: RoutingContext) {
        val data = posts.findAll()
        rc.response().end(Json.encode(data)).await()
    }

    suspend fun getById(rc: RoutingContext) {
        val params = rc.pathParams()
        val id = params["id"]
        val date = posts.findById(id)
        if (date != null) {
            rc.response().end(Json.encode(date))
        } else {
            rc.fail(404, PostNotFoundException(id))
        }
    }

    suspend fun save(rc: RoutingContext) {
        val body = rc.body().asJsonObject()
        // 如果要转成数组的话使用asJsonArray()
        LOGGER.info("Got body ${body}")
        val (title, content) = body.mapTo(CreatePostCommand::class.java)
        LOGGER.info("Got data ${title} & ${content}")
        var saveId = posts.save(Post(title = title, content = content))
        rc.response()
            .putHeader("Location", "/posts/$saveId")
            .setStatusCode(201)
            .end()
            .await()
    }

    suspend fun update(rc: RoutingContext) {
        val params = rc.pathParams()
        val id = params["id"]
        val body = rc.body().asJsonObject()
        LOGGER.log(Level.INFO, "\npath param id: {0}\nrequest body: {1}", arrayOf(id, body))
        var (title, content) = body.mapTo(CreatePostCommand::class.java)
        val existing = posts.findById(id)
        if (existing != null) {
            val data = existing.apply {
                this.title = title
                this.content = content
            }
            posts.update(data)
            rc.response().setStatusCode(204).end().await()
        } else {
            rc.fail(404, PostNotFoundException(id))
        }
    }


    suspend fun delete(rc: RoutingContext) {
        val params = rc.pathParams()
        val id = params["id"]
        val existing = posts.findById(id)
        if (existing != null) {
            rc.response().setStatusCode(204).end().await()
        } else {
            rc.fail(404, PostNotFoundException(id))
        }
    }


    companion object {
        val LOGGER = Logger.getLogger(PostHandler::class.java.simpleName)
    }

}
