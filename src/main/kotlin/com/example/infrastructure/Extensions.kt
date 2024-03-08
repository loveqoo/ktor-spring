package com.example.infrastructure

import arrow.core.Either
import arrow.core.getOrElse
import org.springframework.context.MessageSource
import java.util.*

object Extensions {

    object MessageSourceExtension {
        fun MessageSource.message(
            code: String,
            args: Array<out Any> = arrayOf(),
            locale: Locale = Locale.getDefault()
        ) = Either.catch { getMessage(code, args, locale) }

        fun MessageSource.errorMessage(e: Throwable): String = e.message ?: message("error.unknown").getOrElse { "Unknown Error" }
    }
}
