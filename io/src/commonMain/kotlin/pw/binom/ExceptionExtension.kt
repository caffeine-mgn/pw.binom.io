package pw.binom

inline fun <R : Throwable, T> R.processing(func: (R) -> T): T = try {
    func(this)
} catch (e: Throwable) {
    e.addSuppressed(this)
    throw e
}
