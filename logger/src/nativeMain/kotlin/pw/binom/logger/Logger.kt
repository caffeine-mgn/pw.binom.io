package pw.binom.logger

import pw.binom.atomic.AtomicReference
import pw.binom.concurrency.StateHolder
import pw.binom.date.Date
import pw.binom.doFreeze

actual class Logger(actual val pkg: String) {

    actual companion object {

        private val stateHolder = StateHolder()

        private val loggers = stateHolder.make { HashMap<String, Logger>() }.resultOrNull!!

        actual val consoleHandler: Handler = ConsoleHandler1()
        actual val global: Logger = getLogger("").also {
            it.handler = consoleHandler
        }

        actual fun getLogger(pkg: String): Logger =
                stateHolder.access(loggers) {
                    it.getOrPut(pkg) { Logger(pkg) }
                }.resultOrNull!!
    }

    actual interface Level {
        actual val name: String
        actual val priority: UInt
    }

    actual fun interface Handler {
        actual fun log(logger: Logger, level: Level, text: String?, exception: Throwable?)
    }

    actual var level: Level? by AtomicReference<Level?>(null)
    actual var handler: Handler? by AtomicReference<Handler?>(null)

    actual fun log(level: Level, text: String?, exception: Throwable?) {
        var handler = handler
        if (handler == null && this !== Companion.global) {
            handler = Companion.global.handler
        }
        handler?.log(
                logger = this,
                level = level,
                text = text,
                exception = exception
        )
    }

    init {
        doFreeze()
    }
}

private class ConsoleHandler1 : Logger.Handler {
    override fun log(logger: Logger, level: Logger.Level, text: String?, exception: Throwable?) {
        val currentLevel = logger.level
        if (currentLevel != null && currentLevel.priority > level.priority)
            return
        val now = Date(Date.now).calendar()

        val sb = StringBuilder()

                .append(now.year.toString())
                .append("/")
                .append((now.month + 1).dateNumber())
                .append("/")
                .append(now.dayOfMonth.dateNumber())

                .append(" ")

                .append(now.hours.dateNumber())
                .append(":")
                .append(now.minutes.dateNumber())
                .append(":")
                .append(now.seconds.dateNumber())

                .append(" [").append(level.name).append("]")
        if (logger.pkg.isNotEmpty())
            sb.append(" [${logger.pkg}]")
        sb.append(":")
        if (text != null)
            sb.append(" ").append(text)
        if (exception != null) {
            sb.append(" ").append(exception.stackTraceToString())
        }
        println(sb.toString())
    }

    init {
        doFreeze()
    }
}