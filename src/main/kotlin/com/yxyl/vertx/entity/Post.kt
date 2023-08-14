package com.yxyl.vertx.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

data class Post(
    var id: Int,
    var title: String,
    var content: String = "",
    @JsonIgnoreProperties(ignoreUnknown = true)
    var created_at: LocalDateTime = LocalDateTime.now()
) {
    constructor(title: String, content: String) : this(0, title, content)
}

data class CreatePostCommand(
    var title: String,
    var content: String
)