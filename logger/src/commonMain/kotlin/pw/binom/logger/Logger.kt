package pw.binom.logger

import pw.binom.atomic.AtomicReference

internal expect fun createGlobalMap(): MutableMap<String, Logger>

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

        init {
            global.handler = ConsoleHandler
        }

        fun getLogger(pkg: String): Logger =
            allLoggers.getOrPut(pkg) { Logger(pkg) }
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
        )

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

    var level: Logger.Level? = null
    private val _handler = AtomicReference<Handler?>(null)
    var handler: Handler?
        get() = _handler.getValue()
        set(value) {
            _handler.setValue(value)
        }

    suspend fun log(level: Logger.Level, text: String?, trace: String? = null, exception: Throwable? = null) {
        val handler = if (this.handler != null) this.handler else if (this == global) null else global.handler
        handler?.log(
            logger = this,
            level = level,
            text = text,
            trace = trace,
            exception = exception,
        )
    }

    fun logSync(level: Logger.Level, text: String?, trace: String? = null, exception: Throwable? = null) {
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