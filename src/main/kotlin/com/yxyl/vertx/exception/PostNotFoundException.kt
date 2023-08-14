package com.yxyl.vertx.exception

class PostNotFoundException(id: String?) : RuntimeException ("Post id :$id was not found ~.~")
