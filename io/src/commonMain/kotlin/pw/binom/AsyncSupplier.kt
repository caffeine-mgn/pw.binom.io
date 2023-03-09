package pw.binom

fun interface AsyncSupplier<T> {
    @Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION")
    suspend fun get(): T
}

fun <T, R> AsyncSupplier<T>.map(func: suspend (T) -> R) = AsyncSupplier {
    func(get())
}

suspend fun <T> AsyncSupplier<T>.takeIf(func: (T) -> Boolean): T? {
    val r = get()
    return if (func(r)) {
        r
    } else {
        null
    }
}

fun <T> AsyncSupplier<T>.oneShot() = object : AsyncSupplier<T> {
    private var result: T? = null
    private var done = false

    override suspend fun get(): T {
        if (!done) {
            result = this@oneShot.get()
            done = true
        }
        return result as T
    }
}
