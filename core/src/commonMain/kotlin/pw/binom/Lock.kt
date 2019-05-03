package pw.binom

import pw.binom.atomic.AtomicBoolean

class Lock{
    private val atom = AtomicBoolean(false)

    fun lock() {
        while (true) {
            if (atom.compareAndSet(expected = false, new = true))
                break
            Thread.sleep(1)
        }
    }

    fun unlock() {
        if (!atom.compareAndSet(expected = true, new = false))
            throw IllegalStateException("Lock already free")
    }

    init {
        doFreeze()
    }
}

inline fun <T> Lock.use(func: () -> T): T =
        try {
            lock()
            func()
        } finally {
            unlock()
        }