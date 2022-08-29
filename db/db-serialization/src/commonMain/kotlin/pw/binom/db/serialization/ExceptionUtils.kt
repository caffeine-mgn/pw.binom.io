package pw.binom.db.serialization

internal inline fun <T> Throwable.wrap(func: () -> T): T =
    try {
        func()
    } catch (e: Throwable) {
        e.addSuppressed(this)
        throw e
    }
