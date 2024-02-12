package com.example

import org.springframework.stereotype.Service

@Service
class HelloWorldService : SimpleService {
    override fun execute() = "Hello World Blah"
}
