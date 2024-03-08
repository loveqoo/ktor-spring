package com.example.module.user

import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Service

@Service
class UserService(
    private val db: R2dbcDatabase,
    private val repo: UserRepository
) {
    suspend fun save(
        form: UserCreationForm
    ): Either<Throwable, UserEntity> = form.get().flatMap { user ->
        Either.catch {
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
