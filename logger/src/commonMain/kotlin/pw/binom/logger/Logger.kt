package pw.binom.logger

import pw.binom.Date
import pw.binom.atomic.AtomicReference

object Logger {

    fun getLog(pkg: String) = loggers.getOrPut(pkg) { LoggerImpl(pkg) }

    private val loggers = HashMap<String, LoggerImpl>()

    interface Level {
        val name: String
        val priority: UInt
    }

    class LoggerImpl(val pkg: String) {
        private val _level = AtomicReference<Level?>(null)
        var level: Level?
            get() = _level.value
            set(value) {
                _level.value = value
            }

        fun log(level: Level, text: String) {
            val currentLevel = this._level.value

            if (currentLevel != null && currentLevel.priority > level.priority)
                return
            val now = Date.now()

            println("${now.year + 1900}/${(now.month + 1).dateNumber()}/${now.dayOfMonth.dateNumber()} ${now.hours.dateNumber()}:${now.min.dateNumber()}:${now.sec.dateNumber()} [${level.name}] [$pkg]: $text")
        }
    }
}

private fun Int.dateNumber() =
        if (this <= 9)
            "0$this"
        else
            this.toString()