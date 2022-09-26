package pw.binom

import pw.binom.collections.autoTrimmed
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize

class BatchExchange<T>(
    trimFactor: Float = 0.5f,
) {
    private var read = ArrayList<T>().autoTrimmed(trimFactor = trimFactor)
    private var read2 = ArrayList<T>().autoTrimmed(trimFactor = trimFactor)

    private val exchangeLock = SpinLock()
    private val processingLock = SpinLock()

    fun isEmpty() = exchangeLock.synchronize {
        read.isEmpty()
    }

    fun clear() {
        processingLock.synchronize {
            exchangeLock.synchronize {
                read.clear()
                read2.clear()
            }
        }
    }

    fun push(value: T) {
        exchangeLock.synchronize {
            read += value
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
                l.clear()
            }
        }
    }
}
