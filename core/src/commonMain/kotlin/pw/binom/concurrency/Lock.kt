package pw.binom.concurrency

interface Lock {
    /**
     * Trying lock. Infinity time
     */
    fun lock()
    fun tryLock(): Boolean
    fun unlock()
}
