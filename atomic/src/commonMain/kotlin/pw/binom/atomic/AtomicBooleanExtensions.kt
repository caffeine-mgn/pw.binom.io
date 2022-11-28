package pw.binom.atomic

inline fun <T> AtomicBoolean.synchronize(func: () -> T): T {
    while (true) {
        if (compareAndSet(false, true)) {
            break
        }
    }
    try {
        return func()
    } finally {
        setValue(false)
    }
}
