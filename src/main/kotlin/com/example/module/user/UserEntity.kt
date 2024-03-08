package com.example.module.user

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import org.komapper.annotation.*

@Serializable
@KomapperEntity
@KomapperTable("APP_USER")
data class UserEntity(
    @KomapperId @KomapperAutoIncrement
    val id: Long = 0L,
    val name: String,
    val email: String,
    val password: String,
    @KomapperCreatedAt
    val createdAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    @KomapperUpdatedAt
    val updatedAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
)
