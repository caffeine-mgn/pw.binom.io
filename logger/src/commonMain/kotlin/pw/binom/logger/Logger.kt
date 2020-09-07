package pw.binom.logger

import pw.binom.atomic.AtomicReference
import pw.binom.concurrency.StateHolder
import pw.binom.date.Date
import pw.binom.doFreeze


object Logger {

    private val stateHolder = StateHolder()

    private val loggers = stateHolder.make { HashMap<String, LoggerImpl>() }.resultOrNull!!

    val global = getLog("").also {
        it.handler = ConsoleHandler
    }

    fun getLog(pkg: String) = //LoggerImpl(pkg)//loggers.getOrPut(pkg) { LoggerImpl(pkg) }
            stateHolder.access(loggers) {
                it.getOrPut(pkg) { LoggerImpl(pkg) }
            }.resultOrNull!!

    object ConsoleHandler : Handler {
        override fun log(logger: LoggerImpl, level: Level, text: String?, exception: Throwable?) {
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

    }

    interface Level {
        val name: String
        val priority: UInt
    }


    fun interface Handler {
        fun log(logger: LoggerImpl, level: Level, text: String?, exception: Throwable?)
    }

    class LoggerImpl(val pkg: String) {
        private val _level = AtomicReference<Level?>(null)
        var level: Level?
            get() = _level.value
            set(value) {
                _level.value = value
            }

        var handler by AtomicReference<Handler?>(null)

        fun log(level: Level, text: String?, exception: Throwable?) {
            var handler = handler
            if (handler == null && this !== global) {
                handler = global.handler
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
}

private fun Int.dateNumber() =
        if (this <= 9)
            "0$this"
        else
            this.toString()