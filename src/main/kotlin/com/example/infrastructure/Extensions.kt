package com.example.infrastructure

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import io.ktor.server.application.*
import org.springframework.context.MessageSource
import java.util.*

object Extensions {

    object MessageSourceExtension {
        fun MessageSource.message(
            code: String,
            args: Array<out Any> = arrayOf(),
            locale: Locale = Locale.getDefault()
        ) = Either.catch { getMessage(code, args, locale) }

        fun MessageSource.errorMessage(e: Throwable): String =
            e.message ?: message("error.unknown").getOrElse { "Unknown Error" }
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
}
