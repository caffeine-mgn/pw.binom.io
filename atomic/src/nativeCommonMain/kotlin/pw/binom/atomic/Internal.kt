package pw.binom.atomic

actual typealias InternalAtomicBoolean = kotlin.native.concurrent.AtomicInt
actual typealias InternalAtomicInt = kotlin.native.concurrent.AtomicInt
actual typealias InternalAtomicLong = kotlin.native.concurrent.AtomicLong
actual typealias InternalAtomicReference<T> = kotlin.native.concurrent.AtomicReference<T>
