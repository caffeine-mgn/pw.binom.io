package pw.binom

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicInt
import pw.binom.atomic.synchronize
import pw.binom.collections.defaultMutableList

class BatchExchange<T> {
    private var read = defaultMutableList<T>()
    private var read2 = defaultMutableList<T>()

    private var internalSize = AtomicInt(0)
    val size
        get() = internalSize.getValue()

    private val exchangeLock = AtomicBoolean(false)
    private val processingLock = AtomicBoolean(false)

    fun isEmpty() = exchangeLock.synchronize {
        read.isEmpty()
    }

    fun clear() {
        processingLock.synchronize {
            exchangeLock.synchronize {
                read.clear()
                read2.clear()
                internalSize.setValue(0)
            }
        }
    }

    fun push(value: T) {
        exchangeLock.synchronize {
            read += value
            internalSize.inc()
        }
    }

    fun <R> popAll(func: (List<T>) -> R) {
        processingLock.synchronize {
            if (read.isEmpty()) {
                return
            }
            val l = exchangeLock.synchronize {
                val l = read
                read = read2
                read2 = l
                l
            }
            try {
                func(l)
            } finally {
                internalSize.addAndGet(l.size)
                l.clear()
            }
        }
    }
}
