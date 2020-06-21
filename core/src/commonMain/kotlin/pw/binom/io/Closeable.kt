package pw.binom.io

class ClosedException : RuntimeException()

interface Closeable {
    fun close()
}

fun Closeable(func: () -> Unit) = object : Closeable {
    override fun close() {
        func()
    }

}

fun closablesOf(vararg closable: Closeable) =
        Closeable {
            closable.forEach { it.close() }
        }

inline fun <T : Closeable, R> T.hold(func: () -> R): R {
    return try {
        func()
    } finally {
        close()
    }
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