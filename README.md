# kotlin-vertx-example
kotlin vertx-web Coroutine examples

![Static Badge](https://img.shields.io/badge/vert.x-3.7.0-purple.svg)




---

### Init the database

```sql
CREATE TABLE blog.posts (
    id int  AUTO_INCREMENT PRIMARY KEY ,
    title VARCHAR(255),
    content text,
    created_at TIMESTAMP 
);

```

### To run your application:

```groovy
./gradlew clean run
```

