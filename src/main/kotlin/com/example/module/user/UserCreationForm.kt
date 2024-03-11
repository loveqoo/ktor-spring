package com.example.module.user

import com.example.infrastructure.Extensions.AkkurateValidatorExtensions.mapping
import com.example.infrastructure.Extensions.ValidatableExtension.isEmail
import com.example.module.user.validation.accessors.email
import com.example.module.user.validation.accessors.name
import com.example.module.user.validation.accessors.password
import dev.nesk.akkurate.Validator
import dev.nesk.akkurate.annotations.Validate
import dev.nesk.akkurate.constraints.builders.hasLengthBetween
import dev.nesk.akkurate.constraints.builders.hasLengthEqualTo
import dev.nesk.akkurate.constraints.builders.isContaining
import dev.nesk.akkurate.constraints.builders.isNotEmpty

@Validate
class UserCreationForm(
    val name: String = "",
    val email: String = "",
    val password: String = ""
) {

    companion object {
        val checkAndGet = Validator<UserCreationForm> {
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
        }.mapping { form ->
            UserEntity(
                name = form.name,
                email = form.email,
                password = form.password
            )
        }
    }
}
