package com.example.plugins

import com.example.plugins.validation.accessors.sort
import com.example.service.SimpleService
import dev.nesk.akkurate.ValidationResult
import dev.nesk.akkurate.Validator
import dev.nesk.akkurate.annotations.Validate
import dev.nesk.akkurate.constraints.builders.hasLengthGreaterThan
import dev.nesk.akkurate.constraints.builders.isNotEmpty
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.serialization.Serializable


fun Application.configureRouting() {
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }
    install(Resources)
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
        // ...
    }
    install(ContentNegotiation) {
        json()
    }
    configurableLogging {
        onPath("/hello")
    }
    routing {
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
                val service: SimpleService = call.bean()
                call.respondText(service.execute())
            }
        }
        swaggerUI(path = "openapi")
        get<Articles> { article ->
            // Get all articles ...

            when (val result = Articles.Companion.Validators.DEFAULT(article)) {
                is ValidationResult.Success ->
                    call.respond("List of articles sorted starting from ${article.sort}")

                is ValidationResult.Failure -> {
                    val msg = result.violations.joinToString(",") {
                        "${it.path}: ${it.message}"
                    }
                    call.respond(HttpStatusCode.BadRequest, msg)
                }
            }
        }
    }
}

@Serializable
@Validate
@Resource("/articles")
class Articles(val sort: String? = "new") {
    companion object {
        object Validators {
            val DEFAULT = Validator<Articles> {
                sort.isNotEmpty()
                sort.hasLengthGreaterThan(2)
            }
        }
    }
}
