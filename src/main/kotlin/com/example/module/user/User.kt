package com.example.module.user

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.raise.either
import com.example.config.ServerConfig.Spring.beanWithFlatten
import com.example.config.ServerConfig.Spring.respondError
import com.example.infrastructure.Extensions.ApplicationCallExtension.longParameter
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.module() {
    routing {
        route("users") {
            post {
                either {
                    val userForm = call.receive<UserCreationForm>()
                    call.beanWithFlatten<UserService, UserEntity> { service ->
                        service.save(userForm)
                    }.bind()
                }.fold({
                    call.respondError(HttpStatusCode.BadRequest, it)
                }, {
                    call.respondText("Ok")
                })
            }
            get {
                either {
                    call.beanWithFlatten<UserService, List<UserEntity>> { service ->
                        service.findAll()
                    }.bind()
                }.fold({
                    call.respondError(HttpStatusCode.BadRequest, it)
                }, {
                    call.respond(it)
                })
            }
            get("{id}") {
                either {
                    val id = call.longParameter("id").bind()
                    call.beanWithFlatten<UserService, Option<UserEntity>> { service ->
                        service.findById(id)
                    }.bind()
                }.fold({
                    call.respondError(HttpStatusCode.BadRequest, it)
                }, {
                    call.respond(it.getOrElse { "Nothing!!" })
                })
            }
        }
    }
}
