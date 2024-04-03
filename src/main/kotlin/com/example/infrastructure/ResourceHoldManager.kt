package com.example.infrastructure

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component
import java.util.*
import java.util.function.Supplier

object ResourceHoldManager {
    private val factoryMap = mutableMapOf<String, () -> WrappedResource<AutoCloseable>>()
    private val threadLocalMethodCall = ThreadLocal<Stack<MethodContext>>()

    fun registerFactory(
        name: String,
        factory: () -> WrappedResource<AutoCloseable>
    ) = factoryMap.putIfAbsent(name, factory)

    fun methodStartWith(resourceHold: ResourceHold) {
        val currentStack = threadLocalMethodCall.get()
        val factory = factoryMap[resourceHold.key]
        if (currentStack == null) {
            if (factory != null) {
                val newStack = Stack<MethodContext>()
                newStack.push(MethodContext(resourceHold, factory()))
                threadLocalMethodCall.set(newStack)
            }
        } else {
            if (resourceHold.requiresNew) {
                if (factory != null) {
                    currentStack.push(MethodContext(resourceHold, factory()))
                }
            } else {
                val prevMethodContext = currentStack.peek()
                if (prevMethodContext != null) {
                    currentStack.push(MethodContext(resourceHold, prevMethodContext.resource))
                }
            }
        }
    }

    fun methodEnd() {
        val currentStack = threadLocalMethodCall.get()
        if (currentStack != null) {
            val methodContext = currentStack.pop()
            if (methodContext != null && (currentStack.size == 0 || methodContext.annotation.requiresNew)) {
                methodContext.resource.close()
            }
        }
        if (currentStack.empty()) {
            threadLocalMethodCall.remove()
        }
    }
}

private data class MethodContext(
    val annotation: ResourceHold,
    val resource: WrappedResource<AutoCloseable>
)


@Aspect
@Component
class ResourceHoldAspect {
    @Pointcut("@annotation(resourceHold)")
    fun resourceHolderMethod(resourceHold: ResourceHold) {
    }

    @Around("resourceHolderMethod(resourceHold)")
    fun aroundHolderMethod(
        pjp: ProceedingJoinPoint,
        resourceHold: ResourceHold
    ): Any? {
        try {
            ResourceHoldManager.methodStartWith(resourceHold)
            return pjp.proceed()
        } finally {
            ResourceHoldManager.methodEnd()
        }
    }
}


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ResourceHold(val key: String, val requiresNew: Boolean = false)

abstract class WrappedResource<R>(
    private val factory: () -> R
) : Supplier<R> where R : AutoCloseable {
    private var resource: R? = null

    abstract fun key(): String

    abstract fun valid(r: R): Boolean

    fun close() = resource?.close()

    override fun get(): R {
        val currentResource = resource
        if (currentResource != null && valid(currentResource)) {
            return currentResource
        }

        synchronized(this) {
            val existingResource = resource
            if (existingResource != null && valid(existingResource)) {
                return existingResource
            }
            val newResource = factory()
            resource = newResource
            return newResource
        }
    }
}
