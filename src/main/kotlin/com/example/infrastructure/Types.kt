package com.example.infrastructure

import arrow.core.Either
import java.util.function.Supplier

typealias EitherSupplier<T> = Supplier<Either<Throwable, T>>
