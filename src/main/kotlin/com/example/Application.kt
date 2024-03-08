package com.example

import com.example.config.ServerConfig
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    ServerConfig.Spring.run {
        val projectPackage = environment.config.propertyOrNull("project.package")?.getString() ?: "com.example"
        config(projectPackage)
    }
    ServerConfig.Logging.run { config() }
    ServerConfig.Web.run { config() }
}
