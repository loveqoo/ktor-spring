package com.example.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpringConfig {
    @Bean
    fun userList(): List<String> = listOf("Anthony", "Scully")
}