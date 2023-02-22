package pw.binom.concurrency

interface Lock {
    /**
     * Trying lock. Infinity time
     */
    fun lock(name: String? = null)
    fun tryLock(name: String? = null): Boolean
    fun unlock()
}
