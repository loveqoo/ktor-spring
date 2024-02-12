package com.example.plugins

import com.example.SimpleService
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Application.configureRouting() {
    install(Resources)
    routing {
        application.environment
        get("/") {
            call.respondText("Hello World!")
        }
        get<Articles> { article ->
            // Get all articles ...
            call.respond("List of articles sorted starting from ${article.sort}")
        }
        get("/hello") {
            val service: SimpleService = call.bean()
            call.respondText(service.execute())
        }
    }
}

@Serializable
@Resource("/articles")
class Articles(val sort: String? = "new")
