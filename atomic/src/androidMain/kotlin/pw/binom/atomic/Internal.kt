package pw.binom.atomic

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

actual typealias InternalAtomicBoolean = AtomicBoolean
actual typealias InternalAtomicInt = AtomicInteger
actual typealias InternalAtomicLong = AtomicLong
actual typealias InternalAtomicReference<T> = AtomicReference<T>
