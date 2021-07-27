package pw.binom.io

class ClosedException : RuntimeException {
    constructor()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}

fun interface Closeable {
    fun close()
}

fun closablesOf(vararg closable: Closeable) =
    Closeable {
        closable.forEach { it.close() }
    }

inline fun <T : Closeable, R> T.use(func: (T) -> R): R {

    val result = try {
        func(this)
    } catch (funcException: Throwable) {
        try {
            close()
        } catch (closeException: Throwable) {
            closeException.addSuppressed(funcException)
            throw closeException
        }
        throw funcException
    }
    close()
    return result
}

interface AsyncCloseable {
    suspend fun asyncClose()
}

fun AsyncCloseable(func: suspend () -> Unit) = object : AsyncCloseable {
    override suspend fun asyncClose() {
        func()
    }
}

suspend inline fun <T : AsyncCloseable, R> T.use(func: (T) -> R): R {
    val result = try {
        func(this)
    } catch (funcException: Throwable) {
        try {
            asyncClose()
        } catch (closeException: Throwable) {
            closeException.addSuppressed(funcException)
            throw closeException
        }
        throw funcException
    }
    asyncClose()
    return result
}