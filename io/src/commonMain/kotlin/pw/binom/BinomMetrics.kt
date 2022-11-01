package pw.binom

import pw.binom.atomic.AtomicBoolean
import pw.binom.io.Closeable
import pw.binom.metric.MetricProvider
import pw.binom.metric.MetricUnit

object BinomMetrics : MetricProvider {
    private val lock = AtomicBoolean(false)

    private val internalList = ArrayList<MetricUnit>()
    private val internalProviders = ArrayList<MetricProvider>()

    private inline fun <T> locking(func: () -> T): T {
        while (true) {
            if (lock.compareAndSet(false, true)) {
                break
            }
        }
        return try {
            func()
        } finally {
            lock.setValue(false)
        }
    }

    override val metrics: List<MetricUnit>
        get() = locking {
            internalProviders.flatMap { it.metrics } + internalList
        }

    fun reg(metric: MetricUnit): Closeable {
        locking {
            internalList += metric
        }
        return Closeable {
            locking {
                internalList -= metric
                internalList.trimToSize()
            }
        }
    }

    fun reg(metricProvider: MetricProvider): Closeable {
        locking {
            internalProviders += metricProvider
        }
        return Closeable {
            locking {
                internalProviders -= metricProvider
                internalProviders.trimToSize()
            }
        }
    }
}
