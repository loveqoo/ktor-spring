package com.example.service

import com.example.infrastructure.Extensions.MessageSourceExtension.message
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service

@Service
class HelloWorldService(
    val messageSource: MessageSource,
    @Qualifier("userList") val userList: List<String>
) : SimpleService {
    override fun execute(): String = messageSource.message(
        "greeting",
        arrayOf(userList.joinToString())
    )
}
