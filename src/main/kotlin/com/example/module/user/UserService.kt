package com.example.module.user

import arrow.core.Either
import arrow.core.Option
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.kotlin.circuitbreaker.executeSuspendFunction
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Service

@Service
class UserService(
    private val db: R2dbcDatabase,
    private val repo: UserRepository
) {
    private val cb: CircuitBreaker = CircuitBreaker.ofDefaults("")

    suspend fun save(
        user: UserEntity
    ): Either<Throwable, UserEntity> = Either.catch {
        cb.executeSuspendFunction {
            db.withTransaction {
                repo.save(user)
            }
        }
    }

    suspend fun findAll(): Either<Throwable, List<UserEntity>> = Either.catch {
        repo.findAll()
    }

    suspend fun findById(
        id: Long
    ): Either<Throwable, Option<UserEntity>> = Either.catch {
        repo.findById(id)
    }
}
