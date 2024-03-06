package com.example.infrastructure

//import io.ktor.util.logging.*
//import org.jetbrains.exposed.dao.*
//import org.jetbrains.exposed.dao.id.EntityID
//import org.jetbrains.exposed.dao.id.LongIdTable
//import org.jetbrains.exposed.sql.javatime.datetime
//import org.slf4j.LoggerFactory
//import java.time.LocalDateTime
//
//object EntityBase {
//    private val logger = LoggerFactory.getLogger(this::class.java)
//
//    abstract class BaseLongIdTable(name: String, idColumnName: String = "id") : LongIdTable(name, idColumnName) {
//        val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
//        val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
//    }
//
//    abstract class BaseEntity(id: EntityID<Long>, table: BaseLongIdTable) : LongEntity(id) {
//        val createdAt by table.createdAt
//        var updatedAt by table.updatedAt
//    }
//
//    abstract class BaseEntityClass<E : BaseEntity>(table: BaseLongIdTable) : LongEntityClass<E>(table) {
//        init {
//            EntityHook.subscribe { entityChange ->
//                if (entityChange.changeType == EntityChangeType.Updated) {
//                    runCatching {
//                        entityChange.toEntity(this)?.updatedAt = LocalDateTime.now()
//                    }.getOrElse(logger::error)
//                }
//            }
//        }
//    }
//}
