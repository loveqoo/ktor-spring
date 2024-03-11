package com.example.config

import arrow.core.Either
import arrow.core.getOrElse
import com.example.config.ServerConfig.Spring.messageSource
import com.example.infrastructure.Extensions.MessageSourceExtension.errorMessage
import com.example.infrastructure.Extensions.MessageSourceExtension.message
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.basic
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.path
import io.ktor.server.resources.Resources
import io.ktor.server.response.ApplicationSendPipeline
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.util.AttributeKey
import io.ktor.util.logging.Logger
import io.ktor.util.logging.error
import io.ktor.util.pipeline.PipelinePhase
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.slf4j.event.Level
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext
import org.springframework.context.MessageSource
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.util.function.Supplier

object ServerConfig {

    object Web {
        fun Application.config() {
            install(DefaultHeaders) {
                header("X-Engine", "Ktor") // will send this header with each response
            }
            install(ContentNegotiation) {
                json()
            }
            install(Authentication) {
                basic("auth") {
                    realm = "Need to Authenticate"
                    validate { credentials ->
                        if (credentials.name == credentials.password) {
                            UserIdPrincipal(credentials.name)
                        } else {
                            null
                        }
                    }
                }
            }
            val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

            install(MicrometerMetrics) {
                registry = appMicrometerRegistry
                // ...
            }
            install(Resources)
            routing {
                swaggerUI(path = "openapi")
                route("actuator") {
                    authenticate("auth") {
                        get("prometheus") {
                            call.respond(appMicrometerRegistry.scrape())
                        }
                    }
                }
                get {
                    call.respondText(call.messageSource.message("greeting", arrayOf("스트레인저")).getOrElse { "" })
                }
            }
        }
    }

    object Logging {
        private const val PHASE_NAME = "LoggingContentBody"

        fun ApplicationCallPipeline.config(
            dsl: LogConfigBuilder.() -> Unit = {}
        ) {
            val (ignore, transform, logLevel, logger) = LogConfigBuilder().apply(dsl).get()

            val phase = PipelinePhase(PHASE_NAME)
            sendPipeline.insertPhaseBefore(ApplicationSendPipeline.Engine, phase)
            sendPipeline.intercept(phase) { response ->
                val currentLogger: Logger = logger ?: application.log
                val ignored = runCatching { ignore(call) }.getOrElse { ex ->
                    currentLogger.error(ex)
                    false
                }
                if (!ignored) {
                    when (response) {
                        is OutgoingContent.ByteArrayContent -> runCatching { transform(response.bytes()) }
                        is OutgoingContent.NoContent -> Result.success("")
                        is OutgoingContent.ReadChannelContent -> Result.success("")
                        is OutgoingContent.WriteChannelContent -> Result.success("")
                        else -> Result.failure(
                            IllegalStateException("Not found any status in OutgoingContent at ${call.request.path()}")
                        )
                    }.fold({
                        currentLogger.logStringByLevel(logLevel)(it)
                    }, {
                        currentLogger.error("Logging Error", it)
                    })
                }
            }
        }

        private fun Logger.logStringByLevel(
            logLevel: Level
        ): (String) -> Unit = when (logLevel) {
            Level.INFO -> this::info
            Level.DEBUG -> this::debug
            Level.WARN -> this::warn
            Level.TRACE -> this::trace
            Level.ERROR -> this::error
        }

        data class LogConfig(
            val ignore: (ApplicationCall) -> Boolean,
            val transform: (ByteArray) -> String = ::String,
            val logLevel: Level = Level.INFO,
            val logger: Logger?
        )

        class LogConfigBuilder : Supplier<LogConfig> {
            var filter: (ApplicationCall) -> Boolean = { false }
            var transform: (ByteArray) -> String = ::String
            var logLevel: Level = Level.INFO
            var logger: Logger? = null
            override fun get(): LogConfig = LogConfig(filter, transform, logLevel, logger)
        }
    }

    object Spring {
        private val pluginKey = AttributeKey<ApplicationContext>("SpringApplicationContext")

        class SpringDependencyInjection {

            class SpringConfiguration : Supplier<AnnotationConfigApplicationContext> {
                var basePackages = emptyArray<String>()

                @Suppress("SpreadOperator")
                private val ctx = lazy {
                    AnnotationConfigApplicationContext(*basePackages)
                }

                fun initialize() = ctx.value

                override fun get(): AnnotationConfigApplicationContext = ctx.value
            }

            companion object Plugin :
                BaseApplicationPlugin<ApplicationCallPipeline, SpringConfiguration, SpringDependencyInjection> {
                override val key = AttributeKey<SpringDependencyInjection>("Setup a Spring's ApplicationContext")
                override fun install(
                    pipeline: ApplicationCallPipeline,
                    configure: SpringConfiguration.() -> Unit
                ): SpringDependencyInjection = SpringDependencyInjection().also {
                    val configuration = SpringConfiguration().apply(configure)
                    pipeline.intercept(ApplicationCallPipeline.Plugins) {
                        call.attributes.put(pluginKey, configuration.get())
                    }
                }
            }
        }

        val ApplicationCall.springApplicationContext get() = attributes[pluginKey]
        val ApplicationCall.messageSource
            get() = bean<MessageSource>().getOrNull() ?: throw NoSuchBeanDefinitionException(MessageSource::class.java)

        inline fun <reified T> ApplicationCall.bean(): Either<Throwable, T> = Either.catch {
            springApplicationContext.getBean(T::class.java)
        }

        suspend fun ApplicationCall.respondError(
            statusCode: HttpStatusCode,
            e: Throwable,
            defaultMessage: String = "Unknown Error"
        ) {
            this.respond(statusCode, this.messageSource.errorMessage(e, defaultMessage))
        }

        @Throws(IllegalStateException::class)
        fun Application.config(vararg packages: String) {
            check(packages.isNotEmpty()) {
                "base packages must set"
            }
            install(SpringDependencyInjection) {
                basePackages = arrayOf(*packages)
                initialize()
            }
        }

        fun Application.config(configuration: SpringDependencyInjection.SpringConfiguration.() -> Unit) {
            install(SpringDependencyInjection) {
                configuration()
            }
        }
    }
}
