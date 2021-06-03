package pw.binom.logger

import pw.binom.date.Date
import pw.binom.date.format.toDatePattern

private val dataPattern = "yyyy/MM/dd HH:mm:ssXXX".toDatePattern()

object ConsoleHandler : Logger.Handler {
    override suspend fun log(
        logger: Logger,
        level: Logger.Level,
        text: String?,
        trace: String?,
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

    override fun logSync(
        logger: Logger,
        level: Logger.Level,
        text: String?,
        trace: String?,
        exception: Throwable?
    ) {
        val currentLevel = logger.level
        if (currentLevel != null && currentLevel.priority > level.priority)
            return
        val now = Date(Date.nowTime).calendar()


        val sb = StringBuilder()
            .append(dataPattern.toString(now))
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