package pw.binom.logger

class LoggerHandlerComposer(val handler1: Logger.Handler, val handler2: Logger.Handler) : Logger.Handler {
    override suspend fun log(
        logger: Logger,
        level: Logger.Level,
        text: String?,
        trace: String?,
        exception: Throwable?
    ) {
        handler1.log(
            logger = logger,
            level = level,
            text = text,
            trace = trace,
            exception = exception,
        )
        handler2.log(
            logger = logger,
            level = level,
            text = text,
            trace = trace,
            exception = exception,
        )
    }

    override fun logSync(logger: Logger, level: Logger.Level, text: String?, trace: String?, exception: Throwable?) {
        handler1.logSync(
            logger = logger,
            level = level,
            text = text,
            trace = trace,
            exception = exception,
        )
        handler2.logSync(
            logger = logger,
            level = level,
            text = text,
            trace = trace,
            exception = exception,
        )
    }
}

operator fun Logger.Handler.plus(otherHandler: Logger.Handler) = LoggerHandlerComposer(
    handler1 = this,
    handler2 = otherHandler,
)
