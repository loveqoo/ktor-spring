package com.example.module.user

import arrow.core.getOrElse
import arrow.core.raise.either
import com.example.config.ServerConfig.Spring.bean
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
                    val service = call.bean<UserService>().bind()
                    val user = UserCreationForm.checkAndGet(userForm).bind()
                    service.save(user).bind()
                }.fold({ ex ->
                    call.respondError(HttpStatusCode.BadRequest, ex)
                }, {
                    call.respondText("Ok")
                })
            }
            get {
                either {
                    val service = call.bean<UserService>().bind()
                    service.findAll().bind()
                }.fold({ ex ->
                    call.respondError(HttpStatusCode.BadRequest, ex)
                }, { list ->
                    call.respond(list)
                })
            }
            get("{id}") {
                either {
                    val id = call.longParameter("id").bind()
                    val service = call.bean<UserService>().bind()
                    service.findById(id).bind()
                }.fold({ ex ->
                    call.respondError(HttpStatusCode.BadRequest, ex)
                }, { userOpt ->
                    call.respond(userOpt.getOrElse { "Nothing!!" })
                })
            }
        }
    }
}
