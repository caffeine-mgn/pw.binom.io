package pw.binom.logger

expect class Logger {
    companion object {
        val global: Logger
        val consoleHandler : Handler
        fun getLogger(pkg: String): Logger
    }

    val pkg:String
    var level: Level?
    var handler: Handler?
    fun log(level: Level, text: String?, exception: Throwable?)

    interface Level {
        val name: String
        val priority: UInt
    }


    fun interface Handler {
        fun log(logger: Logger, level: Level, text: String?, exception: Throwable?)
    }
}



internal fun Int.dateNumber() =
        if (this <= 9)
            "0$this"
        else
            this.toString()