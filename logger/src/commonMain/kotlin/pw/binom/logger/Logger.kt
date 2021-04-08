package pw.binom.logger

import pw.binom.atomic.AtomicReference
import pw.binom.concurrency.FrozenHashMap

class Logger(val pkg: String) {
    companion object {
        private val allLoggers = FrozenHashMap<String, Logger>()
        val global: Logger = Logger("")

        init {
            global.handler = ConsoleHandler
        }

        fun getLogger(pkg: String): Logger =
            allLoggers.getOrPut(pkg) { Logger(pkg) }
    }

    interface Handler {
        suspend fun log(
            logger: Logger,
            level: Level,
            text: String?,
            trace: String? = null,
            exception: Throwable?
        )

        fun logSync(
            logger: Logger,
            level: Level,
            text: String?,
            trace: String? = null,
            exception: Throwable?
        )
    }

    var level: Logger.Level? = null
    var handler by AtomicReference<Handler?>(null)
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

    interface Level {
        val name: String
        val priority: UInt
    }
}

internal fun Int.dateNumber() =
    if (this <= 9)
        "0$this"
    else
        this.toString()