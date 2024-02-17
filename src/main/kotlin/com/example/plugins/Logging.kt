package com.example.plugins

import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.logging.*
import io.ktor.util.pipeline.*
import org.slf4j.event.Level
import java.util.function.Supplier
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

private const val PHASE_NAME = "LoggingContentBody"

fun Application.configurableLogging(dsl: LogConfigListBuilder.() -> Unit = {}) {
    val logConfigList = LogConfigListBuilder().apply(dsl).get()

    val phase = PipelinePhase(PHASE_NAME)
    sendPipeline.insertPhaseBefore(ApplicationSendPipeline.Engine, phase)
    sendPipeline.intercept(phase) { response ->
        logConfigList.forEach { (filter, transform, logLevel, logger) ->
            val currentLogger: Logger = logger ?: application.log
            val filtered = runCatching { filter(call) }.getOrElse { ex ->
                currentLogger.error(ex)
                false
            }
            if (filtered) {
                when (response) {
                    is OutgoingContent.ByteArrayContent -> runCatching { transform(response.bytes()) }
                    is OutgoingContent.NoContent -> success("")
                    is OutgoingContent.ReadChannelContent -> success("")
                    is OutgoingContent.WriteChannelContent -> success("")
                    else -> failure(IllegalStateException("Not found any status in OutgoingContent at ${call.request.path()}"))
                }.fold({
                    currentLogger.logStringByLevel(logLevel)(it)
                }, {
                    currentLogger.error("Logging Error", it)
                })
                return@forEach
            }
        }
    }
}

private fun Logger.logStringByLevel(logLevel: Level): (String) -> Unit {
    return when (logLevel) {
        Level.INFO -> this::info
        Level.DEBUG -> this::debug
        Level.WARN -> this::warn
        Level.TRACE -> this::trace
        Level.ERROR -> this::error
    }
}

data class LogConfig(
    val filter: (ApplicationCall) -> Boolean,
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

class LogConfigListBuilder() : Supplier<List<LogConfig>> {
    private val logConfigList = mutableListOf<LogConfig>()
    fun config(builder: LogConfigBuilder.() -> Unit) = logConfigList.add(LogConfigBuilder().apply(builder).get())
    fun onPath(path: String) = logConfigList.add(LogConfigBuilder().also { builder ->
        builder.filter = {
            it.request.path() == path
        }
    }.get())

    override fun get(): List<LogConfig> = logConfigList
}
