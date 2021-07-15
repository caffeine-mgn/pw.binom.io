package pw.binom.strong

private val threadLocal = ThreadLocal<Strong?>()

internal actual var STRONG_LOCAL: Strong?
    get() = threadLocal.get()
    set(value) {
        threadLocal.set(value)
    }