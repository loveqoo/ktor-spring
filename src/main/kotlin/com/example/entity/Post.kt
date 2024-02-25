package com.example.entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Posts : LongIdTable("post") {
    val title = varchar("title", 255)
    val content = text("content")
    val author = reference("author", Authors)
    val status = enumerationByName("status", 10, PostStatus::class)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

enum class PostStatus {
    DRAFT, RELEASE, DROP
}

class Post(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Post>(Posts)

    var title by Posts.title
    var content by Posts.content
    var author by Author referencedOn Posts.author
    var status by Posts.status
    var createdAt by Posts.createdAt

    fun description() = "$title by $author"
}
