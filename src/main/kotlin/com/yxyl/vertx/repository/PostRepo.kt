package com.yxyl.vertx.repository

import com.yxyl.vertx.entity.Post
import io.vertx.kotlin.coroutines.await
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple
import java.util.*
import java.util.stream.StreamSupport

class PostRepo(private val client: MySQLPool) {

    suspend fun findAll() = client.query("select * from posts order by id asc ")
        .execute()
        .map { rs ->
            StreamSupport.stream(rs.spliterator(), false)
                .map { mapFunc(it!!) }
                .toList()
        }.await()


    suspend fun findById(id: String?): Post? = client.preparedQuery("select * from posts where  id = ?")
        .execute(Tuple.of(id))
        .map { it.iterator() }
        .map { if (it.hasNext()) mapFunc(it.next()) else null }
        .await()

    suspend fun save(data: Post) =
        client.preparedQuery("insert into posts(title ,content ) values (?,?)")
            .execute(Tuple.of(data.title, data.content))
            .await()

    suspend fun saveAll(data: List<Post>): Int? =
        client.preparedQuery("insert into posts (title,content) values ?,? ")
            .execute(Tuple.tuple())
            .map { it.rowCount() }
            .await()

    suspend fun update(data: Post) =

        client.preparedQuery("update posts set title  = ?,content = ? where id = ?")
            .execute(Tuple.of(data.title, data.content, data.id))
            .map { it.rowCount() }
            .await()

    suspend fun deleteAll() =
        client.query("delete from posts")
            .execute()
            .map { it.rowCount() }
            .await()

    suspend fun deleteById(id: UUID) =
        client.preparedQuery("delete from posts where id = ?")
            .execute(Tuple.of(id))
            .map { it.rowCount() }
            .await()

    companion object {
        val mapFunc: (Row) -> Post = { row ->
            Post(
                row.getInteger("id"),
                row.getString("title"),
                row.getString("content"),
                row.getLocalDateTime("created_at")
            )
        }
    }
}
