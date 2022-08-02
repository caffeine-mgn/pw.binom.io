package pw.binom

import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize

class BatchExchange<T>(
    trimFactor: Float = 0.5f,
    checkSizeCounter: Int = 50
) {
    private var read = ArrayList<T>().autoTrimmed(trimFactor = trimFactor, checkSizeCounter = checkSizeCounter)
    private var read2 = ArrayList<T>().autoTrimmed(trimFactor = trimFactor, checkSizeCounter = checkSizeCounter)

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

    fun <R> popAll(func: (List<T>) -> R): R {
        processingLock.synchronize {
            val l = exchangeLock.synchronize {
                val l = read
                read = read2
                read2 = l
                l
            }
            try {
                return func(l)
            } finally {
                l.clear()
            }
        }
    }
}
