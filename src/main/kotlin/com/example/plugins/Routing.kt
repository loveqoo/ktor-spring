package com.example.plugins

import arrow.core.getOrElse
import arrow.core.raise.either
import com.example.domain.UserForm
import com.example.domain.UserService
import com.example.service.SimpleService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Application.configureRouting() {
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
        // ...
    }
    install(ContentNegotiation) {
        json()
    }
    configLogging()

    install(Resources)
    routing {
        swaggerUI(path = "openapi")
        get {
            call.respondText("Hello World!")
        }
        route("metrics-micrometer") {
            get {
                call.respond(appMicrometerRegistry.scrape())
            }
        }
        route("kotlinx-serialization") {
            get {
                call.respond(mapOf("hello" to "world"))
            }
        }
        route("hello") {
            get {
                either {
                    val service = call.bean<SimpleService>().bind()
                    service.execute()
                }.fold({
                    call.respond(HttpStatusCode.BadRequest, it.message ?: "unknown error")
                }, {
                    call.respondText(it)
                })
            }
        }
        post("users") {
            either {
                val userForm = call.receive<UserForm>()
                val service = call.bean<UserService>().bind()
                service.save(userForm).bind()
            }.fold({
                call.respond(HttpStatusCode.BadRequest, it.message ?: "unknown error")
            }, {
                call.respondText("Ok")
            })
        }
        get("users") {
            either {
                val service = call.bean<UserService>().bind()
                service.findAll().bind()
            }.fold({
                call.respond(HttpStatusCode.BadRequest, it.message ?: "unknown error")
            }, {
                call.respond(it)
            })
        }
        get("users/{id}") {
            either {
                val id = call.parameters["id"]?.toLong() ?: error("Invalid Id")
                val service = call.bean<UserService>().bind()
                service.findById(id).bind()
            }.fold({
                call.respond(HttpStatusCode.NotFound, it.message ?: "unknown error")
            }, {
                call.respond(it.getOrElse { "Nothing!!" })
            })
        }
    }
}
