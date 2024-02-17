package com.example.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class HelloWorldService(@Qualifier("userList") val userList: List<String>) : SimpleService {
    override fun execute() = "Hello World ${userList.joinToString()}"
}
