package com.example.config

import com.example.domain.user
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource

@Configuration
class SpringConfig {

    @Bean
    fun messageSource() = ResourceBundleMessageSource().apply {
        setBasename("messages")
        setDefaultEncoding("UTF-8")
    }

    @Bean
    fun userList(): List<String> = listOf("Anthony", "Scully")

//    @Bean(destroyMethod = "stop")
//    fun h2Server(): Server {
//        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-ifNotExists").start()
//    }

    @Bean
    //@DependsOn("h2Server")
    fun database(): R2dbcDatabase = R2dbcDatabase("r2dbc:h2:mem:///example;DB_CLOSE_DELAY=-1").also { db ->
        runBlocking {
            delay(1000L)
            db.withTransaction {
                db.runQuery {
                    QueryDsl.create(Meta.user)
                }
            }
        }
    }
}
