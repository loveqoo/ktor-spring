package com.example.module.user

import arrow.core.Either
import arrow.core.left
import com.example.infrastructure.CustomValidator.isEmail
import com.example.module.user.validation.accessors.email
import com.example.module.user.validation.accessors.name
import com.example.module.user.validation.accessors.password
import dev.nesk.akkurate.ValidationResult
import dev.nesk.akkurate.Validator
import dev.nesk.akkurate.annotations.Validate
import dev.nesk.akkurate.constraints.builders.hasLengthBetween
import dev.nesk.akkurate.constraints.builders.hasLengthEqualTo
import dev.nesk.akkurate.constraints.builders.isContaining
import dev.nesk.akkurate.constraints.builders.isNotEmpty
import io.ktor.resources.*
import java.util.function.Supplier

@Resource("/users")
@Validate
class UserCreationForm(
    val name: String = "",
    val email: String = "",
    val password: String = ""
) : Supplier<Either<Throwable, UserEntity>> {

    override fun get(): Either<Throwable, UserEntity> = when (val result = validator(this)) {
        is ValidationResult.Success -> {
            Either.catch {
                UserEntity(
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
        val validator = Validator<UserCreationForm> {
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
