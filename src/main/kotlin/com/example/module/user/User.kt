package com.example.module.user

import arrow.core.getOrElse
import arrow.core.raise.either
import com.example.config.ServerConfig.Spring.bean
import com.example.config.ServerConfig.Spring.respondError
import com.example.infrastructure.Extensions.ApplicationCallExtension.longParameter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.module() {
    routing {
        route("users") {
            post {
                either {
                    val userForm = call.receive<UserCreationForm>()
                    val service = call.bean<UserService>().bind()
                    service.save(userForm).bind()
                }.fold({
                    call.respondError(HttpStatusCode.BadRequest, it)
                }, {
                    call.respondText("Ok")
                })
            }
            get {
                either {
                    val service = call.bean<UserService>().bind()
                    service.findAll().bind()
                }.fold({
                    call.respondError(HttpStatusCode.BadRequest, it)
                }, {
                    call.respond(it)
                })
            }
            get("{id}") {
                either {
                    val id = call.longParameter("id").bind()
                    val service = call.bean<UserService>().bind()
                    service.findById(id).bind()
                }.fold({
                    call.respondError(HttpStatusCode.BadRequest, it)
                }, {
                    call.respond(it.getOrElse { "Nothing!!" })
                })
            }
        }
    }
}
