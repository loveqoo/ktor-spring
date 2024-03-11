package com.example.infrastructure

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.either
import dev.nesk.akkurate.ValidationResult
import dev.nesk.akkurate.Validator
import dev.nesk.akkurate.constraints.constrain
import dev.nesk.akkurate.constraints.otherwise
import dev.nesk.akkurate.validatables.Validatable
import io.ktor.server.application.ApplicationCall
import org.springframework.context.MessageSource
import java.util.Locale

object Extensions {

    object MessageSourceExtension {
        fun MessageSource.message(
            code: String,
            args: Array<out Any> = arrayOf(),
            locale: Locale = Locale.getDefault()
        ) = Either.catch { getMessage(code, args, locale) }

        fun MessageSource.errorMessage(
            e: Throwable,
            defaultMessage: String = "Unknown Error"
        ): String =
            e.message ?: message("error.unknown").getOrElse { defaultMessage }
    }

    object ApplicationCallExtension {
        fun ApplicationCall.longParameter(
            paramName: String
        ): Either<Throwable, Long> = either {
            require(paramName.isNotEmpty()) {
                "parameter name is empty"
            }
            parameters[paramName]?.toLong() ?: error("$paramName is not defined or not a type of Long.")
        }
    }

    object ValidatableExtension {
        fun Validatable<String>.isEmail() = constrain {
            "^(.+)@(\\S+)$".toRegex().matches(it)
        } otherwise {
            "Invalid email format"
        }
    }

    object AkkurateValidatorExtensions {
        fun <F, T> Validator.Runner<F>.mapping(
            failure: (ValidationResult.Failure) -> Either<Throwable, Nothing> = { result ->
                IllegalArgumentException(
                    result.violations.joinToString(",") {
                        "${it.path}: ${it.message}"
                    }
                ).left()
            },
            resolve: (F) -> T
        ): (F) -> Either<Throwable, T> = { form: F ->
            when (val result = this@mapping(form)) {
                is ValidationResult.Success -> {
                    Either.catch { resolve(form) }
                }

                is ValidationResult.Failure -> failure(result)
            }
        }
    }
}
