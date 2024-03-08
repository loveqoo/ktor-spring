package com.example.module.user

import arrow.core.Option
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.single
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    private val db: R2dbcDatabase
) {
    private val user = Meta.userEntity

    suspend fun findById(id: Long): Option<UserEntity> = Option.catch {
        db.runQuery {
            QueryDsl.from(user).where {
                user.id eq id
            }.single()
        }
    }

    suspend fun findAll(): List<UserEntity> = db.runQuery {
        QueryDsl.from(user)
    }

    suspend fun save(newUser: UserEntity): UserEntity = db.runQuery {
        QueryDsl.insert(user).single(newUser)
    }
}
