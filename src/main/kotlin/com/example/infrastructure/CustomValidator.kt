package com.example.infrastructure

import dev.nesk.akkurate.constraints.constrain
import dev.nesk.akkurate.constraints.otherwise
import dev.nesk.akkurate.validatables.Validatable

object CustomValidator {
    fun Validatable<String>.isEmail() = constrain {
        "^(.+)@(\\S+)$".toRegex().matches(it)
    } otherwise {
        "Invalid email format"
    }
}
