package pw.binom.concurrency

import kotlin.time.Duration
import kotlin.time.ExperimentalTime

interface ExchangeInput<T> {
    @OptIn(ExperimentalTime::class)
    fun get(duration: Duration): T?
    fun get(): T
}