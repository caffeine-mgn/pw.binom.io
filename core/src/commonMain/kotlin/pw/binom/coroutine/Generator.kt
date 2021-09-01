package pw.binom.coroutine

/**
 * Coroutine primitive.
 */
interface Generator<T> {
    val isFinished: Boolean

    /**
     * Should return one value from some values sequences
     * @throws NoSuchElementException when generator have no data for return
     */
    suspend fun next(): T

    suspend fun forEach(func: (T) -> Unit) {
        while (!isFinished) {
            func(next())
        }
    }

    /**
     * Force finish generator
     */
    suspend fun finish() {
        while (!isFinished) {
            next()
        }
    }
}