package pw.binom.logger

import pw.binom.atomic.AtomicReference
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize

internal fun createGlobalMap(): MutableMap<String, Logger> = HashMap()

/**
 * Logger class
 */
class Logger(
    /**
     * Logger package name. Meta information
     */
    val pkg: String
) {
    companion object {
        private val allLoggers = createGlobalMap()
        val global: Logger = Logger("")
        private val lock = SpinLock()

        init {
            global.handler = ConsoleHandler
        }

        fun getLogger(pkg: String): Logger =
            lock.synchronize {
                allLoggers.getOrPut(pkg) { Logger(pkg) }
            }

        val ofThisOrGlobal
            get() = LoggerPropertyDelegatorProvider()
    }

    /**
     * Logger handler
     */
    interface Handler {
        /**
         * Logs in async mode
         */
        suspend fun log(
            logger: Logger,
            level: Level,
            text: String?,
            trace: String? = null,
            exception: Throwable?
        ) {
            logSync(
                logger = logger,
                level = level,
                text = text,
                trace = trace,
                exception = exception,
            )
        }

        /**
         * Logs in sync mode
         */
        fun logSync(
            logger: Logger,
            level: Level,
            text: String?,
            trace: String? = null,
            exception: Throwable?
        )
    }

    var level: Level? = null
    private val _handler = AtomicReference<Handler?>(null)
    var handler: Handler?
        get() = _handler.getValue()
        set(value) {
            _handler.setValue(value)
        }

    suspend fun log(level: Level, text: String?, trace: String? = null, exception: Throwable? = null) {
        val handler = if (this.handler != null) this.handler else if (this == global) null else global.handler
        handler?.log(
            logger = this,
            level = level,
            text = text,
            trace = trace,
            exception = exception,
        )
    }

    fun logSync(level: Level, text: String?, trace: String? = null, exception: Throwable? = null) {
        val handler = if (this.handler != null) this.handler else if (this == global) null else global.handler
        handler?.logSync(
            logger = this,
            level = level,
            text = text,
            trace = trace,
            exception = exception,
        )
    }

    /**
     * Log level. Used for filter logs by level
     */
    interface Level {
        val name: String
        val priority: UInt
    }
}
