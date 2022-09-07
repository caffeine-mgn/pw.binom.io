package pw.binom

fun interface Supplier<T> {
    fun get(): T
}

fun <T, R> Supplier<T>.map(func: (T) -> R) = Supplier {
    func(get())
}

fun <T> Supplier<T>.takeIf(func: (T) -> Boolean): T? {
    val r = get()
    return if (func(r)) {
        r
    } else {
        null
    }
}

fun <T> Supplier<T>.oneShot() = object : Supplier<T> {
    private var result: T? = null
    private var done = false

    override fun get(): T {
        if (!done) {
            result = this@oneShot.get()
            done = true
        }
        return result as T
    }
}
