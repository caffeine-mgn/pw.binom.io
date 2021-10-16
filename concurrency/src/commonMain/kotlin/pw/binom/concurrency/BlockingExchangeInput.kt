package pw.binom.concurrency

import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * Objects in [BlockingExchangeOutput] will got as ObjectTree and attach
 */
interface BlockingExchangeInput<T> {
    @OptIn(ExperimentalTime::class)
    fun get(duration: Duration): T?
    fun get(): T
}