package com.example.domain

import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import arrow.core.left
import com.example.domain.validation.accessors.email
import com.example.domain.validation.accessors.name
import com.example.domain.validation.accessors.password
import com.example.infrastructure.CustomValidator.isEmail
import dev.nesk.akkurate.ValidationResult
import dev.nesk.akkurate.Validator
import dev.nesk.akkurate.annotations.Validate
import dev.nesk.akkurate.constraints.builders.hasLengthBetween
import dev.nesk.akkurate.constraints.builders.hasLengthEqualTo
import dev.nesk.akkurate.constraints.builders.isContaining
import dev.nesk.akkurate.constraints.builders.isNotEmpty
import io.ktor.resources.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.komapper.annotation.*
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.single
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.function.Supplier

@Serializable
@KomapperEntity
@KomapperTable("APP_USER")
data class User(
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

@Repository
class UserRepository(
    private val database: R2dbcDatabase
) {
    private val user = Meta.user

    suspend fun findById(id: Long): Option<User> = Option.catch {
        database.runQuery {
            QueryDsl.from(user).where {
                user.id eq id
            }.single()
        }
    }

    suspend fun findAll(): List<User> = database.runQuery {
        QueryDsl.from(user)
    }

    suspend fun save(newUser: User): User = database.runQuery {
        QueryDsl.insert(user).single(newUser)
    }
}

@Service
class UserService(
    private val database: R2dbcDatabase,
    private val userRepository: UserRepository
) {
    suspend fun save(
        form: UserForm
    ): Either<Throwable, User> = form.get().flatMap { user ->
        Either.catch {
            database.withTransaction {
                userRepository.save(user)
            }
        }
    }

    suspend fun findAll(): Either<Throwable, List<User>> = Either.catch {
        userRepository.findAll()
    }

    suspend fun findById(
        id: Long
    ): Either<Throwable, Option<User>> = Either.catch {
        userRepository.findById(id)
    }
}

@Resource("/users")
@Validate
class UserForm(
    val name: String = "",
    val email: String = "",
    val password: String = ""
) : Supplier<Either<Throwable, User>> {

    @Resource("{id}")
    data class Id(val parent: UserForm = UserForm(), val id: Long)

    override fun get(): Either<Throwable, User> = when (val result = validator(this)) {
        is ValidationResult.Success -> {
            Either.catch {
                User(
                    name = name,
                    email = email,
                    password = password
                )
            }
        }

        is ValidationResult.Failure -> {
            IllegalArgumentException(result.violations.joinToString(",") {
                "${it.path}: ${it.message}"
            }).left()
        }
    }

    companion object {
        val validator = Validator<UserForm> {
            name {
                isNotEmpty()
                hasLengthBetween(3..255)
            }
            email {
                isNotEmpty()
                isContaining("@")
                isEmail()
            }
            password {
                isNotEmpty()
                hasLengthEqualTo(32)
            }
        }
    }

}

