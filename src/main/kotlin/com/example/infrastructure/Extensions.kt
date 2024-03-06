package com.example.infrastructure

import org.springframework.context.MessageSource
import java.util.*

object Extensions {

    object MessageSourceExtension {
        fun MessageSource.message(
            code: String,
            args: Array<out Any>,
            locale: Locale = Locale.getDefault()
        ) = getMessage(code, args, locale)
    }
}
