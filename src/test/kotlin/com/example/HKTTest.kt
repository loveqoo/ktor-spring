package com.example

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import org.junit.Test

class HKTTest {
    @Test
    fun listTest() {
        val list = ListK(listOf(1, 2, 3))
        val result = ListFunctor.run {
            list.map { it * 2 }
        }
        println((result as ListK).unwrap())
    }

    @Test
    fun optionFunctorTest() {
        val option = OptionK(Some(5))
        val result = OptionFunctor.run {
            option.map { it * 3 }
        }
        println((result as OptionK).unwrap())
    }

    @Test
    fun optionMonadTest() {
        val option = OptionK(Some(5))
        val result = OptionMonad.run {
            option.flatMap {
                OptionK(Some(it * 3))
            }
        }
        println((result as OptionK).unwrap())
    }

    @Test
    fun optionCombineTest() {
        val opt1 = OptionK(Some(10))
        val opt2 = OptionK(Some(20))
        val result = combineOptions(opt1, opt2) { a, b -> a + b }
        println(result.unwrap())
    }
}


interface Kind<F, A>

class ListK<A>(
    private val list: List<A>
) : Kind<ListK.K, A> {
    object K

    fun unwrap(): List<A> = list
}

interface Functor<F> {
    fun <A, B> Kind<F, A>.map(fn: (A) -> B): Kind<F, B>
}

object ListFunctor : Functor<ListK.K> {
    override fun <A, B> Kind<ListK.K, A>.map(
        fn: (A) -> B
    ): Kind<ListK.K, B> {
        val unwrapped = (this as ListK<A>).unwrap()
        return ListK(unwrapped.map(fn))
    }
}

@JvmInline
value class OptionK<A>(
    private val option: Option<A>
) : Kind<OptionK.K, A> {
    object K

    fun unwrap(): Option<A> = option
}

object OptionFunctor : Functor<OptionK.K> {
    override fun <A, B> Kind<OptionK.K, A>.map(
        fn: (A) -> B
    ): Kind<OptionK.K, B> = when (val unwrapped = (this as OptionK<A>).unwrap()) {
        is Some -> OptionK(Some(fn(unwrapped.value)))
        is None -> OptionK(None)
    }
}

interface Monad<F> : Functor<F> {
    fun <A, B> Kind<F, A>.flatMap(fn: (A) -> Kind<F, B>): Kind<F, B>
}

object OptionMonad : Monad<OptionK.K> {
    override fun <A, B> Kind<OptionK.K, A>.map(
        fn: (A) -> B
    ): Kind<OptionK.K, B> = OptionFunctor.run {
        this@map.map(fn)
    }

    override fun <A, B> Kind<OptionK.K, A>.flatMap(
        fn: (A) -> Kind<OptionK.K, B>
    ): Kind<OptionK.K, B> = when (val unwrapped = (this as OptionK<A>).unwrap()) {
        is Some -> fn(unwrapped.value)
        is None -> OptionK(None)
    }
}

fun combineOptions(
    opt1: OptionK<Int>,
    opt2: OptionK<Int>,
    fn: (Int, Int) -> Int
): OptionK<Int> = OptionMonad.run { opt1.flatMap { a -> opt2.map { b -> fn(a, b) } } } as OptionK<Int>
