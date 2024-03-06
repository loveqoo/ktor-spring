package com.example.entity

//import org.jetbrains.exposed.dao.LongEntity
//import org.jetbrains.exposed.dao.LongEntityClass
//import org.jetbrains.exposed.dao.id.EntityID
//import org.jetbrains.exposed.dao.id.LongIdTable
//import org.jetbrains.exposed.sql.javatime.datetime
//import java.time.LocalDateTime
//
//object Authors : LongIdTable("author") {
//    val name = varchar("name", 100)
//    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
//}
//
//class Author(id: EntityID<Long>) : LongEntity(id) {
//    companion object : LongEntityClass<Author>(Authors)
//
//    var name by Authors.name
//    var createdAt by Authors.createdAt
//}
