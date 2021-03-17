package pw.binom.io

class ClosedException : RuntimeException()

fun interface Closeable {
    fun close()
}

fun closablesOf(vararg closable: Closeable) =
    Closeable {
        closable.forEach { it.close() }
    }

inline fun <T : Closeable, R> T.use(func: (T) -> R): R {
    return try {
        func(this)
    } finally {
        close()
    }
}

interface AsyncCloseable {
    suspend fun asyncClose()
}

fun AsyncCloseable(func: suspend () -> Unit) = object : AsyncCloseable {
    override suspend fun asyncClose() {
        func()
    }
}

suspend inline fun <T : AsyncCloseable, R> T.use(func: (T) -> R): R =
    try {
        func(this)
    } finally {
        asyncClose()
    }