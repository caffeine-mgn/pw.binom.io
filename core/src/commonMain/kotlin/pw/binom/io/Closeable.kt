package pw.binom.io

class ClosedException : RuntimeException()

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

interface AsyncCloseable {
    suspend fun close()
}

suspend inline fun <T : AsyncCloseable, R> T.use(func: (T) -> R): R {
    return try {
        func(this)
    } finally {
        close()
    }
}