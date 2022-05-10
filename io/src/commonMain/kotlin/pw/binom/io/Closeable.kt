package pw.binom.io

fun interface Closeable {
    fun close()
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
