package pw.binom.logger

import pw.binom.date.Date
import kotlin.native.concurrent.SharedImmutable

object Logger {

    fun getLog(pkg: String) = LoggerImpl(pkg)//loggers.getOrPut(pkg) { LoggerImpl(pkg) }

    @SharedImmutable
    private val loggers = HashMap<String, LoggerImpl>()

    interface Level {
        val name: String
        val priority: UInt
    }

    class LoggerImpl(val pkg: String) {
        /*
        private val _level = AtomicReference<Level?>(null)
        var level: Level?
            get() = _level.value
            set(value) {
                _level.value = value
            }
*/
        fun log(level: Level, text: String) {
            /*
            val currentLevel = this._level.value

            if (currentLevel != null && currentLevel.priority > level.priority)
                return
                */
            val now = Date(Date.now).calendar()

            println("${now.year}/${(now.month + 1).dateNumber()}/${now.dayOfMonth.dateNumber()} ${now.hours.dateNumber()}:${now.minutes.dateNumber()}:${now.seconds.dateNumber()} [${level.name}] [$pkg]: $text")
        }
    }
}

private fun Int.dateNumber() =
        if (this <= 9)
            "0$this"
        else
            this.toString()