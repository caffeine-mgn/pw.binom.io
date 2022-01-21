package pw.binom.concurrency

import kotlin.time.Duration

/**
 * Objects in [BlockingExchangeOutput] will got as ObjectTree and attach
 */
interface BlockingExchangeInput<T> {
    fun get(duration: Duration): T?
    fun get(): T
}