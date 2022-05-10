package pw.binom.concurrency

/**
 * Objects in [BlockingExchangeOutput] will add as ObjectTree
 */
interface BlockingExchangeOutput<T : Any?> {
    fun put(value: T)
}
