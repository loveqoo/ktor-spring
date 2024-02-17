package com.example.plugins

import io.ktor.server.application.*
import io.ktor.util.*
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.util.function.Supplier

private val pluginKey = AttributeKey<ApplicationContext>("SpringApplicationContext")

class SpringDependencyInjection {

    class SpringConfiguration : Supplier<AnnotationConfigApplicationContext> {
        var basePackages = emptyArray<String>()
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

inline fun <reified T> ApplicationCall.bean(): T = springApplicationContext.getBean(T::class.java)

@Throws(IllegalStateException::class)
fun Application.configureSpring(vararg packages: String) {
    check(packages.isNotEmpty()) {
        "base packages must set"
    }
    install(SpringDependencyInjection) {
        basePackages = arrayOf(*packages)
        initialize()
    }
}

fun Application.configureSpring(configuration: SpringDependencyInjection.SpringConfiguration.() -> Unit) {
    install(SpringDependencyInjection) {
        configuration()
    }
}
