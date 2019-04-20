package pw.binom.io

interface Closeable {
    fun close()
}

inline fun <T : Closeable, R> T.use(func: (T) -> R): R {
    return try {
        func(this)
    } finally {
        close()
    }
}